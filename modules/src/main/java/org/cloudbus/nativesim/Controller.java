/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim;

import lombok.Data;
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

@Data
public class Controller {
    private int userId; // every user match only one controller
    private Calendar cal; //
    private Status status; //

    private ServiceGraph serviceGraph;
    private List<Request> localRequests =new ArrayList<>();
    private List<EndPoint> localEndpoints =new ArrayList<>();
    private List<Service> localServices=new ArrayList<>();
    private List<Pod> localPods=new ArrayList<>();
    private List<Communication> localCommunications=new ArrayList<>();
    private List<Container> localContainers=new ArrayList<>();
    private List<NativeCloudlet> localCloudlets=new ArrayList<>();

    public Controller(int userId, Calendar calendar){//TODO: 2023/12/7 the same user can't create two controllers
        this.userId = userId;
        this.cal = calendar;
        this.status = Status.Idle;
        NativeSim.controllers.add(this);
    }

    public <T> void submit(T entity) {
        try {
            // 获取实体类型
            Class<?> entityType = entity.getClass();
            // 添加到相应的集合中
            switch (entityType.getSimpleName()) {// 获取类名
                case "Service" -> getLocalServices().add((Service) entity);
                case "Pod" -> getLocalPods().add((Pod) entity);
                case "Container" -> getLocalContainers().add((Container) entity);
                case "ServiceGraph" -> setServiceGraph((ServiceGraph) entity);
                case "Communication" -> getLocalCommunications().add((Communication) entity);
                case "NativeCloudlet" -> getLocalCloudlets().add((NativeCloudlet) entity);
                case "Request" -> getLocalRequests().add((Request) entity);
                case "EndPoint" -> getLocalEndpoints().add((EndPoint) entity);
                default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType.getSimpleName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void submitAll(List<T> entities) {
        assert !entities.isEmpty();
        try {
            // 获取实体类型
            Class<?> entityType = entities.get(0).getClass();
            // 添加到相应的集合中
            switch (entityType.getSimpleName()) {// 获取类名
                case "Service" -> getLocalServices().addAll((List<Service>) entities);
                case "Pod" -> getLocalPods().addAll((List<Pod>) entities);
                case "Container" -> getLocalContainers().addAll((List<Container>) entities);
                case "Communication" -> getLocalCommunications().addAll((List<Communication>)entities);
                case "NativeCloudlet" -> getLocalCloudlets().addAll((List<NativeCloudlet>) entities);
                case "Request" -> getLocalRequests().addAll((List<Request>) entities);
                case "EndPoint" -> getLocalEndpoints().addAll((List<EndPoint>) entities);
                default -> throw new IllegalArgumentException("Unsupported entity type: List<" + entityType.getSimpleName() + ">");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize() {

    }

    public <T> int index(T entity) {
        int id;
        Class<?> entityType = entity.getClass();
        switch (entityType.getSimpleName()) {// 获取类名
            case "Service" -> id = localServices.indexOf(entity);
            case "Pod" -> id = localPods.indexOf(entity);
            case "Communication" -> id = localCommunications.indexOf(entity);
            case "Container" -> id = localContainers.indexOf(entity);
            case "ServiceGraph" -> id = userId;
            case "NativeCloudlet" -> id = localCloudlets.indexOf(entity);
            case "Request" -> id = localRequests.indexOf(entity);
            case "EndPoint" -> id = localEndpoints.indexOf(entity);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType.getSimpleName());
        }
        return id;
    }

    public <T> void order(T entity) {
        int id;
        Class<?> entityType = entity.getClass();
        switch (entityType.getSimpleName()) {// 获取类名
            case "Service" -> id = localServices.size();
            case "Pod" -> id = localPods.size();
            case "Communication" -> id = localCommunications.size();
            case "Container" -> id = localContainers.size();
            case "ServiceGraph" -> id = userId;
            case "NativeCloudlet" -> id = localCloudlets.size();
            case "Request" -> id = localRequests.size();
            case "EndPoint" -> id = localEndpoints.size();
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType.getSimpleName());
        }
        try {
            Method method = entity.getClass().getMethod("setId", int.class);
            method.invoke(entity, id);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    @AssertTrue
    public static boolean checkMapping(Service service, Pod pod){
        return service.getLabels().stream().anyMatch(u -> pod.getLabels().contains(u));
    }
    //TODO: 2023/12/7 check container mapping
//    @AssertTrue
//    public static boolean checkMapping(Service service,NativeContainer container){
//        return service.getLabels().stream().anyMatch(u -> container.getLabel().equals(u));
//    }
//    @AssertTrue
//    public static boolean checkMapping(Pod pod,NativeContainer container){
//        return pod.getLabels().stream().anyMatch(u -> u.contains(container.getLabel()));
//    }
    @AssertTrue
    public static boolean checkMapping(Service service,Communication communication){
        return (communication.getOrigin().equals(service)) || (communication.getDest().equals(service));
    }

    public Service selectServiceByUID(String uid){
        return localServices.stream().
                filter(u -> u.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }
    public Pod selectPodByUID(String uid){
        return localPods.stream().
                filter(u -> u.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }
    public Communication selectCommunicationByUID(String uid){
        return localCommunications.stream().
                filter(c -> c.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }
    public Container selectContainerByUID(String uid){
        return localContainers.stream().filter(c -> c.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }
    public NativeCloudlet selectCloudletByUID(String uid){
        return localCloudlets.stream().filter(c -> c.getUid().equals(uid))
                .findFirst()
                .orElse(null);
    }

    public List<Service> selectServicesByLabel(String label){
        return localServices.stream().
                filter(u -> u.getLabels().contains(label)).collect(Collectors.toList());
    }
    public List<Pod> selectPodsByLabel(String label){
        return localPods.stream().
                filter(p -> p.getLabels().contains(label)).collect(Collectors.toList());
    }
    //TODO: 2023/12/7 select Containers
//    public List<NativeContainer> selectContainersByLabel(String label){
//        return localContainers.stream().
//                filter(c -> c.getLabel().equals(label)).collect(Collectors.toList());
//    }
    public List<Pod> selectPodsByPrefix(String prefix){
        return localPods.stream().
                filter(p -> p.getPrefix().equals(prefix)).collect(Collectors.toList());
    }

    public Service selectServicesByName(String name){
        return localServices.stream().filter(u -> u.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

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

    // ... (其他代码)

}
