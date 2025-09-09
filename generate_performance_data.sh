#!/bin/bash

# Performance Test Data Generator Script
# This script generates test data for performance testing

# Configuration
BASE_URL="http://localhost:8010/wallet"
ACCESS_TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJnZW5lcmF0ZVRpbWUiOjE3NTYyNzY2MzI5MzksInN1YiI6ImFkbWluIiwiaWF0IjoxNzU2Mjc2NjMyLCJleHAiOjE3NTYyODI2MzJ9.idu7GHFSWlxsonhpGJlJrEkPzQ8Vza8it6wM_xOQGC4CMFUXkNdg1Tgow5Zj1vAEk0-cUQlW7KHy7MWCDW9Ung"
TOTAL_USERS=1000
LOG_FILE="performance_test.log"

# Iranian mobile prefixes
MOBILE_PREFIXES=("0910" "0911" "0912" "0913" "0914" "0915" "0916" "0917" "0918" "0919" "0990" "0991" "0992" "0993" "0994" "0995" "0996" "0997" "0998" "0999")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1" | tee -a "$LOG_FILE"
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO:${NC} $1" | tee -a "$LOG_FILE"
}

# Generate national code with check digit
generate_national_code() {
    local index=$1
    
    # Use different ranges to ensure we can generate 1000 unique codes
    local base_number
    if [ $index -lt 100 ]; then
        # First 100 codes: 1000000000 to 1000000099
        base_number=$((1000000000 + index))
    elif [ $index -lt 500 ]; then
        # Next 400 codes: 2000000000 to 2000000399
        base_number=$((2000000000 + index - 100))
    elif [ $index -lt 800 ]; then
        # Next 300 codes: 3000000000 to 3000000299
        base_number=$((3000000000 + index - 500))
    else
        # Last 200 codes: 4000000000 to 4000000199
        base_number=$((4000000000 + index - 800))
    fi
    
    # Convert to string and ensure it's 10 digits
    local national_code=$(printf "%010d" $base_number)
    
    # Calculate check digit (Iranian national code validation algorithm)
    local sum=0
    for ((i=0; i<9; i++)); do
        local digit=${national_code:$i:1}
        sum=$((sum + digit * (10 - i)))
    done
    
    local remainder=$((sum % 11))
    local check_digit
    if [ $remainder -lt 2 ]; then
        check_digit=$remainder
    else
        check_digit=$((11 - remainder))
    fi
    
    # Replace the last digit with the calculated check digit
    echo "${national_code:0:9}${check_digit}"
}

