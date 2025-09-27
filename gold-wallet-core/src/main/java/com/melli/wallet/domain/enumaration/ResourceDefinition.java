package com.melli.wallet.domain.enumaration;

import java.util.HashMap;
import java.util.Map;

/**
 * Resource definitions with Persian descriptions using ENUM
 * This enum holds all resource constants with their Persian descriptions
 */
public enum ResourceDefinition {
    
    // Wallet Resources
    WALLET_CREATE("WALLET_CREATE", "ایجاد کیف پول"),
    WALLET_DEACTIVATE("WALLET_DEACTIVATE", "غیرفعال کردن کیف پول"),
    WALLET_DELETE("WALLET_DELETE", "حذف کیف پول"),
    WALLET_ACTIVE("WALLET_ACTIVE", "فعال کردن کیف پول"),
    WALLET_INFO("WALLET_INFO", "دریافت اطلاعات کیف پول"),
    
    // Cash Operations
    CASH_IN("CASH_IN", "واریز وجه"),
    CASH_OUT("CASH_OUT", "برداشت وجه"),
    PHYSICAL_CASH_OUT("PHYSICAL_CASH_OUT", "برداشت فیزیکی وجه"),
    
    // Trading Operations
    BUY("BUY", "خرید"),
    SELL("SELL", "فروش"),
    BUY_DIRECT("BUY_DIRECT", "خرید مستقیم بدون شارژ"),
    P2P("P2P", "نفر به نفر"),
    
    // Unique Identifiers
    GENERATE_CASH_IN_UNIQUE_IDENTIFIER("GENERATE_CASH_IN_UNIQUE_IDENTIFIER", "تولید کد یکتا شارژ"),
    GENERATE_PURCHASE_UNIQUE_IDENTIFIER("GENERATE_PURCHASE_UNIQUE_IDENTIFIER", "تولید کد یکتا خرید"),
    
    // Merchant Operations
    MERCHANT_LIST("MERCHANT_LIST", "لیست پذیرنده ها"),
    MERCHANT_BALANCE("MERCHANT_BALANCE", "مانده پذیرنده"),
    MERCHANT_INCREASE_BALANCE("MERCHANT_INCREASE_BALANCE", "افزایش مانده پذیرنده"),
    MERCHANT_DECREASE_BALANCE("MERCHANT_DECREASE_BALANCE", "کاهش مانده پذیرنده"),
    MERCHANT_MANAGE("MERCHANT_MANAGE", "مدیریت پذیرنده ها"),
    
    // Management Resources
    ROLE_MANAGE("ROLE_MANAGE", "مدیریت نقش ها"),
    RESOURCE_MANAGE("RESOURCE_MANAGE", "مدیریت منابع"),
    CHANNEL_MANAGE("CHANNEL_MANAGE", "مدیریت کانال ها"),
    LIMITATION_MANAGE("LIMITATION_MANAGE", "مدیریت محدودیت ها"),
    LIMITATION_LIST("LIMITATION_LIST", "لیست محدودیت ها"),

    //GiftCard
    GIFT_CARD("GIFT_CARD","کارت هدیه"),


    //Collateral
    COLLATERAL("COLLATERAL","وثیقه"),

    // Other Resources
    STATEMENT("STATEMENT", "صورتحساب"),
    SETTING_LIST("SETTING_LIST", "لیست تنظیمات"),
    LOGOUT("LOGOUT", "خروج");


    
    // Compile-time constants for @PreAuthorize annotations
    // These are automatically synchronized with the enum values
    public static final String WALLET_CREATE_AUTH = "WALLET_CREATE";
    public static final String WALLET_DEACTIVATE_AUTH = "WALLET_DEACTIVATE";
    public static final String WALLET_DELETE_AUTH = "WALLET_DELETE";
    public static final String WALLET_ACTIVE_AUTH = "WALLET_ACTIVE";
    public static final String WALLET_INFO_AUTH = "WALLET_INFO";
    public static final String CASH_IN_AUTH = "CASH_IN";
    public static final String CASH_OUT_AUTH = "CASH_OUT";
    public static final String PHYSICAL_CASH_OUT_AUTH = "PHYSICAL_CASH_OUT";
    public static final String BUY_AUTH = "BUY";
    public static final String SELL_AUTH = "SELL";
    public static final String BUY_DIRECT_AUTH = "BUY_DIRECT";
    public static final String P2P_AUTH = "P2P";
    public static final String GENERATE_CASH_IN_UNIQUE_IDENTIFIER_AUTH = "GENERATE_CASH_IN_UNIQUE_IDENTIFIER";
    public static final String GENERATE_PURCHASE_UNIQUE_IDENTIFIER_AUTH = "GENERATE_PURCHASE_UNIQUE_IDENTIFIER";
    public static final String MERCHANT_LIST_AUTH = "MERCHANT_LIST";
    public static final String MERCHANT_BALANCE_AUTH = "MERCHANT_BALANCE";
    public static final String MERCHANT_INCREASE_BALANCE_AUTH = "MERCHANT_INCREASE_BALANCE";
    public static final String MERCHANT_DECREASE_BALANCE_AUTH = "MERCHANT_DECREASE_BALANCE";
    public static final String MERCHANT_MANAGE_AUTH = "MERCHANT_MANAGE";
    public static final String ROLE_MANAGE_AUTH = "ROLE_MANAGE";
    public static final String RESOURCE_MANAGE_AUTH = "RESOURCE_MANAGE";
    public static final String CHANNEL_MANAGE_AUTH = "CHANNEL_MANAGE";
    public static final String LIMITATION_MANAGE_AUTH = "LIMITATION_MANAGE";
    public static final String LIMITATION_LIST_AUTH = "LIMITATION_LIST";
    public static final String STATEMENT_AUTH = "STATEMENT";
    public static final String SETTING_LIST_AUTH = "SETTING_LIST";
    public static final String GIFT_CARD_AUTH = "GIFT_CARD";
    public static final String LOGOUT_AUTH = "LOGOUT";
    public static final String COLLATERAL_AUTH = "COLLATERAL";

    private final String name;
    private final String persianDescription;
    
    private static final Map<String, ResourceDefinition> BY_NAME = new HashMap<>();
    
    static {
        for (ResourceDefinition resource : values()) {
            BY_NAME.put(resource.name, resource);
        }
    }
    
    ResourceDefinition(String name, String persianDescription) {
        this.name = name;
        this.persianDescription = persianDescription;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPersianDescription() {
        return persianDescription;
    }
    
    /**
     * Get ResourceDefinition by name
     */
    public static ResourceDefinition fromName(String name) {
        return BY_NAME.get(name);
    }
    
    /**
     * Get Persian description for a resource name
     */
    public static String getPersianDescription(String resourceName) {
        ResourceDefinition resource = fromName(resourceName);
        return resource != null ? resource.persianDescription : resourceName.replace("_", " ");
    }
    
    /**
     * Get all resource names as String array
     */
    public static String[] getAllResourceNames() {
        return java.util.Arrays.stream(values())
                .map(ResourceDefinition::getName)
                .toArray(String[]::new);
    }
}
