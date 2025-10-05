@echo off
echo ========================================
echo Building GRPC Module (No Tests)
echo ========================================

echo.
echo 1. Cleaning project...
call mvn clean

echo.
echo 2. Compiling without tests...
call mvn compile -DskipTests -Dmaven.test.skip=true

echo.
echo 3. Building package without tests...
call mvn package -DskipTests -Dmaven.test.skip=true

echo.
echo ========================================
echo Build completed successfully!
echo ========================================
pause
