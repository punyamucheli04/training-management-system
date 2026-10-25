# =====================================================================
#  Production build: ONE container that serves the React pages and the
#  REST API from the same HTTPS address. Used only for deployment;
#  local development (mvn spring-boot:run + npm run dev) is unchanged.
#
#  Stage 1  builds React   (npm run build  -> static files)
#  Stage 2  builds Spring Boot with those files inside (src/main/resources/static)
#  Stage 3  runs only the finished .jar on a small Java runtime image
# =====================================================================

# ---- Stage 1: React -------------------------------------------------
FROM node:22-alpine AS web
WORKDIR /web
COPY frontend/package.json ./
RUN npm install --legacy-peer-deps
COPY frontend/ ./
# The API is on the same address as the pages
ENV VITE_API_URL=/api
RUN npm run build

# ---- Stage 2: Spring Boot -------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS api
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=web /web/dist ./src/main/resources/static
RUN mvn -q -B -DskipTests package

# ---- Stage 3: run ---------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
# Run as a normal user, not root
RUN useradd --system --create-home appuser
COPY --from=api /app/target/training-management-system-1.0.0.jar app.jar
USER appuser
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
