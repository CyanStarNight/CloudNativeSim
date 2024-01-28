/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.nativesim.provisioner.InstancePeProvisioner;

@Getter
@Setter
public class NativePe extends Pe {

    private InstancePeProvisioner instancePeProvisioner;

    public NativePe(int id, PeProvisioner peProvisioner,InstancePeProvisioner instancePeProvisioner) {
        super(id, peProvisioner);
        this.instancePeProvisioner = instancePeProvisioner;
    }

}
