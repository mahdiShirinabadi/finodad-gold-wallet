package com.melli.hub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Sort {
    private static final String ORDER_ASC = "asc";
    private static final String ORDER_DESC = "desc";
    private String field;
    private String order;
}
