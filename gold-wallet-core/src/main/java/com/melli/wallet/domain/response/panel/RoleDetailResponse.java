package com.melli.wallet.domain.response.panel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailResponse {

    private Long id;
    private String name;
    private String persianDescription;
    private String additionalData;
    private Date endTime;
    private List<ResourceListResponse> resources;
    private Date createdAt;
    private Date updatedAt;
}
