@echo off
echo ========================================
echo Force Clean GRPC Module
echo ========================================

echo.
echo 1. Stopping any running Java processes...
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul

echo.
echo 2. Waiting 3 seconds...
timeout /t 3 /nobreak >nul

echo.
echo 3. Force removing target directory...
if exist "target" (
    rmdir /s /q "target" 2>nul
    if exist "target" (
        echo Target directory still exists, trying PowerShell...
        powershell -Command "Remove-Item -Path 'target' -Recurse -Force -ErrorAction SilentlyContinue"
    )
)

echo.
echo 4. Cleaning Maven cache...
call mvn dependency:purge-local-repository -DmanualInclude="com.google.protobuf:protoc"

echo.
echo 5. Rebuilding project...
call mvn clean compile

echo.
echo ========================================
echo Force clean completed!
echo ========================================
pause
