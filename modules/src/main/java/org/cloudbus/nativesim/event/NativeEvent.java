/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.event;

import lombok.Data;
import org.cloudbus.nativesim.NativeController;
import org.cloudbus.nativesim.NativeSim;
import org.cloudbus.nativesim.entity.*;
import org.cloudbus.nativesim.util.Status;

import java.lang.reflect.Method;
import java.util.List;

@Data
public class NativeEvent {
//Attention: Events will request and response to the controller.
    public String EventType;
    public int userId;
    NativeController controller;
    Status status; // the flag identify the status of the event
    double executeTime,startTime,endTime;
    double lifetime; // the lifetime of the event

    public NativeEvent(int userId){
        this.userId = userId;
        NativeSim.connectWithController(this,userId);
    }

    private void ready(){
        controller.response(this,status=Status.Ready);
    }
    private void start(){
        status = Status.Processing;
        startTime = NativeSim.clock();
    }
    private void pause(){
        executeTime = NativeSim.clock() - startTime;
        lifetime -= executeTime;
    }

    private void error(){ //TODO: 2023/12/11 执行出错？输入出错？等等
        controller.response(this,status=Status.Error);

    }
    private void end(){ //TODO: 2023/12/11
        lifetime = 0;
        controller.response(this,status=Status.END);
    }

    public void handleResponse(Status message, double clock){//TODO: 2023/12/11 处理session、改变status和执行事件方法

        if (message == Status.Denied) end();
        else if (message == Status.Received) start();
        else if (message == Status.Wait && status == Status.Ready) {
            while (NativeSim.clock() - clock >= 5.0) {// every 5 sec request
                ready();
            }
        }
        else if (message == Status.Wait && status == Status.Error) { //continue to prepare
            error();
        }
    }
    /**Unit: Submit*/
    //TODO: 2023/12/8 cloudlets已经提交给了broker，contro
    public <T> void submit(T entity) {
        try {
            // 获取实体类型
            Class<?> entityType = entity.getClass();
            // 添加到相应的集合中
            switch (entityType.getSimpleName()) {// 获取类名
                case "Service" -> controller.getLocalServices().add((Service) entity);
                case "Pod" -> controller.getLocalPods().add((Pod) entity);
                case "Communication" -> controller.getLocalCommunications().add((Communication) entity);
                case "NativeContainer" -> controller.getLocalContainers().add((Container) entity);
                case "ServiceGraph" -> controller.setServiceGraph((ServiceGraph) entity);
                case "NativeCloudlet" -> controller.getLocalCloudlets().add((NativeCloudlet) entity);
                default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType.getSimpleName());
            }
            // 调用 setId 方法
            Method setIdMethod = entityType.getDeclaredMethod("setId", int.class);
            setIdMethod.invoke(entity, controller.order(entity));
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常
        }
    }

    public void submitCloudlets(List<NativeCloudlet> cloudlets){
        controller.getLocalCloudlets().addAll(cloudlets);
        for (NativeCloudlet cloudlet : cloudlets)
            cloudlet.setId(controller.getLocalCloudlets().indexOf(cloudlet));
    }
//    public void submit(Service service){
//        localServices.add(service);
//        service.setId(order(service));
//    }
//    public void submit(Pod pod){
//        localPods.add(pod);
//        pod.setId(order(pod));
//    }
//    public void submit(Communication commu){
//        localCommunications.add(commu);
//        commu.setId(order(commu));
//    }
//    public void submit(NativeContainer container){
//        localContainers.add(container);
//        container.setId(order(container));
//    }
//    public void submit(ServiceGraph sg){
//        serviceGraph = sg;
//    }
//    public void submit(NativeCloudlet cloudlet){
//        localCloudlets.add(cloudlet);
//    }




}
