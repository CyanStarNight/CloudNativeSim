package org.cloudbus.nativesim.entity;

import lombok.*;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletScheduler;
import org.cloudbus.nativesim.NativeController;
import org.cloudbus.nativesim.util.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.cloudbus.nativesim.util.Tools.getValue;
/**
 * @author JingFeng Wu
 * the memeber of containers will be specified in file inputs.
 */
@Getter
@Setter
public class NativeContainer extends Container {//Attention: 继承的目的是为了双向映射pods和communications
    private String uid; // the global id
    Pod pod;

    public NativeContainer(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String containerManager, ContainerCloudletScheduler containerCloudletScheduler, double schedulingInterval, Pod pod) {
        super(id, userId, mips, numberOfPes, ram, bw, size, containerManager, containerCloudletScheduler, schedulingInterval);
        uid = UUID.randomUUID().toString();
        this.pod = pod;
    }
    public NativeContainer(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, ContainerCloudletScheduler cloudletScheduler,Pod pod){
        super(id, userId, mips, numberOfPes, ram, bw, size,"",cloudletScheduler,0.1);
        uid = UUID.randomUUID().toString();
        this.pod = pod;
    }
    public NativeContainer(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, ContainerCloudletScheduler cloudletScheduler){
        super(id, userId, mips, numberOfPes, ram, bw, size,"",cloudletScheduler,0.1);
        uid = UUID.randomUUID().toString();
    }

    @Override
    public void setId(int id){
        super.setId(id);
    }

    public Pod getPod() {
        return pod;
    }

    public void setPod(Pod pod) {
        this.pod = pod;
    }

    public void init(){ // link the entities and initialize the parameters.

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
        NativeContainer container = (NativeContainer) o;
        return Objects.equals(uid, container.uid) && Objects.equals(pod, container.pod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, pod);
    }

}