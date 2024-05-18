/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import core.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static core.Status.Created;

@Getter
@Setter
public class Request {

    private int id;
    // 请求的接口
    public API api;
    // 请求的状态
    public Status status;
    // 请求的开始时间
    private double startTime;
    // 请求的响应时间
    private double responseTime;
    // 服务链路
    private List<Service> serviceChain;
    // 请求计数
    private static int count = 0;


    public Request(API api, double currentTime) {
        this.id = ++count;
        this.api = api;
        this.startTime = currentTime;
        this.status = Created;
    }


    public void addDelay(double delay){
        responseTime += delay;
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
        return api.chain;
    }

    public String getApiName(){
        return api.name;
    }
}
