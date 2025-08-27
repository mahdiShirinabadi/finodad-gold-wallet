@echo off
setlocal enabledelayedexpansion

REM Test Data Generator Script for creating 1000 test wallets
REM Usage: generate_test_data.bat

REM Configuration
set TOTAL_WALLETS=1000
set BASE_URL=http://localhost:8080
set API_ENDPOINT=/api/v1/wallet/create
set LOG_FILE=wallet_generation.log
set ACCESS_TOKEN=your_access_token_here

REM Initialize counters
set success_count=0
set failure_count=0
set start_time=%time%

echo Starting test data generation for %TOTAL_WALLETS% wallets
echo Log file: %LOG_FILE%
echo API URL: %BASE_URL%%API_ENDPOINT%
echo ==================================

REM Clear log file
echo. > %LOG_FILE%

REM Main loop
for /l %%i in (0,1,%TOTAL_WALLETS%) do (
    REM Generate national code (simple approach for batch)
    set /a base_number=1234567890+%%i
    set national_code=!base_number!
    
    REM Generate mobile number (simple approach)
    set /a random_prefix=910+%%i%%20
    set /a random_number=%%i%%10000000
    set mobile=09!random_prefix!!random_number!
    
    REM Create JSON payload
    set json_payload={"nationalCode":"!national_code!","mobile":"!mobile!"}
    
    REM Make API call
    for /f "tokens=*" %%r in ('curl -s -w "%%{http_code}" -X POST -H "Content-Type: application/json" -H "Accept: application/json" -H "Authorization: Bearer %ACCESS_TOKEN%" -d "!json_payload!" "%BASE_URL%%API_ENDPOINT%"') do set response=%%r
    
    REM Extract status code (last line)
    for /f "tokens=*" %%s in ('echo !response! ^| findstr /r "[0-9][0-9][0-9]$"') do set status_code=%%s
    
    REM Log the request
    echo [%date% %time%] Wallet %%i: NationalCode=!national_code!, Mobile=!mobile!, Status=!status_code! >> %LOG_FILE%
    
    if !status_code!==200 (
        set /a success_count+=1
        if %%i%%100==0 (
            echo ✓ Created wallet %%i: NationalCode=!national_code!, Mobile=!mobile!
        )
    ) else (
        set /a failure_count+=1
        echo ✗ Failed wallet %%i: NationalCode=!national_code!, Mobile=!mobile!, Status=!status_code!
    )
    
    REM Small delay
    timeout /t 1 /nobreak >nul
)

REM Calculate duration (simplified)
set end_time=%time%

echo.
echo ==================================
echo Test data generation completed!
echo Total wallets created: %success_count%
echo Total failures: %failure_count%
echo Start time: %start_time%
echo End time: %end_time%
echo ==================================
echo Check %LOG_FILE% for detailed logs

pause
