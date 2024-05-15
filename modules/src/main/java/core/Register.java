/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;

import lombok.*;
import org.cloudbus.cloudsim.*;
import request.AppInterface;
import request.Request;
import service.*;
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
        Map<String, Object> map = Tools.readJson(servicesFile); // Assumes a method to read JSON into a Map
        ServiceGraph serviceGraph = new ServiceGraph(appId);

        // Assuming each service in the JSON has a list of dependencies or children services it communicates with
        List<ChainNode> roots = new ArrayList<>();

        // First pass: Create all ServiceNodes without setting up children
        for (Map<String, Object> m : (List<Map<String, Object>>)map.get("services")) {
            Service s = registerService(m);
            ChainNode node = new ChainNode(s);
            roots.add(node); // Initially adding all nodes to roots, will filter out non-roots later
            ChainNode.serviceNodeMap.put(s.getName(), node); // For easy lookup
        }

        // Second pass: Establish parent-child relationships
        for (Map<String, Object> m : (List<Map<String, Object>>)map.get("services")) {
            ChainNode serviceNode = ChainNode.serviceNodeMap.get(Tools.getValue(m, "name"));
            List<String> childrenNames = Tools.getValue(m, "calls"); // Assuming "dependencies" key contains child service names
            if (childrenNames != null) {
                for (String childName : childrenNames) {
                    ChainNode child = ChainNode.serviceNodeMap.get(childName);
                    serviceNode.getChildren().add(child);
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
        Map<String,Object> map =  Tools.readJson(requestsFile);
        List<Request> requests = new ArrayList<>();
        for (Map<String, Object> m : (List<Map<String,Object>>)map.get("requests")){
            // 注册请求接口
            String API = Tools.getValue(m, "API");
            int num = Tools.getValue(m, "num");
            String type = Tools.getValue(m, "method");
            AppInterface port = new AppInterface(API, num, type);
            AppInterface.map.put(API, port);
            // 注册请求体
            for (int i = 0;i<num;i++) {
                Request r = new Request(API);
                r.setPort(port);
                requests.add(r);
            }
        }
        // 因为请求是顺序输入的，需要打乱其发送顺序
        Collections.shuffle(requests);
        // 分配id
        for (int i = 0; i < requests.size(); i++) requests.get(i).setId(i);
        return requests;
    }

    public Service registerService(Map<String,Object> map){

        String service_name = Tools.getValue(map,"name");
        Service service = new Service(appId,service_name);

        service.setLabels(Tools.getValue(map,"labels"));

        List<String> apis = Tools.getValue(map, "apis");
        List<Integer> endpointsList = Tools.getValue(map, "endpoints");

        if (apis != null){
            for(int i=0; i<apis.size();i++){
                String api = apis.get(i);
                if (endpointsList != null)
                    service.getApiMap().put(api, endpointsList.get(i));
                else service.getApiMap().put(api, 1);
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

        instance.setName(Tools.getValue(map,"name"));
        instance.setLabels(Tools.getValue(map,"labels"));
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

        return instance;
    }

    @SuppressWarnings("unchecked")
    public List<Instance> registerInstanceList() {
        Map<String, Object> map = Tools.readYaml(instancesFile);
        List<Instance> instances = new ArrayList<>();
        String type = getInstanceType(); // 假设这个方法返回你想创建的实例类型

        switch (type) {
            case "Pod":
                for (Map<String, Object> m : (ArrayList<Map<String, Object>>) Tools.getValue(map, "pods")) {
                    Pod pod = (Pod) registerInstance(m);
                    pod.setNum_replicas(Tools.getValue(m, "replicas")); // 注意这里使用m而不是map
                    pod.setPrefix(Tools.getValue(m, "prefix"));
                    instances.add(pod);
                }
                break;

            case "Container":
                for (Map<String, Object> m : (ArrayList<Map<String, Object>>) Tools.getValue(map, "containers")) {
                    Container container = (Container) registerInstance(m);
                    container.setNum_replicas(Tools.getValue(m, "replicas")); // 同上，注意变量的使用
                    // container.setPrefix(getValue(m, "prefix")); // 如果Container也有这个属性
                    instances.add(container);
                }
                break;

            default:
                for (Map<String, Object> m : (ArrayList<Map<String, Object>>) Tools.getValue(map, "instances")) {
                    instances.add(registerInstance(m));
                }
                break;
        }

        return instances;
    }


    @SuppressWarnings("unchecked")
    public List<Instance> registerPods(){

        Map<String,Object> map = Tools.readYaml(instancesFile);
        List<Instance> pods = new ArrayList<>();

        for (Map<String,Object> m : (List<Map<String,Object>>)map.get("pods")){

            Pod pod = (Pod) registerInstance(m);

            pod.setNum_replicas(Tools.getValue(m,"replicas"));
            pod.setPrefix(Tools.getValue(m,"prefix"));
            pods.add(pod);
        }

        return pods;
    }


    @SuppressWarnings("unchecked")
    public List<Container> registerContainers(){

        Map<String,Object> map = Tools.readYaml(instancesFile);
        List<Container> containers = new ArrayList<>();

        for (Map<String,Object> m : (List<Map<String,Object>>)map.get("containers")){
            Container container = (Container) registerInstance(m);
            containers.add(container);
        }

        return containers;
    }


}
