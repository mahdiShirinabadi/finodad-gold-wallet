package com.melli.wallet.domain.request.panel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequestJson {

    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Persian description must not exceed 500 characters")
    private String persianDescription;

    @Size(max = 1000, message = "Additional data must not exceed 1000 characters")
    private String additionalData;

    private Date endTime;
}
