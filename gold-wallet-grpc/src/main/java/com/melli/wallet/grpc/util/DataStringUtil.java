package com.melli.wallet.grpc.util;

/**
 * Class Name: DataStringUtil
 * Description: Utility class for creating data strings for signature verification in GRPC services
 */
public class DataStringUtil {

    /**
     * Create data string for CashIn operations
     * Format: uniqueIdentifier|referenceNumber|amount|nationalCode
     */
    public static String createCashInDataString(String uniqueIdentifier, String referenceNumber, String amount, String nationalCode) {
        return uniqueIdentifier + "|" + referenceNumber + "|" + amount + "|" + nationalCode;
    }

    /**
     * Create data string for CashOut operations
     * Format: uniqueIdentifier|iban|amount|nationalCode
     */
    public static String createCashOutDataString(String uniqueIdentifier, String iban, String amount, String nationalCode) {
        return uniqueIdentifier + "|" + iban + "|" + amount + "|" + nationalCode;
    }

    /**
     * Create data string for Collateral Create operations
     * Format: uniqueIdentifier|quantity|accountNumber
     */
    public static String createCollateralCreateDataString(String uniqueIdentifier, String quantity, String accountNumber) {
        return uniqueIdentifier + "|" + quantity + "|" + accountNumber;
    }

    /**
     * Create data string for Collateral Release operations
     * Format: quantity|collateralCode|nationalCode
     */
    public static String createCollateralReleaseDataString(String quantity, String collateralCode, String nationalCode) {
        return quantity + "|" + collateralCode + "|" + nationalCode;
    }

    /**
     * Create data string for Collateral Increase operations
     * Format: quantity|collateralCode|nationalCode
     */
    public static String createCollateralIncreaseDataString(String quantity, String collateralCode, String nationalCode) {
        return quantity + "|" + collateralCode + "|" + nationalCode;
    }

    /**
     * Create data string for P2P operations
     * Format: uniqueIdentifier|quantity|nationalCode|accountNumber|destAccountNumber
     */
    public static String createP2pDataString(String uniqueIdentifier, String quantity, String nationalCode, String accountNumber, String destAccountNumber) {
        return uniqueIdentifier + "|" + quantity + "|" + nationalCode + "|" + accountNumber + "|" + destAccountNumber;
    }

    /**
     * Create data string for GiftCard Process operations
     * Format: uniqueIdentifier|quantity|nationalCode
     */
    public static String createGiftCardProcessDataString(String uniqueIdentifier, String quantity, String nationalCode) {
        return uniqueIdentifier + "|" + quantity + "|" + nationalCode;
    }

    /**
     * Create data string for Physical CashOut operations
     * Format: uniqueIdentifier|quantity|nationalCode|accountNumber
     */
    public static String createPhysicalCashOutDataString(String uniqueIdentifier, String quantity, String nationalCode, String accountNumber) {
        return uniqueIdentifier + "|" + quantity + "|" + nationalCode + "|" + accountNumber;
    }

    /**
     * Create data string for Purchase Buy operations
     * Format: uniqueIdentifier|quantity|price|nationalCode|walletAccountNumber
     */
    public static String createPurchaseBuyDataString(String uniqueIdentifier, String quantity, String price, String nationalCode, String walletAccountNumber) {
        return uniqueIdentifier + "|" + quantity + "|" + price + "|" + nationalCode + "|" + walletAccountNumber;
    }

    /**
     * Create data string for Purchase Sell operations
     * Format: uniqueIdentifier|quantity|price|nationalCode|walletAccountNumber
     */
    public static String createPurchaseSellDataString(String uniqueIdentifier, String quantity, String price, String nationalCode, String walletAccountNumber) {
        return uniqueIdentifier + "|" + quantity + "|" + price + "|" + nationalCode + "|" + walletAccountNumber;
    }
}
