package com.melli.wallet.domain.response.panel;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PanelShedlockObject {
    private String name;
    private String lockedBy;
    private String lockAt;
    private String lockUntil;
}
