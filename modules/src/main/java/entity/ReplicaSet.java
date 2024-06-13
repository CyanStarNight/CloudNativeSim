/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReplicaSet {

    private List<Instance> replicas = new ArrayList<>();

    private String prefix;

    public static Map<String,ReplicaSet> replicaSetMap = new HashMap<>();

    public ReplicaSet(String prefix) {
        this.prefix = prefix;
    }


    public int indexReplica(Instance instance){
        return replicas.indexOf(instance);
    }

    public void add(Instance instance){
        replicas.add(instance);
    }

    public void remove(Instance instance){
        replicas.remove(instance);
    }

    public int getReplicaCount(){
        return replicas.size();
    }

    public Instance replicate() {
        assert !getReplicas().isEmpty();
        Instance behavior = getReplicas().get(0);

        return behavior.clone();
    }
}
