/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import lombok.*;

import java.util.*;

/**
 * @author JingFeng Wu
 * the memeber of containers will be specified in file inputs.
 */


@Getter
@Setter
public class Container extends Instance{

    public final String type = "Container";

    private double availableMips;
    private int num_replicas;


    public Container(int appId) {
        super(appId);
    }

    public Container(int appId,String name) {
        super(appId);
        setName(name);
    }


    @Override
    public String toString() {
        return "NativeContainer{" +
                "uid=" + getUid() +
                ", id=" + getId() +
                ", userId=" + getAppId() +
                ", mips=" + getCurrentAllocatedMips() +
                ", ram=" + getCurrentAllocatedRam() +
                ", bw=" + getCurrentAllocatedReceiveBw() +
                ", size=" + getSize() +
////                ", cloudletScheduler=" + super.cloudletScheduler() +
//                ", pod=" + getPod().getId() + //这里不能get pod 否则会循环递归
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return Objects.equals(getUid(), container.getUid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid());
    }

}