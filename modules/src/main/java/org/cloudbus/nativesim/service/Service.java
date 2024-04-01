package org.cloudbus.nativesim.service;

import java.util.*;

import lombok.*;
import org.cloudbus.nativesim.extend.NativeCloudlet;
import org.cloudbus.nativesim.policy.cloudletScheduler.NativeCloudletScheduler;
import org.cloudbus.nativesim.core.Status;


/**
 * @author JingFeng Wu
 */
@Getter
@Setter
public class Service{

    private int userId;
    public String name;
    private List<String> labels;

    private List<String> apiList;

    private List<Instance> instanceList = new ArrayList<>();

    protected ServiceGraph serviceGraph;

    public Status status = Status.Ready;

    private double mips;
    private int numberOfPes;
    private int ram;
    private long bw;

    private NativeCloudletScheduler cloudletScheduler;

    private boolean inMigration;
    private boolean beingInstantiated;
    private boolean beingAllocated;

    /**
     * name -> service
     */
    public static Map<String, Service> serviceNameMap = new HashMap<>();
    public static Service getService(String name){
        return serviceNameMap.get(name);
    }
    public static List<Service> getAllServices(){
        return (List<Service>)serviceNameMap.values();
    }


    public void setName(String name){
        serviceNameMap.remove(this.name);
        this.name = name;
        serviceNameMap.put(name,this);
    }

    public Service(int userId,String name){
        setLabels(new ArrayList<>());
        setApiList(new ArrayList<>());
        setInstanceList(new ArrayList<>());
        setUserId(userId);
        setName(name);
        setBeingInstantiated(false);
        inMigration = false;
    }

    public Service(int userId,String name,ServiceGraph serviceGraph){
        new Service(userId,name);
        setServiceGraph(serviceGraph);
    }




    public double getTotalUtilizationOfCpu(double time) {
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
    }

    public double getTotalUtilizationOfCpuMips(double time) {
        return getTotalUtilizationOfCpu(time) * getMips();
    }


    public int getNum_instance() {
        return getInstanceList().size();
    }



    public static List<Instance> matchInstancesWithLabels(String serviceName){
        Service service = serviceNameMap.get(serviceName);
        return Instance.matchInstancesWithLabels(service);
    }

    public int getInDegree(){
        return ServiceTreeNode.serviceNodeMap.get(getName()).getInDegree();
    }

    public int getOutDegree(){
        return ServiceTreeNode.serviceNodeMap.get(getName()).getOutDegree();
    }

    @Override
    public String toString() {
        return "Service #" + getName();
    }


    public List<NativeCloudlet> createCloudlets(String API, int num) { //TODO: createCloudlets的逻辑需要完善

        List<NativeCloudlet> cloudlets = new ArrayList<>();
        for (int i =0 ;i < num; i++){
            cloudlets.add(new NativeCloudlet(i,API,getName()));
        }

        return cloudlets;
    }

}
