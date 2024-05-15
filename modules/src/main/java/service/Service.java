/*
 * Copyright ©2024. Jingfeng Wu.
 */

package service;

import java.util.*;

import lombok.*;
import core.Status;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import request.Request;


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
    // api -> endpoints
    private Map<String,Integer> apiMap = new HashMap<>();
    // 服务的状态
    public Status status = Status.Ready;
    private boolean beingInstantiated = false;
    private boolean beingAllocated = false;
    private boolean inMigration = false;
    //服务的实例集合
    private List<Instance> instanceList = new ArrayList<>();
    // 会分配到哪个服务图谱
    protected ServiceGraph serviceGraph;

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

    // 返回api列表
    public List<String> getApiList() {
        return apiMap.keySet().stream().toList();
    }

    // 获取入度和出度
    public int getInDegree(){
        return ChainNode.serviceNodeMap.get(getName()).getInDegree();
    }
    public int getOutDegree(){
        return ChainNode.serviceNodeMap.get(getName()).getOutDegree();
    }

    // 创建cloudlets
    public List<NativeCloudlet> createCloudlets(Request request, int num) {

        List<NativeCloudlet> nativeCloudlets = new ArrayList<>();
        for (int i =0 ;i < num; i++){
            NativeCloudlet nativeCloudlet = new NativeCloudlet(i, request, getName(),
                    new UtilizationModelStochastic(),
                    new UtilizationModelStochastic(),
                    new UtilizationModelStochastic());
            nativeCloudlets.add(nativeCloudlet);
            distributeCloudlets(nativeCloudlet);
        }

        return nativeCloudlets;
    }

    public void distributeCloudlets(NativeCloudlet nativeCloudlet) {
        // 分发cloudlets到该服务的实例上
        List<Instance> instanceList = getInstanceList();
        // 按cloudlet数量重新从小到大排序
        instanceList.sort(Comparator.comparingInt(i -> i.getCloudletScheduler().getWaitingQueue().size()));
        Instance selectedInstance = instanceList.get(0);
        nativeCloudlet.setInstanceUid(selectedInstance.getUid());
        selectedInstance.getCloudletScheduler().receiveCloudlet(nativeCloudlet);

    }

    @Override
    public String toString() {
        return "Service #" + getName();
    }
}
