package com.melli.hub.util.pdf.sina.travel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyDetail {
    private String policyNo;
    private String agentCode;
    private String dateOfIssue;
    private String zone;
    private String validityDays;
}
