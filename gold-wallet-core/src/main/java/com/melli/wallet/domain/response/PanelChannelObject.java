package com.melli.wallet.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PanelChannelObject {
    @Schema(name = NamingProperty.ID, description = "شناسه کانال")
    @JsonProperty(NamingProperty.ID)
    private String id;
    
    @Schema(name = NamingProperty.CREATE_TIME, description = "زمان ایجاد")
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createTime;
    
    @Schema(name = NamingProperty.CREATE_BY, description = "ایجاد کننده")
    @JsonProperty(NamingProperty.CREATE_BY)
    private String createBy;
    
    @Schema(name = NamingProperty.UPDATE_TIME, description = "زمان بروزرسانی")
    @JsonProperty(NamingProperty.UPDATE_TIME)
    private String updateTime;
    
    @Schema(name = NamingProperty.UPDATE_BY, description = "بروزرسانی کننده")
    @JsonProperty(NamingProperty.UPDATE_BY)
    private String updateBy;
    
    @Schema(name = NamingProperty.FIRST_NAME, description = "نام")
    @JsonProperty(NamingProperty.FIRST_NAME)
    private String firstName;
    
    @Schema(name = NamingProperty.LAST_NAME, description = "نام خانوادگی")
    @JsonProperty(NamingProperty.LAST_NAME)
    private String lastName;
    
    @Schema(name = NamingProperty.USERNAME, description = "نام کاربری")
    @JsonProperty(NamingProperty.USERNAME)
    private String username;
    

    @Schema(name = NamingProperty.TRUST, description = "مورد اعتماد")
    @JsonProperty(NamingProperty.TRUST)
    private int trust;
    
    @Schema(name = NamingProperty.SIGN, description = "امضا")
    @JsonProperty(NamingProperty.SIGN)
    private int sign;
    
    @Schema(name = NamingProperty.PUBLIC_KEY, description = "کلید عمومی")
    @JsonProperty(NamingProperty.PUBLIC_KEY)
    private String publicKey;
    
    @Schema(name = NamingProperty.IP, description = "آی پی")
    @JsonProperty(NamingProperty.IP)
    private String ip;
    
    @Schema(name = NamingProperty.STATUS, description = "وضعیت")
    @JsonProperty(NamingProperty.STATUS)
    private String status;
    
    @Schema(name = NamingProperty.MOBILE, description = "شماره موبایل")
    @JsonProperty(NamingProperty.MOBILE)
    private String mobile;

}
