/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import java.util.*;

import core.Generator;
import lombok.*;
import core.Status;
import org.cloudbus.cloudsim.UtilizationModelFull;
import policy.cloudletScheduler.NativeCloudletScheduler;


/**
 * @author JingFeng Wu
 */
@Getter
@Setter
public class Service implements Cloneable{

    private int userId;
    // 服务名，用于映射和标识
    public String name;
    // 服务label，用于映射
    private List<String> labels = new ArrayList<>();
    // apis
    private List<String> apiList = new ArrayList<>();
    // 服务的状态
    public Status status = Status.Ready;
    private boolean beingInstantiated = false;
    private boolean beingAllocated = false;
    private boolean inMigration = false;
    //服务的实例集合
    private List<Instance> instanceList = new ArrayList<>();
    // 会分配到哪个服务图谱
    protected ServiceGraph serviceGraph;
    // cloudlets scheduler
    private NativeCloudletScheduler cloudletScheduler;
    // name map
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
        setUserId(userId);
        setName(name);
        setBeingInstantiated(false);
        inMigration = false;
    }

    public Service(int userId,String name,ServiceGraph serviceGraph){
        new Service(userId,name);
        setServiceGraph(serviceGraph);
    }



    public int getNum_instance() {
        return getInstanceList().size();
    }

    public static List<Instance> matchInstancesWithLabels(String serviceName){
        Service service = serviceNameMap.get(serviceName);
        return Instance.matchInstancesWithLabels(service);
    }

    public ChainNode getChainNode(){
        return ChainNode.serviceNodeMap.get(getName());
    }

    // 获取入度和出度
    public int getInDegree(){
        return getChainNode().getInDegree();
    }
    public int getOutDegree(){
        return getChainNode().getOutDegree();
    }

    // 获取source服务在这条chain上的端点数量(出度)
    public int getEndpoints(Request request) {
        int endpoints = 0;
        // 遍历服务链
        for (Service service : request.getServiceChain()) {
            ChainNode node = service.getChainNode();
            // 判定是不是子服务节点
            if(getChainNode().getChildren().contains(node))
                endpoints++;
        }

        // 返回子服务节点的数量
        return endpoints;
    }


    // 创建cloudlets
    public List<NativeCloudlet> createCloudlets(Request request, Generator generator) {
        List<NativeCloudlet> nativeCloudlets = new ArrayList<>();
        // 获取source服务在这条chain上的端点数量(出度+本身)
        int endpoints = getEndpoints(request) + 1;
        // 创建cloudlets
        for (int i =0 ;i < endpoints; i++){
            NativeCloudlet nativeCloudlet = new NativeCloudlet(request, getName(), generator.generateCloudletLength());
            nativeCloudlets.add(nativeCloudlet);
        }

        return nativeCloudlets;
    }


    @Override
    public Service clone() {
        Service service = null;
        try{
            service = (Service) super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return service;
    }


    @Override
    public String toString() {
        return "Service #" + getName();
    }
}
