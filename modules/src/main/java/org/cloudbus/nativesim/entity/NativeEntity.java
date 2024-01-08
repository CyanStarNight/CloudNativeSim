/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.cloudbus.nativesim.util.NativeStateHistoryEntry;
import org.cloudbus.nativesim.scheduler.NativeCloudletScheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class NativeEntity {
    private String uid; // the global id
    private int id;
    private int userId;
    private String name;

    private long size;
    private double mips;
    private int numberOfPes;
    private int ram;
    private long bw;
    private long currentAllocatedSize;
    private int currentAllocatedRam;
    private long currentAllocatedBw;
    private List<Double> currentAllocatedMips;


    private NativeCloudletScheduler cloudletScheduler;

    private boolean inMigration;
    private boolean beingInstantiated;

    private final List<NativeStateHistoryEntry> stateHistory = new LinkedList<NativeStateHistoryEntry>();

    public void setUid() {
        uid = UUID.randomUUID().toString();
    }

    public static String getUid(int userId, int id) {
        return userId + "-" + id;
    }

    public NativeEntity(int userId) {
        setUid();
        setUserId(userId);
    }
    public NativeEntity(int userId,String name) {
        setUid();
        setUserId(userId);
        setName(name);
    }

    public double getTotalUtilizationOfCpu(double time) {
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
    }

    public double getTotalUtilizationOfCpuMips(double time) {
        return getTotalUtilizationOfCpu(time) * getMips();
    }

    public void addStateHistoryEntry(
            double time,
            double allocatedMips,
            double requestedMips,
            boolean isInMigration) {
        NativeStateHistoryEntry newState = new NativeStateHistoryEntry(
                time,
                allocatedMips,
                requestedMips,
                isInMigration);
        if (!getStateHistory().isEmpty()) {
            NativeStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
        getStateHistory().add(newState);
    }

    @Override
    public String toString() {
        return "\n"+this.getClass().getSimpleName()+":\n"
                +"name: "+name
                +"\nid: "+id;
    }
}
