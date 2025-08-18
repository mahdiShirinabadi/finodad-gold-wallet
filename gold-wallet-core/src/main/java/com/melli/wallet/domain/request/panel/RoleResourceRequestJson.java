package com.melli.wallet.domain.request.panel;

import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResourceRequestJson {

    @Schema(name = NamingProperty.ROLE_ID)
    @NumberValidation(label = "roleId")
    private Long roleId;

    @Schema(name = NamingProperty.RESOURCE_IDS)
    @NotEmpty(message = "Resource IDs list cannot be empty")
    private List<Long> resourceIds;
}
