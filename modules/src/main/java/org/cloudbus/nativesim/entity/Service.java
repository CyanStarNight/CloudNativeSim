package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.cloudbus.nativesim.network.Communication;
import org.cloudbus.nativesim.network.EndPoint;
import org.cloudbus.nativesim.util.Status;

import static org.cloudbus.cloudsim.Log.printLine;
import static org.cloudbus.nativesim.Controller.checkMapping;


/**
 * @author JingFeng Wu
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class Service extends NativeEntity {

    private ArrayList<String> labels;

    List<Pod> pods;
    List<Communication> calls;
    List<EndPoint> endPoints;
    List<NativeVm> vmList;
    ServiceGraph serviceGraph;

    Communication firstIn;
    Communication firstOut;
    int inDegree, outDegree;
    double etv,ltv;

    Status status = Status.Ready;

    private int num_pods;

    public Service(){
        super();
    }
    public Service(int userId,String name){
        super(userId,name);
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
            while (commu.getHLink() != null) {
                commu = commu.getHLink();
                inDegree++;
            }
        }
    }

    public void buildOut_degree() {
        outDegree = 0;
        if (this.firstOut != null) {
            Communication commu = this.firstOut;
            this.outDegree++;
            while (commu.getTLink() != null) {
                commu = commu.getTLink();
                this.outDegree++;
            }
        }
    }
    void buildEtv() {
        etv = 0.0;
        for (Communication e = firstOut; e != null; e = e.getTLink()) {
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
        for (Communication e = firstIn; e != null; e = e.getHLink()) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\nout-degree: "+ getOutDegree());
        Communication firstOut = getFirstOut();
        while (firstOut!=null){
            sb.append("\n    ").
                    append(firstOut.getOrigin().getName()).
                    append("-->").
                    append(firstOut.getDest().getName()).
                    append(" (cost=").append(String.format("%.3f", firstOut.getCost())).append(")");
            firstOut = firstOut.getTLink();
        }

        return super.toString()
                + "\nstatus: "+status
                + "\nlabels: "+labels
                + "\npods: " +num_pods +sb;
    }
}
