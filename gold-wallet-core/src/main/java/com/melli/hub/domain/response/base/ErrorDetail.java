package com.melli.hub.domain.response.base;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    private String message;
    private Integer code; // Optional: for application-specific error codes
    // Additional fields can be added here, such as a list of fields that caused the error
}
