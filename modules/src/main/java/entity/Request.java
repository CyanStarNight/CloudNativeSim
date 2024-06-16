/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import core.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.Status.Created;

@Getter
@Setter
public class Request {

    private int id;
    // 请求的接口
    public API api;
    // 请求的状态
    public Status status;
    // 请求的开始时间节点
    private double startTime;
    // 延迟
    private double responseTime;
    // 服务链路
    private List<Service> serviceChain;
    private Map<Service,Double> nodeDelay = new HashMap<>();
    // 请求计数
    private static int count = 0;


    public Request(API api, double currentTime) {
        this.id = ++count;
        this.api = api;
        this.startTime = currentTime;
        this.status = Created;
    }


    public void addDelay(Service service, double delay){
        this.nodeDelay.put(service,delay);
    }


    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", API='" + api + '\'' +
                ", status=" + status +
                '}';
    }

    public List<Service> getChain() {
        return api.serviceChain;
    }

    public String getApiName(){
        return api.name;
    }

    public double getSLO(){
        return api.sloThreshold;
    }
}
