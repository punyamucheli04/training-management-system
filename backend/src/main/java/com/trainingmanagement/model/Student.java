package com.trainingmanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A row of the <b>student</b> table (read from student_department_view).
 *
 * JSON property names are exactly the MySQL column names (e.g. "Student_ID"),
 * so the database, the REST API and the React code all use the same names.
 */
public class Student {

    @JsonProperty("Student_ID")
    private Integer studentId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("DOB")
    private LocalDate dob;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Mobile_Number")
    private String mobileNumber;

    @JsonProperty("CGPA")
    private BigDecimal cgpa;

    @JsonProperty("Dept_ID")
    private Integer deptId;

    /** from department (JOIN) - read only */
    @JsonProperty("Dept_Name")
    private String deptName;

    /** from department (JOIN) - read only */
    @JsonProperty("HOD_Name")
    private String hodName;

    /** stored function calculate_attendance_percentage() - read only */
    @JsonProperty("Attendance_Pct")
    private BigDecimal attendancePct;

    public Student() {
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getHodName() {
        return hodName;
    }

    public void setHodName(String hodName) {
        this.hodName = hodName;
    }

    public BigDecimal getAttendancePct() {
        return attendancePct;
    }

    public void setAttendancePct(BigDecimal attendancePct) {
        this.attendancePct = attendancePct;
    }
}
