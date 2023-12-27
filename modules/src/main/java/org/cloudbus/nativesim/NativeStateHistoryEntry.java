/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NativeStateHistoryEntry {
    private double time;

    /** The allocated mips. */
    private double allocatedMips;

    /** The requested mips. */
    private double requestedMips;

    /** The is in migration. */
    private boolean isInMigration;
}
