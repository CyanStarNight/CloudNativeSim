/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import java.util.*;

import core.Generator;
import lombok.*;
import core.Status;
import policy.cloudletScheduler.NativeCloudletScheduler;
import policy.scaling.ServiceScalingPolicy;


/**
 * @author JingFeng Wu
 */
@Getter
@Setter
public class Service{

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
    protected ServiceGraph serviceGraph;//TODO: 目前不支持一个service多个graph
    // cloudlets scheduler
    private NativeCloudletScheduler cloudletScheduler;
    // scaling policy
    private ServiceScalingPolicy serviceScalingPolicy;
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

    // 获取图中的子服务
    public List<Service> getChildServices() {
        if (serviceGraph == null) {
            throw new IllegalStateException("ServiceGraph is not set.");
        }
        return serviceGraph.getCalls(this);
    }

    // 获取图中的父服务
    public List<Service> getParentServices() {
        if (serviceGraph == null) {
            throw new IllegalStateException("ServiceGraph is not set.");
        }
        return serviceGraph.getParentServices(this);
    }

    // 获取服务链中的子服务
    public List<Service> getChildServicesInChain(List<Service> serviceChain) {
        List<Service> childServices = new ArrayList<>();
        for (Service service : serviceChain) {
            if (serviceChain.contains(service) && this.getChildServices().contains(service)) {
                childServices.add(service);
            }
        }
        return childServices;
    }

    // 获取服务链中的父服务
    public List<Service> getParentServicesInChain(List<Service> serviceChain) {
        List<Service> parentServices = new ArrayList<>();
        for (Service service : serviceChain) {
            if (serviceChain.contains(service) && this.getParentServices().contains(service)) {
                parentServices.add(service);
            }
        }
        return parentServices;
    }




    // 获取source服务在这条chain上的端点数量+1(本身执行&响应）
    public int getEndpoints(Request request) {
        int endpoints = 1;
        for (Service service : request.getServiceChain()) {
            if (serviceGraph.getCalls(this).contains(service)) {
                endpoints++;
            }
        }
        return endpoints;
    }


    // 创建cloudlets
    public RpcCloudlet createCloudlet(Request request, Generator generator) {
        // 获取source服务在这条chain上的端点数量
        int endpoints = getEndpoints(request);
        // 创建cloudlets
        return new RpcCloudlet(request, getName(), generator.generateCloudletLength()*endpoints);
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

    public void removeInstance(Instance instance) {
        instanceList.remove(instance);
    }

    public void setCloudletScheduler(NativeCloudletScheduler cloudletScheduler) {
        this.cloudletScheduler = cloudletScheduler;
        cloudletScheduler.setService(this);
    }


}
