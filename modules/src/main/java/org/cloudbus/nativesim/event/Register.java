package org.cloudbus.nativesim.event;

import lombok.Data;
import lombok.NonNull;
import org.cloudbus.nativesim.entity.DataCenter;
import org.cloudbus.nativesim.entity.Pod;
import org.cloudbus.nativesim.entity.Service;
import org.cloudbus.nativesim.entity.ServiceChain;
import org.cloudbus.nativesim.util.Edge;
import org.cloudbus.nativesim.util.Vertex;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * @author JingFeng Wu
 *  Register会继承SimEvent的规则和属性
 *  与Init不同，它负责注册各种实体、策略和设备，是面向用户的事件
 */
// TODO: 2023/9/26 是否要注册设备？目前默认硬件资源充足
@Data
public class Register extends SimEvent{
    private String config_file_path,commu_file_path;
    private List< Map<String,Object> > config; // the configuration of software application
    private List<String> deployment; //the deployment of the hardware equipment
    private int[] costs;

    public Register(String config_file, String commu_file,List<String> deployment_parameters) {
        config_file_path= config_file;
        config = ReadYaml(config_file);
        commu_file_path = commu_file;
        deployment = deployment_parameters;
    }

    public Register(String config_file, String commu_file,List<String> deployment_parameters,int[] costs) {
        config_file_path= config_file;
        config = ReadYaml(config_file);
        commu_file_path = commu_file;
        deployment = deployment_parameters;
        this.costs = costs;
    }

    public static List< Map<String,Object> > ReadYaml(String filePath) {
        InputStream inputStream = null;
        try{
            inputStream = new FileInputStream(filePath);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        List< Map<String,Object> > config = new ArrayList<>();
        for (Object obj : yaml.loadAll(inputStream)){
            Map<String, Object> map = (Map<String, Object>) obj;
            config.add(map);
        }
        return config;
    }

    ServiceChain ServiceChainRegistry(){
        ServiceChain serviceChain = new ServiceChain();
        List<Vertex> services = new ArrayList<>();
        List<Pod> pods = new ArrayList<>();
    
        for (Map<String, Object> map : this.config){
            if (map.get("kind").equals("Service"))
                services.add(Service.ServiceRegistry(map));
            if (map.get("kind").equals("Deployment"))
                pods.add(Pod.PodRegistry(map));
            else continue;
        }
    
        serviceChain.setServices(services);
        serviceChain.setPods(pods);

        List<Edge> communications = new ArrayList<>();
        Map<String,Object> map = ReadYaml(this.commu_file_path).get(0);
        int index = 0;
        for(Map s: (ArrayList<Map>) map.get("services")){  //iterate the services
            String tailVertex_name = (String) s.get("name");
            for (String headVertex_name: (ArrayList<String>) s.get("call")){
                Edge e = new Edge();
                Vertex tailV = serviceChain.findVertex(tailVertex_name);
                Vertex headV = serviceChain.findVertex(headVertex_name);
                e.setTailVec(tailV);
                e.setHeadVec(headV);
                if (costs != null) e.setCost(costs[index++]);
                serviceChain.buildNodeOutEdge(tailV,e);
                serviceChain.buildNodeInEdge(headV,e);
                communications.add(e);
            }
        }
        serviceChain.communications = communications;
        serviceChain.setCommunications(communications);

        return serviceChain;
    }
    // TODO: 2023/8/15 利用inputStream动态更新

    List<DataCenter> DataCenterRegistry(){
        List<DataCenter> dataCenters = new ArrayList<>();


        return dataCenters;
    }
}
