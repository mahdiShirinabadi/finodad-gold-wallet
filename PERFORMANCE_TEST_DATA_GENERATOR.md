# راهنمای تولید داده‌های تست عملکرد

## 🎯 **هدف**
تولید داده‌های تست برای بررسی عملکرد سیستم در شرایط بار بالا

## 📊 **استراتژی تولید داده**

### **مرحله 1: ایجاد کاربران (1000 کاربر)**
```bash
# ایجاد 1000 کیف پول
curl -X POST "http://localhost:8010/wallet/api/v1/wallet/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "nationalCode": "1234567890",
    "mobile": "09123456789"
  }'
```

### **مرحله 2: تراکنش‌های Cash In**
```bash
# تولید UUID برای Cash In
curl -X POST "http://localhost:8010/wallet/api/v1/cashIn/generate/uuid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "nationalCode": "1234567890",
    "amount": "1000000",
    "accountNumber": "1234567890"
  }'

# انجام Cash In
curl -X POST "http://localhost:8010/wallet/api/v1/cashIn/charge" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "nationalCode": "1234567890",
    "uniqueIdentifier": "UUID_FROM_PREVIOUS_STEP",
    "amount": "1000000",
    "referenceNumber": "REF123456",
    "accountNumber": "1234567890",
    "additionalData": "test data",
    "cashInType": "NORMAL",
    "sign": "SIGNATURE",
    "dataString": "DATA_STRING"
  }'
```

### **مرحله 3: تراکنش‌های خرید و فروش**
```bash
# تولید UUID برای خرید
curl -X POST "http://localhost:8010/wallet/api/v1/purchase/buy/generate/uuid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "nationalCode": "1234567890",
    "quantity": "10.5",
    "price": "2500000",
    "accountNumber": "1234567890",
    "merchantId": "MERCHANT001",
    "currency": "GOLD"
  }'

# انجام خرید
curl -X POST "http://localhost:8010/wallet/api/v1/purchase/buy" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "nationalCode": "1234567890",
    "uniqueIdentifier": "UUID_FROM_PREVIOUS_STEP",
    "quantity": "10.5",
    "totalPrice": "26250000",
    "walletAccountNumber": "1234567890",
    "additionalData": "test purchase",
    "merchantId": "MERCHANT001",
    "sign": "SIGNATURE",
    "dataString": "DATA_STRING"
  }'
```

### **مرحله 4: تراکنش‌های Cash Out**
```bash
# تولید UUID برای Cash Out
curl -X POST "http://localhost:8010/wallet/api/v1/cashOut/generate/uuid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "nationalCode": "1234567890",
    "amount": "500000",
    "accountNumber": "1234567890"
  }'

# انجام Cash Out
curl -X POST "http://localhost:8010/wallet/api/v1/cashOut/withdraw" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "nationalCode": "1234567890",
    "uniqueIdentifier": "UUID_FROM_PREVIOUS_STEP",
    "amount": "500000",
    "referenceNumber": "REF789012",
    "accountNumber": "1234567890",
    "additionalData": "test withdrawal",
    "sign": "SIGNATURE",
    "dataString": "DATA_STRING"
  }'
```

## 🚀 **اسکریپت‌های خودکار**

### **1. اسکریپت Java (توصیه شده)**
```java
@SpringBootTest
@ActiveProfiles("dev")
public class PerformanceDataGenerator {
    
    private static final int TOTAL_USERS = 1000;
    private static final int CASH_IN_PER_USER = 5;
    private static final int PURCHASE_PER_USER = 10;
    private static final int CASH_OUT_PER_USER = 3;
    
    @Test
    public void generatePerformanceData() {
        // 1. ایجاد کاربران
        List<String> nationalCodes = createUsers();
        
        // 2. Cash In برای هر کاربر
        for (String nationalCode : nationalCodes) {
            generateCashInTransactions(nationalCode, CASH_IN_PER_USER);
        }
        
        // 3. تراکنش‌های خرید
        for (String nationalCode : nationalCodes) {
            generatePurchaseTransactions(nationalCode, PURCHASE_PER_USER);
        }
        
        // 4. Cash Out برای هر کاربر
        for (String nationalCode : nationalCodes) {
            generateCashOutTransactions(nationalCode, CASH_OUT_PER_USER);
        }
    }
}
```

