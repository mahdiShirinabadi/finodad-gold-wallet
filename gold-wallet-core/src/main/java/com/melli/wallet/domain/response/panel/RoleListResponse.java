package com.melli.wallet.domain.response.panel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleListResponse {

    private Long id;
    private String name;
    private String persianDescription;
    private Integer resourceCount;
    private Date createdAt;
}
