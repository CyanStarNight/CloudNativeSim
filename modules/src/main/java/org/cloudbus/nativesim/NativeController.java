/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim;

import lombok.Data;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.nativesim.entity.*;
import org.cloudbus.nativesim.event.NativeEvent;
import org.cloudbus.nativesim.util.Status;

import javax.validation.constraints.AssertTrue;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Data
public class NativeController {
    private int userId; // every user match only one controller
    private Calendar cal; //
    private Status status; //
    private NativeEvent currentEvent; //
    private Queue<NativeEvent> queue_events;

    private ServiceGraph serviceGraph;
    private List<Service> localServices=new ArrayList<>();
    private List<Pod> localPods=new ArrayList<>();
    private List<Communication> localCommunications=new ArrayList<>();
    private List<NativeContainer> localContainers=new ArrayList<>();
    private List<NativeCloudlet> localCloudlets=new ArrayList<>();

    public NativeController(int userId, Calendar calendar){//TODO: 2023/12/7 the same user can't create two controllers
        this.userId = userId;
        this.cal = calendar;
        this.status = Status.Idle;
        NativeSim.controllers.add(this);
    }

    public void response(NativeEvent event, Status eventStatus){
        double clock = NativeSim.clock();
        switch (eventStatus){
            case Ready -> { // request
                queue_events.offer(event);
                if (status == Status.Idle){
                    Log.printLine("Processing: "+event.EventType);
                    event.handleResponse(Status.Received,clock);
                    setStatus(Status.Busy);
                    queue_events.poll();
                }else event.handleResponse(Status.Wait,clock); // continue to wait
            }
            case Error -> {
                Log.printLine("Somethings error: "+event.EventType);
                event.handleResponse(Status.Denied,clock);
                setStatus(Status.Idle);
            }
            case END -> {
                Log.printLine("Closing event: "+event.EventType);
                setStatus(Status.Idle);
                event = null;
            }
        }

    }

    //Attention: the results from Selector may be null or empty!

    /**Unit: Check Mapping*/
    @AssertTrue
    public static boolean checkMapping(ServiceGraph serviceGraph, int userId){
        return serviceGraph.getId() == userId;
    }
    @AssertTrue
    public static boolean checkMapping(Service service,Pod pod){
        return service.getLabels().stream().anyMatch(u -> pod.getLabels().contains(u));
    }
    //TODO: 2023/12/7 check container mapping
//    @AssertTrue
//    public static boolean checkMapping(Service service,NativeContainer container){
//        return service.getLabels().stream().anyMatch(u -> container.getLabel().equals(u));
//    }
//    @AssertTrue
//    public static boolean checkMapping(Pod pod,NativeContainer container){
//        return pod.getLabels().stream().anyMatch(u -> u.contains(container.getLabel()));
//    }
    @AssertTrue
    public static boolean checkMapping(Service service,Communication communication){
        return (communication.getOrigin().equals(service)) || (communication.getDest().equals(service));
    }


    /**Unit: manage ID*/

    // cloudlet id will be
    public <T> int order(T entity) {
        int id;
        Class<?> entityType = entity.getClass();
        switch (entityType.getSimpleName()) {// 获取类名
            case "Service" -> id = localServices.indexOf(entity);
            case "Pod" -> id = localPods.indexOf(entity);
            case "Communication" -> id = localCommunications.indexOf(entity);
            case "NativeContainer" -> id = localContainers.indexOf(entity);
            case "ServiceGraph" -> id = userId;
            case "NativeCloudlet" -> id = localCloudlets.indexOf(entity);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType.getSimpleName());
        }
        return id;
    }
//    public int order(Service service) {return localServices.indexOf(service);}
//    public int order(Communication communication) {return localCommunications.indexOf(communication);}
//    public int order(Pod pod) {return localPods.indexOf(pod);}
//    public int order(NativeContainer container) {return localContainers.indexOf(container);}
//    public int order(Cloudlet cloudlet) {return localContainers.indexOf(cloudlet);}

    /**Unit: Select Entities*/
    public Service selectServiceByUID(String uid){
        return localServices.stream().
                filter(u -> u.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }
    public Pod selectPodByUID(String uid){
        return localPods.stream().
                filter(u -> u.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }
    public Communication selectCommunicationByUID(String uid){
        return localCommunications.stream().
                filter(c -> c.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }
    public NativeContainer selectContainerByUID(String uid){
        return localContainers.stream().filter(c -> c.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }
    public NativeCloudlet selectCloudletByUID(String uid){
        return localCloudlets.stream().filter(c -> c.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }

    public List<Service> selectServicesByLabel(String label){
        return localServices.stream().
                filter(u -> u.getLabels().contains(label)).collect(Collectors.toList());
    }
    public List<Pod> selectPodsByLabel(String label){
        return localPods.stream().
                filter(p -> p.getLabels().contains(label)).collect(Collectors.toList());
    }
    //TODO: 2023/12/7 select Containers
//    public List<NativeContainer> selectContainersByLabel(String label){
//        return localContainers.stream().
//                filter(c -> c.getLabel().equals(label)).collect(Collectors.toList());
//    }
    public List<Pod> selectPodsByPrefix(String prefix){
        return localPods.stream().
                filter(p -> p.getPrefix().equals(prefix)).collect(Collectors.toList());
    }

    public Service selectServicesByName(String name){
        return localServices.stream().filter(u -> u.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
    /**Unit: Update States*/
    public boolean update(){

        return false;
    }

}
