package com.melli.wallet.domain.response.panel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelListResponse {
    private Long id;
    private String name;
    private String username;
    private String description;
    private Boolean isActive;
    private List<RoleSummaryResponse> assignedRoles;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleSummaryResponse {
        private Long id;
        private String name;
        private String persianDescription;
    }
}
