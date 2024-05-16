/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import core.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Request {

    private int id;
    // 请求的描述
    public String method;
    public String url;
    // 请求的接口
    public API api;
    // 请求的状态
    public Status status;
    // 请求的开始时间
    private double startTime;
    // 请求的响应时间
    private double responseTime;
    // 请求计数
    private static int count = 0;


    public Request(API api, double currentTime) {
        this.id = ++count;
        this.api = api;
        this.startTime = currentTime;
    }


    public void addDelay(double delay){
        responseTime += delay;
    }


    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", API='" + method + '\'' +
                ", status=" + status +
                '}';
    }

    public List<Service> getChain() {
        return api.chain;
    }

    public String getAPI(){
        return method+" "+url;
    }
}
