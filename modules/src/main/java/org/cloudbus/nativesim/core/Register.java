/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.core;

import lombok.*;
import org.cloudbus.cloudsim.*;
import org.cloudbus.nativesim.request.Request;
import org.cloudbus.nativesim.policy.cloudletScheduler.NativeCloudletSchedulerTimeShared;
import org.cloudbus.nativesim.service.*;
import org.cloudbus.nativesim.request.RequestType;

import java.util.*;

import static org.cloudbus.nativesim.service.ServiceTreeNode.serviceNodeMap;
import static org.cloudbus.nativesim.util.Tools.*;

/**
 * @author JingFeng Wu
 * Register作为公用注册模块
 */
@Getter
@Setter
public class Register {

    private int appId;
    public String instanceType;

    private Datacenter datacenter;

    private String instancesFile;
    private String servicesFile;
    private String requestsFile;

    public Register(int appId, String instanceType) {
        this.appId = appId;
        this.instanceType = instanceType;
    }

    public Register(int appId, String instanceType, String servicesFile, String instancesFile, String requestsFile){
        this.appId = appId;
        this.instanceType = instanceType;
        setServicesFile(servicesFile);
        setInstancesFile(instancesFile);
        setRequestsFile(requestsFile);
    }

    @SuppressWarnings("unchecked")
    public ServiceGraph registerServiceGraph() {
        Map<String, Object> map = ReadJson(servicesFile); // Assumes a method to read JSON into a Map
        ServiceGraph serviceGraph = new ServiceGraph(appId);

        // Assuming each service in the JSON has a list of dependencies or children services it communicates with
        List<ServiceTreeNode> roots = new ArrayList<>();

        // First pass: Create all ServiceNodes without setting up children
        for (Map<String, Object> m : (List<Map<String, Object>>)map.get("services")) {
            Service s = registerService(m);
            ServiceTreeNode node = new ServiceTreeNode(s);
            roots.add(node); // Initially adding all nodes to roots, will filter out non-roots later
            serviceNodeMap.put(s.getName(), node); // For easy lookup
        }

        // Second pass: Establish parent-child relationships
        for (Map<String, Object> m : (List<Map<String, Object>>)map.get("services")) {
            ServiceTreeNode parent = serviceNodeMap.get(getValue(m, "name"));
            List<String> childrenNames = getValue(m, "calls"); // Assuming "dependencies" key contains child service names
            if (childrenNames != null) {
                for (String childName : childrenNames) {
                    ServiceTreeNode child = serviceNodeMap.get(childName);
                    parent.getChildren().add(child);
                    roots.remove(child); // Remove from roots since it's not a root
                }
            }
        }

        serviceGraph.setRoots(roots); // Only root nodes remain in the roots list
        serviceGraph.buildServiceChains(); // Build the chains
        return serviceGraph;
    }


    @SuppressWarnings("unchecked")
    public List<Request> registerRequests(){
        Map<String,Object> map =  ReadJson(requestsFile);
        List<Request> requests = new ArrayList<>();
        int shift = 0;
        for (Map<String, Object> m : (List<Map<String,Object>>)map.get("requests")){

            String API = getValue(m, "API");
            int num = getValue(m, "num");
            String type = getValue(m, "method");

            if (!RequestType.map.containsKey(API))
                RequestType.map.put(API,new RequestType(num, type,API));

            for (int i = 0;i<num;i++) {
                Request r = new Request(i + shift, API);
                r.setCharacteristics(RequestType.map.get(API));
                requests.add(r);
            }

            shift += num; // 更新 shift 的值
        }

        return requests;
    }

    public Service registerService(Map<String,Object> map){

        String service_name = getValue(map,"name");
        Service service = new Service(appId,service_name);

        service.setLabels(getValue(map,"labels"));

        List<String> apis = getValue(map, "apis");
        if (apis != null){
            for(String api: apis){
                service.getApiList().add(api);
            }
        }
//        System.out.println("Register: Service " + service.getName()+" has been registered.");
        return service;
    }


    public Instance registerInstance(Map<String,Object> map){

        Instance instance;
        if ("Pod".equals(getInstanceType())) {
            instance = new Pod(appId);
        } else if ("Container".equals(getInstanceType())) {
            instance = new Container(appId); // 同上
        } else {
            instance = new Instance(appId);; // 默认构造
        }

        instance.setName(getValue(map,"name"));
        instance.setLabels(getValue(map,"labels"));
        instance.setSize(Long.parseLong(getValue(map,"size").toString()));

        if (getValue(map, "requests.ram")!=null)
            instance.setRequests_ram(getValue(map, "requests.ram"));
        if (getValue(map, "requests.share")!=null)
            instance.setRequests_share(getValue(map, "requests.share"));
        if (getValue(map, "requests.mips")!=null)
            instance.setRequests_mips(getValue(map, "requests.mips"));


        if (getValue(map, "limits.ram")!=null)
            instance.setLimits_ram(getValue(map, "limits.ram"));
        if (getValue(map, "limits.share")!=null)
            instance.setLimits_share(getValue(map, "limits.share"));
        if (getValue(map, "limits.mips")!=null)
            instance.setLimits_mips(getValue(map, "limits.mips"));

        instance.setReceive_bw(Double.parseDouble(getValue(map,"rec_bw").toString()));
        instance.setTransmit_bw(Double.parseDouble(getValue(map,"trans_bw").toString()));

        return instance;
    }

    @SuppressWarnings("unchecked")
    public List<Instance> registerInstanceList() {
        Map<String, Object> map = ReadYaml(instancesFile);
        List<Instance> instances = new ArrayList<>();
        String type = getInstanceType(); // 假设这个方法返回你想创建的实例类型

        switch (type) {
            case "Pod":
                for (Map<String, Object> m : (ArrayList<Map<String, Object>>) getValue(map, "pods")) {
                    Pod pod = (Pod) registerInstance(m);
                    pod.setNum_replicas(getValue(m, "replicas")); // 注意这里使用m而不是map
                    pod.setPrefix(getValue(m, "prefix"));
                    instances.add(pod);
                }
                break;

            case "Container":
                for (Map<String, Object> m : (ArrayList<Map<String, Object>>) getValue(map, "containers")) {
                    Container container = (Container) registerInstance(m);
                    container.setNum_replicas(getValue(m, "replicas")); // 同上，注意变量的使用
                    // container.setPrefix(getValue(m, "prefix")); // 如果Container也有这个属性
                    instances.add(container);
                }
                break;

            default:
                for (Map<String, Object> m : (ArrayList<Map<String, Object>>) getValue(map, "instances")) {
                    instances.add(registerInstance(m));
                }
                break;
        }

        return instances;
    }


    @SuppressWarnings("unchecked")
    public List<Instance> registerPods(){

        Map<String,Object> map = ReadYaml(instancesFile);
        List<Instance> pods = new ArrayList<>();

        for (Map<String,Object> m : (List<Map<String,Object>>)map.get("pods")){

            Pod pod = (Pod) registerInstance(m);

            pod.setNum_replicas(getValue(m,"replicas"));
            pod.setPrefix(getValue(m,"prefix"));
            pods.add(pod);
        }

        return pods;
    }


    @SuppressWarnings("unchecked")
    public List<Container> registerContainers(){

        Map<String,Object> map = ReadYaml(instancesFile);
        List<Container> containers = new ArrayList<>();

        for (Map<String,Object> m : (List<Map<String,Object>>)map.get("containers")){
            Container container = (Container) registerInstance(m);
            containers.add(container);
        }

        return containers;
    }


}
