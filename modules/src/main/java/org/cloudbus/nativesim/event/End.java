/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.event;

import org.cloudbus.nativesim.NativeController;

public class End extends NativeEvent{

    public End(int userId) {
        super(userId);
    }
}
