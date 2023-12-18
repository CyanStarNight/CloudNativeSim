package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import org.cloudbus.nativesim.util.Status;

import static org.cloudbus.nativesim.NativeController.checkMapping;

/**
 * @author JingFeng Wu
 */
@Data
public class Service {

    public String name;
    private int id;
    private int userId;
    private String uid;
    private ArrayList<String> labels;

    List<Pod> pods;
    List<Communication> calls;
    ServiceGraph serviceGraph;

    Communication firstIn;
    Communication firstOut;
    int inDegree, outDegree;
    double etv,ltv;

    Status status = Status.Ready;
    public String type;

    private int num_pods = 1;

    public Service(){
        uid = UUID.randomUUID().toString();
    }
    public Service(String name){
        uid = UUID.randomUUID().toString();
        this.name = name;
    }

    public void init(){
        num_pods = pods.size();
        buildIn_degree();
        buildOut_degree();
        buildEtv();
        buildLtv();
    }

    //Unit： DAG相关
    public void buildIn_degree() {
        inDegree = 0;
        if (this.firstIn != null) {
            Communication commu = this.firstIn;
            inDegree++;
            while (commu.hLink != null) {
                commu = commu.hLink;
                inDegree++;
            }
        }
    }

    public void buildOut_degree() {
        outDegree = 0;
        if (this.firstOut != null) {
            Communication commu = this.firstOut;
            this.outDegree++;
            while (commu.tLink != null) {
                commu = commu.tLink;
                this.outDegree++;
            }
        }
    }
    void buildEtv() {
        etv = 0.0;
        for (Communication e = firstOut; e != null; e = e.tLink) {
            Service dest = e.getDest();
            int inDegree = dest.getInDegree();
            dest.setEtv(Math.max(dest.getEtv(), etv + e.getCost()));
            if (--inDegree == 0) {
                dest.buildEtv();
            }
        }
    }

    void buildLtv() {
        ltv = Double.MAX_VALUE;
        for (Communication e = firstIn; e != null; e = e.hLink) {
            Service origin = e.getOrigin();
            int outDegree = origin.getOutDegree();
            origin.setLtv(Math.min(origin.getLtv(), ltv - e.getCost()));
            if (--outDegree == 0) {
                origin.buildLtv();
            }
        }
    }
    // Unit: Pod相关
    // 最基础的操作有setPods、addPod和addPods.

    public void addPod(Pod pod){
        if (checkMapping(this,pod))
            this.getPods().add(pod);
    }


    // Unit: Commu相关

    public void addCommunication(Communication communication){
        if (checkMapping(this,communication))
            this.getCalls().add(communication);
    }


}
