package org.cloudbus.nativesim.entity;

import lombok.*;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.nativesim.network.EndPoint;
import org.cloudbus.nativesim.util.NativeStateHistoryEntry;
import org.cloudbus.nativesim.scheduler.NativeCloudletScheduler;
import org.cloudbus.nativesim.util.Status;

import java.util.*;

/**
 * @author JingFeng Wu
 * the memeber of containers will be specified in file inputs.
 */

//TODO: 2023/12/17 overbooking等属性要考虑下怎么运用，能不能简化

@Getter
@Setter
public class Container extends Instance{//Attention: 继承的目的是为了双向映射pods和communications

    private String uid; // the global id
    private int id;
    private int userId;
    private String name;
    
    private Pod pod;

    private List<? extends NativePe> peList;
    private Map<String, List<NativePe>> peMap;
    private Map<String, List<Double>> mipsMap;
    private double availableMips;

    public void setUid() {
        uid = userId + "-Container-" + id;
    }

    public static String getUid(int userId, int id) {
        return userId + "-Container-" + id;
    }

    public Container(int userId) {
        setUid();
        setUserId(userId);
    }
    public Container(int userId,String name) {
        setUid();
        setUserId(userId);
        setName(name);
    }
    public Container(
            int userId,
            double mips,
            int numberOfPes,
            int ram,
            long bw,
            long size) {
        setUserId(userId);
        setUid();
        setMips(mips);
        setNumberOfPes(numberOfPes);
        setRam(ram);
        setBw(bw);
        setSize(size);
        setInMigration(false);
        setBeingInstantiated(true);
        setCurrentAllocatedBw(0);
        setCurrentAllocatedMips(null);
        setCurrentAllocatedRam(0);
        setCurrentAllocatedSize(0);
    }


    @Override
    public String toString() {
        return "NativeContainer{" +
                "uid=" + getUid() +
                ", id=" + getId() +
                ", userId=" + getUserId() +
                ", mips=" + getMips() +
                ", numberOfPes=" + getNumberOfPes() +
                ", ram=" + getRam() +
                ", bw=" + getBw() +
                ", size=" + getSize() +
////                ", cloudletScheduler=" + super.cloudletScheduler() +
                ", pod=" + getPod().getId() + //这里不能get pod 否则会循环递归
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return Objects.equals(getUid(), container.getUid()) && Objects.equals(pod, container.pod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid());
    }

}