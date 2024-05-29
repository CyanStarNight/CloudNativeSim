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
    // 请求的开始时间节点
    private double startTime;
    // 延迟
    private double delay;
    // 请求的响应时间节点, 默认-1代表没完成
    private double responseTimeStamp = -1;
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
        this.delay += delay;
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
