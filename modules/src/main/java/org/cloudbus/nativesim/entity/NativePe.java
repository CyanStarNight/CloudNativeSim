/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.nativesim.provisioner.NativePeProvisioner;
@Getter
@Setter
public class NativePe extends Pe {

    private NativePeProvisioner nativePeProvisioner;

    public NativePe(int id, NativePeProvisioner peProvisioner) {
        super(id, peProvisioner);
    }

}
