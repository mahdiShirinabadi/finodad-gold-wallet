#!/bin/bash

# Test Data Generator Script for creating 1000 test wallets
# Usage: ./generate_test_data.sh

# Configuration
TOTAL_WALLETS=1000
BASE_URL="http://localhost:8080"
API_ENDPOINT="/api/v1/wallet/create"
LOG_FILE="wallet_generation.log"
ACCESS_TOKEN="your_access_token_here"  # Add your access token here

# Iranian mobile prefixes
MOBILE_PREFIXES=("0910" "0911" "0912" "0913" "0914" "0915" "0916" "0917" "0918" "0919" "0990" "0991" "0992" "0993" "0994" "0995" "0996" "0997" "0998" "0999")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Initialize counters
success_count=0
failure_count=0
start_time=$(date +%s)

echo -e "${BLUE}Starting test data generation for $TOTAL_WALLETS wallets${NC}"
echo "Log file: $LOG_FILE"
echo "API URL: $BASE_URL$API_ENDPOINT"
echo "=================================="

# Function to generate Iranian national code
generate_national_code() {
    local index=$1
    local base_number=$((1234567890 + index))
    local national_code=$(printf "%010d" $base_number)
    
    # Calculate check digit (Iranian national code validation algorithm)
    local sum=0
    for ((i=0; i<9; i++)); do
        local digit=${national_code:$i:1}
        local weight=$((10 - i))
        sum=$((sum + digit * weight))
    done
    
    local remainder=$((sum % 11))
    local check_digit=0
    if [ $remainder -lt 2 ]; then
        check_digit=$remainder
    else
        check_digit=$((11 - remainder))
    fi
    
    echo "${national_code:0:9}$check_digit"
}

# Function to generate mobile number
generate_mobile() {
    local random_prefix=${MOBILE_PREFIXES[$((RANDOM % ${#MOBILE_PREFIXES[@]}))]}
    local random_number=$(printf "%07d" $((RANDOM % 10000000)))
    echo "$random_prefix$random_number"
}

# Function to create wallet
create_wallet() {
    local index=$1
    local national_code=$2
    local mobile=$3
    
    # Create JSON payload
    local json_payload="{\"nationalCode\":\"$national_code\",\"mobile\":\"$mobile\"}"
    
    # Make API call
    local response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "Accept: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "$json_payload" \
        "$BASE_URL$API_ENDPOINT")
    
    # Extract status code (last line)
    local status_code=$(echo "$response" | tail -n1)
    local response_body=$(echo "$response" | head -n -1)
    
    # Log the request
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Wallet $index: NationalCode=$national_code, Mobile=$mobile, Status=$status_code" >> "$LOG_FILE"
    
    if [ "$status_code" -eq 200 ] || [ "$status_code" -eq 201 ]; then
        success_count=$((success_count + 1))
        if [ $((index % 100)) -eq 0 ]; then
            echo -e "${GREEN}✓ Created wallet $index: NationalCode=$national_code, Mobile=$mobile${NC}"
        fi
    else
        failure_count=$((failure_count + 1))
        echo -e "${RED}✗ Failed wallet $index: NationalCode=$national_code, Mobile=$mobile, Status=$status_code${NC}"
        echo "Response: $response_body" >> "$LOG_FILE"
    fi
}

# Main loop
for ((i=0; i<TOTAL_WALLETS; i++)); do
    national_code=$(generate_national_code $i)
    mobile=$(generate_mobile)
    
    create_wallet $i "$national_code" "$mobile"
    
    # Small delay to avoid overwhelming the system
    sleep 0.05
done

# Calculate duration
end_time=$(date +%s)
duration=$((end_time - start_time))

# Print summary
echo ""
echo "=================================="
echo -e "${BLUE}Test data generation completed!${NC}"
echo -e "${GREEN}Total wallets created: $success_count${NC}"
echo -e "${RED}Total failures: $failure_count${NC}"
echo -e "${YELLOW}Total time: ${duration} seconds${NC}"
echo -e "${YELLOW}Average time per wallet: $(echo "scale=2; $duration / $TOTAL_WALLETS" | bc) seconds${NC}"
echo "=================================="
echo "Check $LOG_FILE for detailed logs"
