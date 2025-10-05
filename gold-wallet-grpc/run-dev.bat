@echo off
echo ========================================
echo Running GRPC Module in Development Mode
echo ========================================

echo.
echo 1. Building project...
call mvn clean compile -DskipTests

echo.
echo 2. Running with dev profile (PostgreSQL Database)...
call mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo.
echo ========================================
echo Application started successfully!
echo Tomcat Server: http://localhost:8080
echo GRPC Server: localhost:9090
echo Health Check: http://localhost:8080/actuator/health
echo Database: PostgreSQL (localhost:5432/wallet)
echo ========================================
pause
