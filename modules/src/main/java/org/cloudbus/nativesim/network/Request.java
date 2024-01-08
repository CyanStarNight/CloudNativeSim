/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.network;

import lombok.Data;

import java.util.Map;

@Data
public class Request {

    private int id;
    private String MethodType;
    private Map<String, String> parameters;
    private EndPoint endPoint;
    private String status;
    private long startTime;
    private long endTime;

    public Request(int id, String methodType, EndPoint endPoint) {
        this.id = id;
        this.MethodType = methodType;
        this.endPoint = endPoint;
        this.startTime = System.currentTimeMillis(); // 设置开始时间
    }

    // 记录请求结束的时间
    public void markEndTime() {
        this.endTime = System.currentTimeMillis();
    }

    // 计算请求的响应时间
    public long getResponseTime() {
        return endTime - startTime;
    }

    // 其他必要的 getters 和 setters...
}


