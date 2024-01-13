package org.cloudbus.nativesim;

import lombok.*;
import org.cloudbus.cloudsim.*;
import org.cloudbus.nativesim.network.Communication;
import org.cloudbus.nativesim.entity.*;
import org.cloudbus.nativesim.network.EndPoint;
import org.cloudbus.nativesim.network.Request;

import java.util.*;

import static org.cloudbus.nativesim.util.Tools.*;

/**
 * @author JingFeng Wu
 * Register作为公用注册模块
 */
@Getter
@Setter
public class Register {

    private int userId;
    private Controller controller;

    private String appName;
    ServiceGraph serviceGraph;

//    Set<String> registerSet = new HashSet<>();  // check for the repeating of registration

    public Register(int userId, Controller controller) {
        this.userId = userId;
        this.controller = controller;
    }
    public Register(int userId) {
        this.userId = userId;
    }

    public ServiceGraph registerDependencies(String appName, String dependencyFile){
        Map<String,Object> map = ReadJson(dependencyFile);
        ServiceGraph serviceGraph = new ServiceGraph(userId,appName,controller);
        List<Service> services = new ArrayList<Service>();
        for (Map m : (List<Map<String,Object>>)map.get("services"))
            services.add(registerService(m));
        serviceGraph.init();
        return serviceGraph;
    }

    public List<Request> registerRequests(String requestsFile){
        Map<String,Object> map =  ReadJson(requestsFile);
        List<Request> requests = new ArrayList<Request>();
        int shift = 0;
        for (Map m : (List<Map<String,Object>>)map.get("requests")){
            int num = getValue(m, "num");
            int i = 0;
            String type = getValue(m, "method");
            String endpoint_name = getValue(m, "endpoint");
            EndPoint endPoint = new EndPoint(userId, endpoint_name);
            while(i < num){
                Request request = new Request(i + shift, type, endPoint);
                requests.add(request);
                controller.submit(request);
                i++; // 确保递增 i
            }
            shift += num; // 更新 shift 的值
        }
        return requests;
    }


    public List<Pod> registerDeployment(String deploymentFile){
        Map<String,Object> map = ReadYaml(deploymentFile);
        List<Pod> pods = new ArrayList<Pod>();
        for (Map<String,Object> m : (List<Map<String,Object>>)map.get("pods")){
            pods.add(registerPod(m));
        }
        return pods;
    }

    //TODO: 2024/1/3 实现动态注册
    public <T> T registerEntityFromMap(String entityType, Map<String,Object> map){
        T entity = null;
        switch (entityType){
            case "Service":
                entity = (T) registerService(map);
                break;
            case "Pod":
                entity = (T) registerPod(map);
                break;
            default:
                // 处理未知的 EntityType 或抛出异常
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);

        }
        return entity;
    }

    @NonNull
    public Service registerService(Map<String,Object> map){
        Service service = new Service(); //Register the services;
        String service_name = getValue(map,"name");
        service.setName(service_name);
        service.setLabels(getValue(map,"labels"));
        List<String> tmp_names =  getValue(map,"calls");
        if(!tmp_names.isEmpty()){
            List<Communication> calls = new ArrayList<>();
            for (String dest: tmp_names)
                calls.add(new Communication(userId,service_name,dest));
            service.setCalls(calls);
            controller.submitAll(calls);
        }
        tmp_names =  getValue(map,"endpoints");
        if(!tmp_names.isEmpty()){
            List<EndPoint> endPoints = new ArrayList<>();
            for (String e: tmp_names)
                endPoints.add(new EndPoint(userId,e));
            service.setEndPoints(endPoints);
            controller.submitAll(endPoints);
        }
        controller.submit(service);
        return service;
    }

    @NonNull
    public Pod registerPod(Map<String,Object> map){
        Pod pod = new Pod();
        pod.setName(getValue(map,"name"));
        pod.setLabels(getValue(map,"labels")); //TODO: 2023/12/8 要求格式必须是 labels：/n - orders
        pod.setNum_replicas(getValue(map,"replicas"));
        pod.setStorage(getValue(map,"storage"));
        pod.setPrefix(getValue(map,"prefix"));
        if(map.containsKey("containers")){
            List<Container> containers = new ArrayList<>();
            for (Map<String,Object> c: (ArrayList<Map>) getValue(map,"containers"))
                containers.add(registerContainer(c,pod));
            pod.setContainerList(containers);
        }
        controller.submit(pod);
        return pod;
    }

    @NonNull
    public Container registerContainer(Map<String,Object> map, Pod pod){
        long size = Long.parseLong(getValue(map,"size").toString());
        double mips = Double.parseDouble(getValue(map,"mips").toString());
        int numberOfPes = getValue(map,"pes");
        int ram = getValue(map,"ram");
        long bw = Long.parseLong(getValue(map,"bw").toString());
        CloudletScheduler cloudletScheduler = new CloudletSchedulerTimeShared();
        Container container = new Container(userId,mips,numberOfPes,ram,bw,size);//Attention: 此处的id会在提交时被controller重置
        container.setPod(pod);
        controller.submit(container);
        return container;
    }

//    public EndPoint registerEndpoint(Map<String,Object> map){
//        return new EndPoint(userId,)
//    }


    public static List<NativeCloudlet> createCloudlets(int userId, int cloudletsNum, long length, long fileSize, long outputSize, int pesNumber){
        // Creates a container to store Cloudlets
        LinkedList<NativeCloudlet> list = new LinkedList<NativeCloudlet>();

        UtilizationModel utilizationModel = new UtilizationModelFull();

        NativeCloudlet[] cloudlet = new NativeCloudlet[cloudletsNum];

        for(int i=0;i<cloudletsNum;i++){
            cloudlet[i] = new NativeCloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }

}
