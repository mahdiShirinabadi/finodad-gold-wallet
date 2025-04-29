package com.melli.wallet.domain.response.limitation;

import lombok.*;

import java.util.List;

/**
 * Class Name: LimitationListResponse
 * Author: Mahdi Shirinabadi
 * Date: 4/23/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LimitationListResponse {
    private List<LimitationObject> limitationObjectList;
}
