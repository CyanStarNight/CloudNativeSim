/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.event;

import org.cloudbus.nativesim.NativeController;

public class Update extends NativeEvent{

    public Update(int userId) {
        super(userId);
    }
}