### **2. اسکریپت Bash**
```bash
#!/bin/bash

BASE_URL="http://localhost:8010/wallet"
ACCESS_TOKEN="YOUR_ACCESS_TOKEN"
TOTAL_USERS=1000

# تولید کد ملی تصادفی
generate_national_code() {
    echo $((1000000000 + RANDOM % 9000000000))
}

# تولید شماره موبایل تصادفی
generate_mobile() {
    prefixes=("0910" "0911" "0912" "0913" "0914" "0915" "0916" "0917" "0918" "0919")
    prefix=${prefixes[$RANDOM % ${#prefixes[@]}]}
    echo "${prefix}$(printf "%07d" $((RANDOM % 10000000)))"
}

# ایجاد کاربر
create_user() {
    local national_code=$1
    local mobile=$2
    
    curl -s -X POST "$BASE_URL/api/v1/wallet/create" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "{
            \"nationalCode\": \"$national_code\",
            \"mobile\": \"$mobile\"
        }"
}

# تولید تراکنش Cash In
generate_cash_in() {
    local national_code=$1
    local amount=$((RANDOM % 10000000 + 100000))
    
    # تولید UUID
    local uuid_response=$(curl -s -X POST "$BASE_URL/api/v1/cashIn/generate/uuid" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "{
            \"nationalCode\": \"$national_code\",
            \"amount\": \"$amount\",
            \"accountNumber\": \"$national_code\"
        }")
    
    local uuid=$(echo $uuid_response | jq -r '.data.uniqueIdentifier')
    
    # انجام Cash In
    curl -s -X POST "$BASE_URL/api/v1/cashIn/charge" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "{
            \"nationalCode\": \"$national_code\",
            \"uniqueIdentifier\": \"$uuid\",
            \"amount\": \"$amount\",
            \"referenceNumber\": \"REF$(date +%s)\",
            \"accountNumber\": \"$national_code\",
            \"additionalData\": \"performance test\",
            \"cashInType\": \"NORMAL\",
            \"sign\": \"test_sign\",
            \"dataString\": \"test_data\"
        }"
}

# اجرای اصلی
main() {
    echo "شروع تولید داده‌های تست عملکرد..."
    
    for i in $(seq 1 $TOTAL_USERS); do
        national_code=$(generate_national_code)
        mobile=$(generate_mobile)
        
        echo "ایجاد کاربر $i: $national_code"
        create_user $national_code $mobile
        
        # تولید 5 تراکنش Cash In برای هر کاربر
        for j in $(seq 1 5); do
            generate_cash_in $national_code
        done
        
        # تاخیر کوتاه
        sleep 0.1
    done
    
    echo "تولید داده‌ها تکمیل شد!"
}

main
```

