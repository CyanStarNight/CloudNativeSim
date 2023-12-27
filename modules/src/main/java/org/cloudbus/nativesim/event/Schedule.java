/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.event;

import org.cloudbus.nativesim.Log;
import org.cloudbus.nativesim.NativeController;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.NativeVm;
import org.cloudbus.nativesim.entity.Pod;

public class Schedule extends NativeEvent{

    public Schedule(int userId) {
        super(userId);
    }

    protected void deallocate(Container container, NativeVm vm) {
        Log.printLine("Deallocated the VM:......" + vm.getId());
        container.getRamProvisioner().deallocateRamForContainer(container);
        getContainerBwProvisioner().deallocateBwForContainer(container);
        getContainerScheduler().deallocatePesForContainer(container);
        setSize(getSize() + container.getSize());
    }
}
