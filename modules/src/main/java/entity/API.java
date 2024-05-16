/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import core.Generator;
import lombok.Data;

import java.util.*;

@Data
public class API {
    public String name;
    // 类型
    public String method;
    // api
    public String url;
    // 权重初始为1
    public double weight = 1;
    // 服务调用链
    protected List<Service> chain = new ArrayList<>();

    // 请求
    protected List<Request> requests = new ArrayList<>();
    // 被请求数
    public int num;
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


    public API(String name) {
        this.name = name;
        Generator.APIs.add(this);
    }

    public API(String name, double weight) {
        this.name = name;
        this.weight = weight;
        Generator.APIs.add(this);
    }


    public API(String method, String url) {
        this.method = method;
        this.url = url;
        this.name = method+" "+url;
        Generator.APIs.add(this);
    }

    public API(String method, String url, double weight) {
        this.method = method;
        this.url = url;
        this.name = method+" "+url;
        this.weight = weight;
        Generator.APIs.add(this);
    }


    @Override
    public String toString() {
        return name;
    }

}


