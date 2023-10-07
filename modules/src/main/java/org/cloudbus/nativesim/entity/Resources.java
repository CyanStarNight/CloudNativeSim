package org.cloudbus.nativesim.entity;

import lombok.Data;

/**
 * @author JingFeng Wu
 */

@Data
public class Resources implements Cloneable {
    private long size;

    private double mips;

    private int numberOfPes;

    private  float ram;

    private int mem;

    private long bw;

    @Override
    public Resources clone() {
        try {
            Resources clone = (Resources) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
