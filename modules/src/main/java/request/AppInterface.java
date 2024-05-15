/*
 * Copyright ©2024. Jingfeng Wu.
 */

package request;

import lombok.Data;
import service.Service;

import java.util.*;

@Data
public class AppInterface {
    // api
    public String API;
    // 类型
    public String type;
    // 权重
    public double weight;
    // api -> port
    public static Map<String, AppInterface> map = new HashMap<>();
    // 请求
    protected List<Request> requests = new ArrayList<>();
    // 被请求数
    public int num;
    // 服务调用链
    protected List<Service> chain = new ArrayList<>();
    // QPS
    public float qps;
    // Failures
    public float failedNum;
    // SLO
    public float slo;
    // SLO violations
    public float violations;
    // Average response time
    public float responseTime_avg;
    // 99% response time
    public float responseTime_99;
    // 95% response time
    public float responseTime_95;
    // 50% response time
    public float responseTime_50;
    // Min response time
    public float responseTime_min;
    // Median response time
    public float responseTime_median;
    // Max response time
    public float responseTime_max;


    public AppInterface(String API) {
        this.API = API;
    }

    public AppInterface(String API,double weight) {
        this.API = API;
        this.weight = weight;
    }

    public AppInterface(String API, int num, String type){
        this.API = API;
        this.num = num;
        this.type = type;
    }

    public static List<AppInterface> getPorts() {
        return map.values().stream().toList();
    }

    public AppInterface getPort(String name){
        return map.get(name);
    }

    @Override
    public String toString() {
        return "API #" + API;
    }

}


