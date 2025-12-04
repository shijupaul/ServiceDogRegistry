package org.policedog.registry.domain;

import lombok.Getter;

@Getter
public enum LeavingReason {
    TRANSFERRED("Transferred"),
    RETIRED_PUT_DOWN("Retired (Put Down)"),
    KIA("KIA"),
    REJECTED("Rejected"),
    RETIRED_RE_HOUSED("Retired (Re-housed)"),
    DIED("Died");

    private final String reason;

    LeavingReason(String reason) {
        this.reason = reason;
    }
}
