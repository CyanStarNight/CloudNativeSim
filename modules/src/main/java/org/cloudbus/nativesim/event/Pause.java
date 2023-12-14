/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.event;

import org.cloudbus.nativesim.NativeController;

public class Pause extends NativeEvent{

    public Pause(int userId) {
        super(userId);
    }
}
