/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.cloudletScheduler;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.schedulers.ContainerScheduler;

import java.util.List;

public class NativeCloudletScheduler extends ContainerScheduler {
    public NativeCloudletScheduler(List<? extends ContainerPe> pelist) {
        super(pelist);
    }

    @Override
    public boolean allocatePesForContainer(Container container, List<Double> list) {
        return false;
    }

    @Override
    public void deallocatePesForContainer(Container container) {

    }
}
