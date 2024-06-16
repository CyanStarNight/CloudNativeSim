/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;

import lombok.*;
import org.cloudbus.cloudsim.*;
import entity.API;
import entity.*;
import util.Tools;

import java.util.*;

/**
 * @author JingFeng Wu
 * Register实现了基于文件的注册机制，
 * 通过文件描述定义了各个实体的初始配置，
 * 对于空字段使用缺省配置。
 */

@Getter
@Setter
public class Register {

    private int appId;
    public String instanceType;

    private Datacenter datacenter;

    private String instancesFile;
    private String servicesFile;

    public Register(int appId, String instanceType) {
        this.appId = appId;
        this.instanceType = instanceType;
    }

    public Register(int appId, String instanceType, String servicesFile, String instancesFile){
        this.appId = appId;
        this.instanceType = instanceType;
        setServicesFile(servicesFile);
        setInstancesFile(instancesFile);
    }


    @SuppressWarnings("unchecked")
    public List<API> registerAPIs(){
        List<API> APIs = new ArrayList<>();
        Map<String,Object> map =  Tools.readJson(servicesFile);
        for (Map<String, Object> m : (List<Map<String,Object>>)map.get("APIs")){
            String name = Tools.getValue(m, "name");
            double weight = Tools.getValue(m, "weight");
            APIs.add(new API(name,weight));
        }
        return APIs;
    }

    public Service registerService(Map<String,Object> map){
        String service_name = Tools.getValue(map,"name");
        Service service = new Service(appId,service_name);
        service.setLabels(Tools.getValue(map,"labels"));
        service.setApiList(Tools.getValue(map, "APIs"));
//        System.out.println("Register: Service " + service.getName()+" has been registered.");
        return service;
    }


    @SuppressWarnings("unchecked")
    public ServiceGraph registerServiceGraph() {
        Map<String, Object> map = Tools.readJson(servicesFile); // Assumes a method to read JSON into a Map
        ServiceGraph serviceGraph = new ServiceGraph(appId);

        // First pass: Create all Service instances
        Map<String, Service> serviceMap = new HashMap<>();
        for (Map<String, Object> m : (List<Map<String, Object>>) map.get("services")) {
            Service s = registerService(m);
            serviceMap.put(s.getName(), s);
            serviceGraph.addService(s, null); // Add service to the graph without parents initially
        }

        // Second pass: Establish parent-child relationships
        for (Map<String, Object> m : (List<Map<String, Object>>) map.get("services")) {
            Service parentService = serviceMap.get(m.get("name"));
            List<String> childrenNames = (List<String>) m.get("calls"); // Assuming "calls" key contains child service names
            if (childrenNames != null) {
                for (String childName : childrenNames) {
                    Service childService = serviceMap.get(childName);
                    serviceGraph.addService(childService, parentService); // Establish parent-child relationship
                }
            }
        }

        return serviceGraph;
    }


    // 实例的注册是多个replica同时注册的
    public List<Instance> registerInstances(Map<String,Object> map, String type){
        int num_replicas = Tools.getValue(map,"replicas");
        List<Instance> replicas = new ArrayList<>();
        String prefix = Tools.getValue(map,"prefix");

        for (int i=0;i<num_replicas;i++){

            Instance instance;
            // 将 type 转换为小写以实现不区分大小写的匹配
            String normalizedType = type.toLowerCase();
            switch (normalizedType){
                case "pod":
                    instance = new Pod(appId,prefix);
                    break;
                case "container":
                    instance = new Container(appId,prefix);
                    break;
                default:

                    instance = new Instance(appId,prefix);; // 默认构造
                    break;
            }
            // 标签
            instance.setLabels(Tools.getValue(map,"labels"));
            // 初始化资源
            instance.setSize(Long.parseLong(Tools.getValue(map,"size").toString()));
            if (Tools.getValue(map, "requests.ram")!=null)
                instance.setRequests_ram(Tools.getValue(map, "requests.ram"));
            if (Tools.getValue(map, "requests.share")!=null)
                instance.setRequests_share(Tools.getValue(map, "requests.share"));
            if (Tools.getValue(map, "requests.mips")!=null)
                instance.setRequests_mips(Tools.getValue(map, "requests.mips"));


            if (Tools.getValue(map, "limits.ram")!=null)
                instance.setLimits_ram(Tools.getValue(map, "limits.ram"));
            if (Tools.getValue(map, "limits.share")!=null)
                instance.setLimits_share(Tools.getValue(map, "limits.share"));
            if (Tools.getValue(map, "limits.mips")!=null)
                instance.setLimits_mips(Tools.getValue(map, "limits.mips"));

            instance.setReceive_bw(Double.parseDouble(Tools.getValue(map,"rec_bw").toString()));
            instance.setTransmit_bw(Double.parseDouble(Tools.getValue(map,"trans_bw").toString()));
            // 副本
            replicas.add(instance); // 引用传递
        }

        return replicas;
    }

    @SuppressWarnings("unchecked")
    public List<Instance> registerAllInstances() {
        Map<String, Object> map = Tools.readYaml(instancesFile);
        List<Instance> allInstances = new ArrayList<>();
        for (Map<String, Object> m : (ArrayList<Map<String, Object>>) Tools.getValue(map, "instances")) {
            String type = Tools.getValue(m, "type");
            allInstances.addAll(registerInstances(m,type));
        }

        return allInstances;
    }


    @SuppressWarnings("unchecked")
    public Pod registerPod(Map<String,Object> map){
        Pod pod = new Pod(appId,Tools.getValue(map,"prefix"));
        // others
        return pod;
    }


    @SuppressWarnings("unchecked")
    public Container registerContainer(Map<String,Object> map){
        Container container = new Container(appId,Tools.getValue(map,"prefix"));
        // others
        return container;
    }

    public ServiceGraph registerServiceGraphTest(int serviceCount) {
        Map<String, Object> map = Tools.readJson(servicesFile); // Assumes a method to read JSON into a Map
        ServiceGraph serviceGraph = new ServiceGraph(appId);

        Map<String, Service> serviceMap = new HashMap<>();
        while (serviceCount > 0) {
            // First pass: Create all Service instances
            for (Map<String, Object> m : (List<Map<String, Object>>) map.get("services")) {
                Service s = registerService(m);
                serviceMap.put(s.getName(), s);
                serviceGraph.addService(s, null); // Add service to the graph without parents initially
            }
            serviceCount--;
        }

        // Second pass: Establish parent-child relationships
        for (Map<String, Object> m : (List<Map<String, Object>>) map.get("services")) {
            Service parentService = serviceMap.get(m.get("name"));
            List<String> childrenNames = (List<String>) m.get("calls"); // Assuming "calls" key contains child service names
            if (childrenNames != null) {
                for (String childName : childrenNames) {
                    Service childService = serviceMap.get(childName);
                    serviceGraph.addService(childService, parentService); // Establish parent-child relationship
                }
            }
        }

        return serviceGraph;
    }
}
