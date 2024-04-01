/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.request;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.core.Status;
import org.cloudbus.nativesim.service.Instance;
import org.cloudbus.nativesim.service.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Request {

    private int id;

    public String API;

    private String uid;

    public String type;

    private RequestType characteristics;

    public Status status;

    private double startTime;

    private double responseTime;

    public static Map<String, Request> requestMap = new HashMap<>();

    public Request(int id,String API) {
        this.id = id;
        this.API = API;
        this.status = Status.Ready;
        this.uid = API+'-'+id;
    }

    public static Request getRequest(String uid){
        return requestMap.get(uid);
    }
    public static List<Request> getRequests(String API){
        return requestMap.values().stream()
                .filter(request -> request.API.equals(API))
                .toList();
    }

    public List<Instance> dispatchRequests(List<Instance> instanceList){
        return characteristics.dispatchRequests(instanceList);
    }

    public List<Service> findCalledServices(List<Service> serviceList){
        return characteristics.findCalledServices(serviceList);
    }

    public void addDelay(double delay){
        responseTime += delay;
    }

    public static void addDelay(String uid, double delay){
        requestMap.get(uid).responseTime += delay;
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", API='" + API + '\'' +
                ", status=" + status +
                '}';
    }
}
