package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.cloudbus.nativesim.util.Status;
import org.cloudbus.nativesim.util.Vertex;
import org.cloudbus.nativesim.util.*;

/**
 * @author JingFeng Wu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Service extends Vertex implements SimEntity {

    private int id;

    private int userId;

    Status status = Status.Ready;

    public String name;

    public ArrayList<String> labels;

    public String type;

    private List<Map<String , Object>> ports;

    /** The Service itself does not need to explicitly specify resource requirements and limitations.
     * Therefore, the design here is the real-time resource consumption of pods.*/
    Resources realtime_resources;

    private Cloudlet serviceCloudlet;

    /**
     *  ReplicaList can be none, it means the service has no replicas.
     *  Assume that every replica has the same amount of resource.
     *  The resource of service equals to the total of the resource of replicas, but less than the need of service.
     */

    private int numReplicas;

//    private ArrayList<Service> replica;

    private ArrayList<Pod> EndPoints;

    private int num_pods = 1;

    public Service() {
    }
    /**
     * ServicesRegistry
     */
//    @NonNull
//    public static List<Vertex> ServicesRegistry(List<Map<String,Object>> config){
//        List<Vertex> services = new ArrayList<>(); //Register the services;
//        for (Map<String, Object> map : config){
//            if (!map.get("kind").equals("Service")) continue;
//            Service service = new Service();
//            service.setName(Tools.getValue(map,"metadata.name"));
////            service.setLabels(Tools.getValue(map,"metadata.labels"));
//            service.setPorts(Tools.getValue(map,"spec.ports"));
//            service.setType(Tools.getValue(map,"spec.type"));
////            service.setEndPoints(EndPointsRegistry());
//            services.add(service);
//        }
//        return services;
//    }

    @NonNull
    public static Service ServiceRegistry(Map<String,Object> map){
        Service service = new Service(); //Register the services;
        assert (map.get("kind").equals("Service")):"not Service.";

        service.setName(Tools.getValue(map,"metadata.name"));
//        service.setLabels(Tools.getValue(map,"metadata.labels"));
        service.setPorts(Tools.getValue(map,"spec.ports"));
        service.setType(Tools.getValue(map,"spec.type"));

        return service;
    }
}
