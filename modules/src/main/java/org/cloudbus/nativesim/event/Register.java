package org.cloudbus.nativesim.event;

import lombok.*;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.nativesim.NativeController;
import org.cloudbus.nativesim.entity.*;

import java.util.*;

import static org.cloudbus.nativesim.util.Tools.ReadMultilineYaml;
import static org.cloudbus.nativesim.util.Tools.getValue;

/**
 * @author JingFeng Wu
 * Register作为Entities公用注册模块
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class Register extends NativeEvent{
    public static final String signal = "Register";

    private String registerFile;
    private List< Map<String,Object> > registry; // attention: the registry will contain some records, split by "-------"

    List<Service> services = new ArrayList<>();
    List<Pod> pods = new ArrayList<>();
    List<Communication> commus = new ArrayList<>();


    public Register(int userId,String registerFile,NativeController controller) {
        super(userId); // userId will identify the controller and the simulation
        this.registerFile = registerFile;
        registry = ReadMultilineYaml(registerFile);
        this.controller = controller;
    }

    /**Unit: Register and Create*/
    // 针对结构性强的文件注册
    public void registerEntities(String serviceGraphName){

        assert checkRegistry();
        for (Map<String, Object> record : registry){

            for(Map<String , Object> map:(List<Map<String , Object>>)record.get("services"))
                services.add(registerService(map));

            for(Map<String , Object> map:(List<Map<String , Object>>)record.get("pods"))
                pods.add(registerPod(map));
        }

        registerServiceGraph(serviceGraphName,controller);

    }
    public boolean checkRegistry(){
        for (Map<String,Object> map : registry){
            if(!map.containsKey("services")) return false;
            if(!map.containsKey("pods")) return false;
        }
        return true;
    }
    // 以下方法针既可以处理单个实体的文件注册，也可以处理结构式的文件
    @NonNull
    public Service registerService(Map<String,Object> map){
        Service service = new Service(); //Register the services;
        String service_name = getValue(map,"name");
        service.setName(service_name);
        service.setLabels(getValue(map,"labels"));
        if(map.containsKey("calls")){
            List<Communication> calls = new ArrayList<>();
            for (Map<String,Object> s: (ArrayList<Map>) getValue(map,"calls"))
                calls.add(registerCommunication(s,service_name));
            service.setCalls(calls);
        }
        submit(service);
        return service;
    }
    public Communication registerCommunication(Map<String,Object> map, String origin_name){ //针对结构式注册
        Communication commu = new Communication(userId);
        commu.setOriginName(origin_name);
        commu.setDestName(getValue(map,"dest"));
        int num = getValue(map,"num");
        int len = getValue(map,"len");
        List<NativeCloudlet> data = createCloudlets(userId,num,len,0,0,1);
        commu.setData(data);
        submit(commu);
        submitCloudlets(data);
        return commu;
    }
    public Communication registerCommunication(Map<String ,Object>map){ //针对单个实体注册
        return registerCommunication(map,getValue(map,"origin"));
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
        submit(pod);
        return pod;
    }
    @NonNull
    public Container registerContainer(Map<String,Object> map,Pod pod){
        long size = Long.parseLong(getValue(map,"size").toString());
        double mips = Double.parseDouble(getValue(map,"mips").toString());
        int numberOfPes = getValue(map,"pes");
        int ram = getValue(map,"ram");
        long bw = Long.parseLong(getValue(map,"bw").toString());
        CloudletScheduler cloudletScheduler = new CloudletSchedulerTimeShared();
        Container container = new Container(userId,mips,numberOfPes,ram,bw,size);//Attention: 此处的id会在提交时被controller重置
        submit(container);
        return container;
    }


    public ServiceGraph registerServiceGraph(String graphName,NativeController controller){
        ServiceGraph sg = new ServiceGraph(userId,graphName);
        sg.init(controller);
        submit(sg);
        return sg;
    }


    //TODO: 2023/12/5 用户如何知道pes、fileSize和outputSize？这块应该放进yaml中吗？
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
