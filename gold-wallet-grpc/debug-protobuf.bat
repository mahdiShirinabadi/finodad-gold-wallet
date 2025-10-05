@echo off
echo ========================================
echo GRPC Protobuf Debug Script
echo ========================================

echo.
echo 1. Cleaning target directory...
call mvn clean

echo.
echo 2. Checking Maven version...
call mvn --version

echo.
echo 3. Checking Java version...
java -version

echo.
echo 4. Attempting to compile protobuf files...
call mvn protobuf:compile -X

echo.
echo 5. If step 4 fails, trying with explicit OS classifier...
call mvn protobuf:compile -Dos.detected.classifier=windows-x86_64 -X

echo.
echo 6. If still failing, trying to download protoc manually...
call mvn dependency:get -Dartifact=com.google.protobuf:protoc:3.25.5:exe:windows-x86_64

echo.
echo ========================================
echo Debug completed. Check output above for errors.
echo ========================================
pause