### **3. اسکریپت PowerShell**
```powershell
$BASE_URL = "http://localhost:8010/wallet"
$ACCESS_TOKEN = "YOUR_ACCESS_TOKEN"
$TOTAL_USERS = 1000

function Generate-NationalCode {
    $random = Get-Random -Minimum 1000000000 -Maximum 9999999999
    return $random.ToString()
}

function Generate-Mobile {
    $prefixes = @("0910", "0911", "0912", "0913", "0914", "0915", "0916", "0917", "0918", "0919")
    $prefix = $prefixes | Get-Random
    $suffix = (Get-Random -Minimum 0 -Maximum 10000000).ToString("D7")
    return "$prefix$suffix"
}

function Create-User {
    param($NationalCode, $Mobile)
    
    $body = @{
        nationalCode = $NationalCode
        mobile = $Mobile
    } | ConvertTo-Json
    
    $headers = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $ACCESS_TOKEN"
    }
    
    try {
        $response = Invoke-RestMethod -Uri "$BASE_URL/api/v1/wallet/create" -Method POST -Body $body -Headers $headers
        Write-Host "کاربر ایجاد شد: $NationalCode"
        return $true
    }
    catch {
        Write-Host "خطا در ایجاد کاربر $NationalCode : $($_.Exception.Message)"
        return $false
    }
}

function Generate-CashIn {
    param($NationalCode)
    
    $amount = Get-Random -Minimum 100000 -Maximum 10000000
    
    # تولید UUID
    $uuidBody = @{
        nationalCode = $NationalCode
        amount = $amount.ToString()
        accountNumber = $NationalCode
    } | ConvertTo-Json
    
    $headers = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $ACCESS_TOKEN"
    }
    
    try {
        $uuidResponse = Invoke-RestMethod -Uri "$BASE_URL/api/v1/cashIn/generate/uuid" -Method POST -Body $uuidBody -Headers $headers
        $uuid = $uuidResponse.data.uniqueIdentifier
        
        # انجام Cash In
        $chargeBody = @{
            nationalCode = $NationalCode
            uniqueIdentifier = $uuid
            amount = $amount.ToString()
            referenceNumber = "REF$(Get-Date -UFormat %s)"
            accountNumber = $NationalCode
            additionalData = "performance test"
            cashInType = "NORMAL"
            sign = "test_sign"
            dataString = "test_data"
        } | ConvertTo-Json
        
        Invoke-RestMethod -Uri "$BASE_URL/api/v1/cashIn/charge" -Method POST -Body $chargeBody -Headers $headers
        Write-Host "Cash In انجام شد برای: $NationalCode"
    }
    catch {
        Write-Host "خطا در Cash In برای $NationalCode : $($_.Exception.Message)"
    }
}

# اجرای اصلی
Write-Host "شروع تولید داده‌های تست عملکرد..."

for ($i = 1; $i -le $TOTAL_USERS; $i++) {
    $nationalCode = Generate-NationalCode
    $mobile = Generate-Mobile
    
    Write-Host "ایجاد کاربر $i : $nationalCode"
    $success = Create-User -NationalCode $nationalCode -Mobile $mobile
    
    if ($success) {
        # تولید 5 تراکنش Cash In برای هر کاربر
        for ($j = 1; $j -le 5; $j++) {
            Generate-CashIn -NationalCode $nationalCode
        }
    }
    
    # تاخیر کوتاه
    Start-Sleep -Milliseconds 100
}

Write-Host "تولید داده‌ها تکمیل شد!"
```

## 📈 **نظارت بر عملکرد**

### **1. مانیتورینگ دیتابیس**
```sql
-- تعداد رکوردها در هر جدول
SELECT 
    'wallet' as table_name, COUNT(*) as record_count FROM wallet
UNION ALL
SELECT 'wallet_account', COUNT(*) FROM wallet_account
UNION ALL
SELECT 'request', COUNT(*) FROM request
UNION ALL
SELECT 'cash_in_request', COUNT(*) FROM cash_in_request
UNION ALL
SELECT 'cash_out_request', COUNT(*) FROM cash_out_request
UNION ALL
SELECT 'purchase_request', COUNT(*) FROM purchase_request;

-- بررسی عملکرد کوئری‌ها
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

### **2. مانیتورینگ اپلیکیشن**
```java
// اضافه کردن متریک‌های عملکرد
@Timed(value = "wallet.creation.time", description = "زمان ایجاد کیف پول")
@Timed(value = "transaction.processing.time", description = "زمان پردازش تراکنش")
@Counted(value = "wallet.creation.count", description = "تعداد ایجاد کیف پول")
```

## ⚠️ **نکات مهم**

### **1. مدیریت حافظه**
- استفاده از Batch Processing برای تراکنش‌های زیاد
- تنظیم Connection Pool مناسب
- مانیتورینگ Heap Memory

### **2. مدیریت دیتابیس**
- تنظیم Auto-vacuum
- مانیتورینگ Index Usage
- تنظیم Work Memory مناسب

### **3. امنیت**
- استفاده از Access Token معتبر
- محدود کردن Rate Limiting
- لاگ کردن تراکنش‌های تست

## 🎯 **مراحل اجرا**

1. **آماده‌سازی**: تنظیم Access Token و URL های صحیح
2. **ایجاد کاربران**: اجرای اسکریپت ایجاد 1000 کاربر
3. **تولید تراکنش‌ها**: اجرای تراکنش‌های Cash In, Purchase, Cash Out
4. **نظارت**: مانیتورینگ عملکرد سیستم
5. **تحلیل**: بررسی نتایج و بهینه‌سازی

## 📊 **نتایج مورد انتظار**

- **1000 کیف پول** با حساب‌های GOLD و RIAL
- **5000 تراکنش Cash In** (5 تراکنش برای هر کاربر)
- **10000 تراکنش خرید/فروش** (10 تراکنش برای هر کاربر)
- **3000 تراکنش Cash Out** (3 تراکنش برای هر کاربر)
- **مجموع: 18000 تراکنش** برای تست عملکرد
