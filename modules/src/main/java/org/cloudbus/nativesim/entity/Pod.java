package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import org.cloudbus.nativesim.NativeController;
import org.cloudbus.nativesim.util.Status;

import javax.validation.constraints.AssertTrue;

@Data
public class Pod {

    private String uid; // the global id
    private int id; // the id in service
    private int userId; // the user

    public String name; // really need ?

    private ArrayList<String> labels; // one service n pods
    private List<Service> serviceList;
    private List<NativeContainer> containerList;

    private String prefix; // the prefix identifying the replicas
    private List<Pod> replicas;

    private int num_containers;
    private int num_replicas;

    private int pod_cpu,pod_ram,pod_bw;
    private int storage;
    private long lifeTime; // controlled by simulation

    public Status status = Status.Ready;

    public Pod(){
        uid = UUID.randomUUID().toString();
    }

    /**Unit: Combine*/
    @AssertTrue
    public boolean matchServices(NativeController controller){
        List<Service> serviceList = new ArrayList<>();
        for (String label : labels){
            serviceList.addAll(controller.selectServicesByLabel(label));
        }
        if (!serviceList.isEmpty()) {
            setServiceList(serviceList);
            return true;
        }
        return false;
    }
    @AssertTrue
    public boolean matchReplicas(NativeController controller){
        List<Pod> replicas = controller.selectPodsByPrefix(prefix);
        if (!replicas.isEmpty()) {
            setReplicas(replicas);
            return true;
        }
        return false;
    }

    public void init(){ // link the entities and initialize the parameters.

    }
}
