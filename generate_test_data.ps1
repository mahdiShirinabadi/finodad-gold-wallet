# Test Data Generator Script for creating 1000 test wallets
# Usage: .\generate_test_data.ps1

# Configuration
$TOTAL_WALLETS = 1000
$BASE_URL = "http://localhost:8080"
$API_ENDPOINT = "/api/v1/wallet/create"
$LOG_FILE = "wallet_generation.log"
$ACCESS_TOKEN = "your_access_token_here"  # Add your access token here

# Iranian mobile prefixes
$MOBILE_PREFIXES = @("0910", "0911", "0912", "0913", "0914", "0915", "0916", "0917", "0918", "0919", "0990", "0991", "0992", "0993", "0994", "0995", "0996", "0997", "0998", "0999")

# Initialize counters
$success_count = 0
$failure_count = 0
$start_time = Get-Date

Write-Host "Starting test data generation for $TOTAL_WALLETS wallets" -ForegroundColor Blue
Write-Host "Log file: $LOG_FILE"
Write-Host "API URL: $BASE_URL$API_ENDPOINT"
Write-Host "=================================="

# Clear log file
"" | Out-File -FilePath $LOG_FILE -Encoding UTF8

# Function to generate Iranian national code
function Generate-NationalCode {
    param($index)
    
    $base_number = 1234567890 + $index
    $national_code = $base_number.ToString("D10")
    
    # Calculate check digit (Iranian national code validation algorithm)
    $sum = 0
    for ($i = 0; $i -lt 9; $i++) {
        $digit = [int]$national_code[$i]
        $weight = 10 - $i
        $sum += $digit * $weight
    }
    
    $remainder = $sum % 11
    if ($remainder -lt 2) {
        $check_digit = $remainder
    } else {
        $check_digit = 11 - $remainder
    }
    
    return $national_code.Substring(0, 9) + $check_digit
}

# Function to generate mobile number
function Generate-Mobile {
    $random_prefix = $MOBILE_PREFIXES | Get-Random
    $random_number = Get-Random -Minimum 0 -Maximum 10000000
    return $random_prefix + $random_number.ToString("D7")
}

# Function to create wallet
function Create-Wallet {
    param($index, $national_code, $mobile)
    
    # Create JSON payload
    $json_payload = @{
        nationalCode = $national_code
        mobile = $mobile
    } | ConvertTo-Json -Compress
    
    try {
        # Make API call
        $headers = @{
            "Content-Type" = "application/json"
            "Authorization" = "Bearer $ACCESS_TOKEN"
        }
        $response = Invoke-RestMethod -Uri "$BASE_URL$API_ENDPOINT" -Method POST -Body $json_payload -Headers $headers -ErrorAction Stop
        
        $success_count++
        if ($index % 100 -eq 0) {
            Write-Host "✓ Created wallet $index`: NationalCode=$national_code, Mobile=$mobile" -ForegroundColor Green
        }
        
        # Log success
        $log_entry = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Wallet $index`: NationalCode=$national_code, Mobile=$mobile, Status=SUCCESS"
        $log_entry | Out-File -FilePath $LOG_FILE -Append -Encoding UTF8
        
    } catch {
        $failure_count++
        Write-Host "✗ Failed wallet $index`: NationalCode=$national_code, Mobile=$mobile, Error=$($_.Exception.Message)" -ForegroundColor Red
        
        # Log failure
        $log_entry = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Wallet $index`: NationalCode=$national_code, Mobile=$mobile, Status=FAILED, Error=$($_.Exception.Message)"
        $log_entry | Out-File -FilePath $LOG_FILE -Append -Encoding UTF8
    }
}

# Main loop
for ($i = 0; $i -lt $TOTAL_WALLETS; $i++) {
    $national_code = Generate-NationalCode $i
    $mobile = Generate-Mobile
    
    Create-Wallet $i $national_code $mobile
    
    # Small delay to avoid overwhelming the system
    Start-Sleep -Milliseconds 50
}

# Calculate duration
$end_time = Get-Date
$duration = ($end_time - $start_time).TotalSeconds

# Print summary
Write-Host ""
Write-Host "==================================" -ForegroundColor Blue
Write-Host "Test data generation completed!" -ForegroundColor Blue
Write-Host "Total wallets created: $success_count" -ForegroundColor Green
Write-Host "Total failures: $failure_count" -ForegroundColor Red
Write-Host "Total time: $([math]::Round($duration, 2)) seconds" -ForegroundColor Yellow
Write-Host "Average time per wallet: $([math]::Round($duration / $TOTAL_WALLETS, 3)) seconds" -ForegroundColor Yellow
Write-Host "==================================" -ForegroundColor Blue
Write-Host "Check $LOG_FILE for detailed logs"
