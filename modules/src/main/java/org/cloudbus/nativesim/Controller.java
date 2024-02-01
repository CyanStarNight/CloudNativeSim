/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.nativesim.entity.*;
import org.cloudbus.nativesim.network.Communication;
import org.cloudbus.nativesim.network.EndPoint;
import org.cloudbus.nativesim.network.Request;
import org.cloudbus.nativesim.util.Status;

import javax.validation.constraints.AssertTrue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
public class Controller extends SimEntity {
    private int userId; // every user match only one controller
    private Calendar cal; //
    private Status status; //

    private static List<Request> localRequests =new ArrayList<>();

    private static ServiceGraph serviceGraph;
    private static List<EndPoint> localEndpoints =new ArrayList<>();
    private static List<Service> localServices=new ArrayList<>();
    private static List<Communication> localCommunications=new ArrayList<>();

    private static List<NativeCloudlet> localCloudlets=new ArrayList<>();
    private static List<Instance> localInstances=new ArrayList<>();
    private static List<Pod> localPods = new ArrayList<>();
    private static List<Container> localContainers=new ArrayList<>();

    public Controller(int userId, Calendar calendar){
        super("controller"+userId);
        this.userId = userId;
        this.cal = calendar;
        this.status = Status.Idle;
        NativeSim.controllers.add(this);
    }

    @Override
    public void startEntity() {

    }

    @Override
    public void processEvent(SimEvent simEvent) {

    }

    @Override
    public void shutdownEntity() {

    }


    public void initialize() {

    }

//    public <T> int index(T entity) {
//        int id;
//        Class<?> entityType = entity.getClass();
//        switch (entityType.getSimpleName()) {// 获取类名
//            case "Service" -> id = localServices.indexOf(entity);
//            case "Pod" -> id = localPods.indexOf(entity);
//            case "Communication" -> id = localCommunications.indexOf(entity);
//            case "Container" -> id = localContainers.indexOf(entity);
//            case "ServiceGraph" -> id = userId;
//            case "NativeCloudlet" -> id = localCloudlets.indexOf(entity);
//            case "Request" -> id = localRequests.indexOf(entity);
//            case "EndPoint" -> id = localEndpoints.indexOf(entity);
//            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType.getSimpleName());
//        }
//        return id;
//    }
//
//    public <T> void order(T entity) {
//        int id;
//        Class<?> entityType = entity.getClass();
//        switch (entityType.getSimpleName()) {// 获取类名
//            case "Service" -> id = localServices.size();
//            case "Pod" -> id = localPods.size();
//            case "Communication" -> id = localCommunications.size();
//            case "Container" -> id = localContainers.size();
//            case "ServiceGraph" -> id = userId;
//            case "NativeCloudlet" -> id = localCloudlets.size();
//            case "Request" -> id = localRequests.size();
//            case "EndPoint" -> id = localEndpoints.size();
//            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType.getSimpleName());
//        }
//        try {
//            Method method = entity.getClass().getMethod("setId", int.class);
//            method.invoke(entity, id);
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
//
//    }

    // 计算失败的请求数量
    public static int calculateFailedRequests(List<Request> requestList) {
        int failedRequests = 0;
        for (Request request : requestList) {
            if ("failed".equalsIgnoreCase(request.getStatus())) { // 假设失败的状态为 "failed"
                failedRequests++;
            }
        }
        return failedRequests;
    }

    // 计算平均响应时间
    public static double calculateAverageResponseTime(List<Request> requestList) {
        if (requestList.isEmpty()) {
            return 0;
        }

        long totalResponseTime = 0;
        for (Request request : requestList) {
            totalResponseTime += request.getResponseTime();
        }
        return (double) totalResponseTime / requestList.size();
    }

    // 计算SLO违规率
    public static double calculateSloViolationRate(List<Request> requestList) {
        // 假设有一个SLO响应时间阈值
        long sloThreshold = 1000; // 1秒，可以根据实际情况调整
        int violations = 0;

        for (Request request : requestList) {
            if (request.getResponseTime() > sloThreshold) {
                violations++;
            }
        }

        return (requestList.isEmpty()) ? 0 : ((double) violations / requestList.size()) * 100;
    }


}
