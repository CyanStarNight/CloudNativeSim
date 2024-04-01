package org.cloudbus.nativesim.service;

import java.util.List;

import lombok.*;

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

    public final String type = "Pod";

    private String prefix; // the prefix identifying the replicas
    private List<Pod> replicas;
    private int num_replicas;

    private List<Container> containerList;

    public Pod(int appId) {
        super(appId);
    }

    public Pod(int appId,String name) {
        super(appId);
        setName(name);
    }


    @Override
    public String toString() {
        return "\n"+this.getClass().getSimpleName()+":\n"
                +"name: "+getName()
                +"\nid: "+getId();
    }
}
