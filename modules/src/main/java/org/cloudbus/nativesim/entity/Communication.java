/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.*;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.nativesim.event.NativeEvent;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class Communication extends NativeEntity { //TODO: 2023/12/17 cloudlets与communication的映射关系也有问题，communications应该可以在路由和关键路径上起到更大作用（可以添加tag、gate、address等字段）。

    private String uid; // the global id
    private int userId; // the user id
    private int id;
    private long bw;
    private String tag; // tags may identify the definition of commu.

    String originName, destName;
    Service origin; // tailVec
    Service dest; // headVec

    List<? extends Cloudlet> data; // cloudlets used to be the data.
    double cost;

//    private long requestTime;
//    private long arrivalTime;
//    private long trans; // tans = arrivalTime - requestTime
//
//    private long responseTime;

    Communication hLink; // the edge which has the same head vertex.
    Communication tLink; // the edge which has the same tail vertex.
    double ete, lte; //ete: earliest time of edge, lte: latest time of edge.

    public Communication(int userId) {
        super(userId);
        tag = "abstract";
    }

    public Communication(int userId,String tag){ // create an abstract commu;
        super(userId);
        this.tag = tag;
    }
//TODO: 2023/12/17 calculate_cost方法计算逻辑不对，具体值 = 关键任务的执行时间。
// 应用的执行时间 = 模拟结束的事件 = 启动时间+关键任务执行时间+实体通信时间

    public double calculate_cost(){
        double cost = 0.0 ;

        for (Cloudlet d : data){

            cost += d.getCloudletLength();
        }
        this.cost = cost;
        return cost;
    }

    public String toString() {
        return originName +
                " -> " + destName +
                " (" + cost + ")";
    }
}