# Generate mobile number
generate_mobile() {
    local prefix=${MOBILE_PREFIXES[$((RANDOM % ${#MOBILE_PREFIXES[@]}))]}
    local suffix=$(printf "%07d" $((RANDOM % 10000000)))
    echo "${prefix}${suffix}"
}

# Create user
create_user() {
    local national_code=$1
    local mobile=$2
    
    local json_payload="{\"nationalCode\":\"$national_code\",\"mobile\":\"$mobile\"}"
    
    local response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "$json_payload" \
        "$BASE_URL/api/v1/wallet/create")
    
    local http_code=$(echo "$response" | tail -n1)
    local response_body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq 200 ]; then
        log "کاربر ایجاد شد: $national_code"
        return 0
    else
        error "خطا در ایجاد کاربر $national_code: HTTP $http_code - $response_body"
        return 1
    fi
}

# Generate Cash In transaction
generate_cash_in() {
    local national_code=$1
    local amount=$((RANDOM % 9000000 + 100000)) # 100K to 10M
    
    # Generate UUID
    local uuid_json="{\"nationalCode\":\"$national_code\",\"amount\":\"$amount\",\"accountNumber\":\"$national_code\"}"
    
    local uuid_response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "$uuid_json" \
        "$BASE_URL/api/v1/cashIn/generate/uuid")
    
    local uuid_http_code=$(echo "$uuid_response" | tail -n1)
    local uuid_response_body=$(echo "$uuid_response" | head -n -1)
    
    if [ "$uuid_http_code" -eq 200 ]; then
        # Extract UUID from response (requires jq)
        local uuid=$(echo "$uuid_response_body" | jq -r '.data.uniqueIdentifier' 2>/dev/null)
        
        if [ "$uuid" != "null" ] && [ -n "$uuid" ]; then
            # Perform Cash In
            local charge_json="{\"nationalCode\":\"$national_code\",\"uniqueIdentifier\":\"$uuid\",\"amount\":\"$amount\",\"referenceNumber\":\"REF$(date +%s)\",\"accountNumber\":\"$national_code\",\"additionalData\":\"performance test\",\"cashInType\":\"NORMAL\",\"sign\":\"test_sign\",\"dataString\":\"test_data\"}"
            
            local charge_response=$(curl -s -w "\n%{http_code}" \
                -X POST \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $ACCESS_TOKEN" \
                -d "$charge_json" \
                "$BASE_URL/api/v1/cashIn/charge")
            
            local charge_http_code=$(echo "$charge_response" | tail -n1)
            
            if [ "$charge_http_code" -eq 200 ]; then
                log "Cash In انجام شد برای: $national_code (مبلغ: $amount)"
                return 0
            else
                error "خطا در Cash In برای $national_code: HTTP $charge_http_code"
                return 1
            fi
        else
            error "خطا در استخراج UUID برای $national_code"
            return 1
        fi
    else
        error "خطا در تولید UUID برای $national_code: HTTP $uuid_http_code"
        return 1
    fi
}

# Main execution
main() {
    log "شروع تولید داده‌های تست عملکرد..."
    log "تعداد کاربران: $TOTAL_USERS"
    log "URL پایه: $BASE_URL"
    
    local success_count=0
    local failure_count=0
    local start_time=$(date +%s)
    
    for ((i=1; i<=TOTAL_USERS; i++)); do
        local national_code=$(generate_national_code $i)
        local mobile=$(generate_mobile)
        
        info "ایجاد کاربر $i: $national_code"
        
        if create_user "$national_code" "$mobile"; then
            success_count=$((success_count + 1))
            
            # Generate 5 Cash In transactions for each user
            for ((j=1; j<=5; j++)); do
                generate_cash_in "$national_code"
            done
        else
            failure_count=$((failure_count + 1))
        fi
        
        # Small delay to avoid overwhelming the system
        sleep 0.1
        
        # Progress indicator
        if [ $((i % 100)) -eq 0 ]; then
            info "پیشرفت: $i/$TOTAL_USERS کاربران"
        fi
    done
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    log "تولید داده‌ها تکمیل شد!"
    log "زمان کل: ${duration} ثانیه"
    log "کاربران موفق: $success_count"
    log "کاربران ناموفق: $failure_count"
    log "نرخ موفقیت: $((success_count * 100 / TOTAL_USERS))%"
}

# Check dependencies
check_dependencies() {
    if ! command -v curl &> /dev/null; then
        error "curl نصب نشده است"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        warning "jq نصب نشده است. نصب jq برای پردازش JSON توصیه می‌شود."
        warning "sudo apt-get install jq (Ubuntu/Debian)"
        warning "brew install jq (macOS)"
    fi
}

# Show usage
usage() {
    echo "استفاده: $0 [OPTIONS]"
    echo ""
    echo "OPTIONS:"
    echo "  -h, --help     نمایش این راهنما"
    echo "  -u, --users    تعداد کاربران (پیش‌فرض: $TOTAL_USERS)"
    echo "  -t, --token    Access Token"
    echo "  -b, --base-url URL پایه API"
    echo ""
    echo "مثال:"
    echo "  $0 -u 500 -t YOUR_TOKEN -b http://localhost:8010/wallet"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            usage
            exit 0
            ;;
        -u|--users)
            TOTAL_USERS="$2"
            shift 2
            ;;
        -t|--token)
            ACCESS_TOKEN="$2"
            shift 2
            ;;
        -b|--base-url)
            BASE_URL="$2"
            shift 2
            ;;
        *)
            error "گزینه ناشناخته: $1"
            usage
            exit 1
            ;;
    esac
done

# Check dependencies and run
check_dependencies
main
