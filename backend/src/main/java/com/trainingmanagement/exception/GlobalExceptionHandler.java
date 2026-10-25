package com.trainingmanagement.exception;

import com.trainingmanagement.model.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts every exception into a clean JSON error with the right HTTP status.
 *
 * Database errors arrive as Spring DataAccessException objects that wrap the
 * original java.sql.SQLException. We read the MySQL error code from that
 * SQLException to produce a message a user can understand.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // MySQL error codes (see MySQL "Server Error Message Reference")
    private static final int ER_DUP_ENTRY = 1062;
    private static final int ER_ROW_IS_REFERENCED = 1451;     // delete parent that has children
    private static final int ER_NO_REFERENCED_ROW = 1452;     // child points to missing parent
    private static final int ER_BAD_NULL = 1048;
    private static final int ER_DATA_TOO_LONG = 1406;
    private static final int ER_OUT_OF_RANGE = 1264;
    private static final int ER_TRUNCATED_WRONG_VALUE = 1292;
    private static final int ER_SIGNAL_EXCEPTION = 1644;     // SIGNAL from a trigger
    private static final int ER_NO_SUCH_TABLE = 1146;        // missing table or view
    private static final int ER_SP_DOES_NOT_EXIST = 1305;    // missing procedure or function
    private static final int ER_VIEW_INVALID = 1356;

    private static final Pattern FK_TABLE = Pattern.compile("REFERENCES `(\\w+)`");

    // ---------------------------------------------------------------- app exceptions

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ApiError> handleTooManyAttempts(TooManyAttemptsException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ---------------------------------------------------------------- request problems

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "The request body is not valid JSON or a value has the wrong format "
                        + "(dates must be YYYY-MM-DD, times HH:MM, numbers must be numeric).");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value '" + ex.getValue() + "' for '" + ex.getName() + "'. A numeric ID is expected.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "Missing request parameter '" + ex.getParameterName() + "'.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method " + ex.getMethod() + " is not supported here.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "No API endpoint at /" + ex.getResourcePath());
    }

    // ---------------------------------------------------------------- database problems

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<ApiError> handleNoConnection(CannotGetJdbcConnectionException ex) {
        log.error("Cannot connect to MySQL: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "Cannot connect to the MySQL database. Make sure MySQL is running and the "
                        + "username/password in backend/src/main/resources/application.properties are correct.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabase(DataAccessException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (!(cause instanceof SQLException sqlEx)) {
            log.error("Database error", ex);
            return build(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred.");
        }

        int code = sqlEx.getErrorCode();
        String msg = sqlEx.getMessage() == null ? "" : sqlEx.getMessage();

        // Errors raised on purpose by our triggers: SIGNAL SQLSTATE '45000'
        if (code == ER_SIGNAL_EXCEPTION || "45000".equals(sqlEx.getSQLState())) {
            return build(HttpStatus.BAD_REQUEST, msg);
        }

        switch (code) {
            case ER_DUP_ENTRY:
                if (msg.toLowerCase().contains("email")) {
                    return build(HttpStatus.CONFLICT, "This email address is already used by another record.");
                }
                if (msg.contains("PRIMARY")) {
                    return build(HttpStatus.CONFLICT, "A record with this ID already exists.");
                }
                return build(HttpStatus.CONFLICT, "Duplicate value: " + msg);

            case ER_NO_REFERENCED_ROW:
                return build(HttpStatus.BAD_REQUEST, "The selected " + referencedName(msg) + " does not exist.");

            case ER_ROW_IS_REFERENCED:
                return build(HttpStatus.CONFLICT,
                        "This record is used by other records (foreign key) and cannot be deleted.");

            case ER_BAD_NULL:
            case ER_DATA_TOO_LONG:
            case ER_OUT_OF_RANGE:
            case ER_TRUNCATED_WRONG_VALUE:
                return build(HttpStatus.BAD_REQUEST, "Invalid data: " + msg);

            case ER_NO_SUCH_TABLE:
            case ER_SP_DOES_NOT_EXIST:
            case ER_VIEW_INVALID:
                log.error("Missing database object: {}", msg);
                return build(HttpStatus.INTERNAL_SERVER_ERROR,
                        "A required database object is missing (" + msg + "). Run views.sql, functions.sql, "
                                + "procedures.sql and triggers.sql from the database folder (see README).");

            default:
                log.error("Database error {}: {}", code, msg);
                return build(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred (MySQL error " + code + ").");
        }
    }

    // ---------------------------------------------------------------- anything else

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred on the server.");
    }

    // ---------------------------------------------------------------- helpers

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), status.getReasonPhrase(), message));
    }

    /** Turns "... REFERENCES `training_session` ..." into "session". */
    private String referencedName(String mysqlMessage) {
        Matcher m = FK_TABLE.matcher(mysqlMessage);
        if (!m.find()) {
            return "related record";
        }
        return switch (m.group(1)) {
            case "department" -> "department";
            case "student" -> "student";
            case "trainer" -> "trainer";
            case "training" -> "training";
            case "training_session" -> "session";
            default -> m.group(1);
        };
    }
}
