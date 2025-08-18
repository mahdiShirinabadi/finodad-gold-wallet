package com.melli.wallet.domain.request.panel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRoleRequestJson {

    @NotNull(message = "Role ID is required")
    private Long roleId;

    @NotNull(message = "Channel ID is required")
    private Long channelId;
}
