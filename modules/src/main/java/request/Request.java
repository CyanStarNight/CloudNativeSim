/*
 * Copyright ©2024. Jingfeng Wu.
 */

package request;

import core.Status;
import lombok.Getter;
import lombok.Setter;
import service.Service;

import java.util.List;

@Getter
@Setter
public class Request {

    private int id;
    // 请求的api
    public String API;
    // 请求的接口
    public AppInterface port;
    // 请求的状态
    public Status status;
    // 请求的开始时间
    private double startTime;
    // 请求的响应时间
    private double responseTime;


    public Request(String API) {
        this.API = API;
        this.status = Status.Ready;
    }

    public Request(int i, String API, long currentTime) {
        this.id = i;
        this.API = API;
        this.startTime = currentTime;
    }


    public void addDelay(double delay){
        responseTime += delay;
    }


    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", API='" + API + '\'' +
                ", status=" + status +
                '}';
    }

    public List<Service> getChain() {
        return port.chain;
    }


}
