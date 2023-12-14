/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.*;
import org.cloudbus.cloudsim.Cloudlet;

import java.util.List;
import java.util.UUID;

@Data
public class Communication { // 微服务架构内的抽象通信过程，类似于网络传输

    private String uid; // the global id
    private int userId; // the user id
    private int id;
    private int bw;
    private String tag; // tags may identify the definition of commu.

    String originName, destName;
    Service origin; // tailVec
    Service dest; // headVec

    List<? extends Cloudlet> data; // cloudlets used to be the data.
    double cost; //TODO: 2023/12/4 cost目前为cloudlets的长度总和或者延迟总和

//    private long requestTime;
//    private long arrivalTime;
//    private long trans; // tans = arrivalTime - requestTime
//
//    private long responseTime;

    Communication hLink; // the edge which has the same head vertex.
    Communication tLink; // the edge which has the same tail vertex.
    double ete, lte; //ete: earliest time of edge, lte: latest time of edge.

    //TODO: 2023/12/4 定义tag字典，检查输入的tag被字典包含
    public Communication(String tag){ // create an abstract commu;
        uid = UUID.randomUUID().toString();
        this.tag = tag;
    }
    public Communication(){
        uid =  UUID.randomUUID().toString();
        tag = "abstract";
    }

    public double calculate_cost(){
        double cost = 0.0 ;
        for (Cloudlet d : data){
//            cost += d.getFinishTime() - d.getSubmissionTime();
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
