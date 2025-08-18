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
public class ResourceDetailResponse {

    private Long id;
    private String name;
    private String faName;
    private Integer display;
    private Date createdAt;
    private Date updatedAt;
}
