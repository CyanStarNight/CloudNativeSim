package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import lombok.*;
import org.cloudbus.nativesim.Controller;
import org.cloudbus.nativesim.network.EndPoint;
import org.cloudbus.nativesim.scheduler.NativeCloudletScheduler;
import org.cloudbus.nativesim.util.NativeStateHistoryEntry;
import org.cloudbus.nativesim.util.Status;

import javax.validation.constraints.AssertTrue;

/**
 * @author JingFeng Wu
 */
/** Attention: pods对模拟的意义似乎更体现在空间资源和架构解释上
 * 1.All the containers share the same namespace、storage、lifetime and process action.
 * 2.Pod will connect the services and containers with double linkages.
 * 3.Pods are the basic units of scheduling for users.
 * 4.Use replicaSet to implements horizontal scaling.
 * */
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class Pod extends Instance{

    private String uid; // the global id
    private int id;
    private int userId;
    public String name; // really need ?

    private String prefix; // the prefix identifying the replicas
    private List<Pod> replicas;
    private int num_replicas;

    private List<Container> containerList;
    private int num_containers;

    public void setUid() {
        uid = userId + "-Pod-" + id;
    }

    public static String getUid(int userId, int id) {
        return userId + "-Pod-" + id;
    }

    public Pod(int userId) {
        setUid();
        setUserId(userId);
    }
    public Pod(int userId,String name) {
        setUid();
        setUserId(userId);
        setName(name);
    }


    @Override
    public String toString() {
        return "\n"+this.getClass().getSimpleName()+":\n"
                +"name: "+name
                +"\nid: "+id;
    }
}
