/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.event;

import org.cloudbus.nativesim.NativeController;

public class Schedule extends NativeEvent{

    public Schedule(int userId) {
        super(userId);
    }
}
