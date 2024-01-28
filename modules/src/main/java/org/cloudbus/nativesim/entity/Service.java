package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.*;
import org.cloudbus.nativesim.network.Communication;
import org.cloudbus.nativesim.network.EndPoint;
import org.cloudbus.nativesim.scheduler.NativeCloudletScheduler;
import org.cloudbus.nativesim.util.NativeLog;
import org.cloudbus.nativesim.util.NativeStateHistoryEntry;
import org.cloudbus.nativesim.util.Status;

import static org.cloudbus.nativesim.Controller.checkMapping;


/**
 * @author JingFeng Wu
 */
@EqualsAndHashCode
@Data
@AllArgsConstructor
public class Service{
    private String uid; // the global id
    private int id;
    private int userId;
    public String name;
    private ArrayList<String> labels;

    private List<Instance> instanceList;
    private List<Pod> podList;
    private List<Container> containerList;
    private List<Communication> calls;
    private List<EndPoint> endPoints;
    private List<NativeVm> vmList;
    private int num_instance,num_pods,num_containers,num_calls,num_vms,num_endpoints;
    private ServiceGraph serviceGraph;
    private NativeDatacenter datacenter;

    private Communication firstIn;
    private Communication firstOut;
    private int inDegree, outDegree;
    private double etv,ltv;

    public Status status = Status.Ready;

    private double mips;
    private int numberOfPes;
    private int ram;
    private long bw;

    private NativeCloudletScheduler cloudletScheduler;

    private boolean inMigration;
    private boolean beingInstantiated;

    private final List<NativeStateHistoryEntry> stateHistory = new LinkedList<NativeStateHistoryEntry>();

    public void setUid() {
        uid = userId + "-Service-" + id;
    }

    public static String getUid(int userId, int id) {
        return userId + "-Service-" + id;
    }

    public Service(int userId) {
        setUid();
        setUserId(userId);
    }

    public Service(int userId,String name){
        setUid();
        setUserId(userId);
        this.name = name;
    }

    public void build(){
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

    public boolean matchInstance(Instance instance){
        return checkMapping(this,instance);
    }
    public boolean addInstance(Instance instance){
        if (instanceList.contains(instance)){ return true; }

        String class_name = instance.getClass().getSimpleName();
        switch (class_name){
            case "Pod":
                assert instance instanceof Pod;
                podList.add((Pod) instance);
                num_pods++;
            case "Container":
                assert instance instanceof Container;
                containerList.add((Container) instance); num_containers++;
            default: break;
        }

        instanceList.add(instance);
        instance.setId(instanceList.indexOf(instance));
        num_instance++;

        return true;
    }
//TODO: 2024/1/24 除了删除映射，还需要删除资源
    public boolean removeInstance(Instance instance){
        if (!instanceList.contains(instance)){
            NativeLog.printLine("Failed to remove: Instance not exist.");
            return false;
        }else {
            instanceList.remove(instance);
            num_instance--;
            String class_name = instance.getClass().getSimpleName();
            switch (class_name){
                case "Pod": num_pods--;
                case "Container": num_containers--;
                default: break;
            }
        }
        return true;
    }

    public boolean matchCommunication(Communication communication){
        return checkMapping(this,communication);
    }

    public boolean addCommunication(Communication communication){
        if (getCalls().contains(communication)){ return true; }

        this.getCalls().add(communication);
        communication.setId(getCalls().indexOf(communication));
        num_calls++;

        return true;
    }

    public boolean removeCommunication(Communication communication){
        if (!getCalls().contains(communication)){
            NativeLog.printLine("Failed to remove: communication not exist.");
            return false;
        }else {
            getCalls().remove(communication);
            num_calls--;
            build();
        }
        return true;
    }

    public double getTotalUtilizationOfCpu(double time) {
        return getCloudletScheduler().getTotalUtilizationOfCpu(time);
    }

    public double getTotalUtilizationOfCpuMips(double time) {
        return getTotalUtilizationOfCpu(time) * getMips();
    }

    public void addStateHistoryEntry(
            double time,
            double allocatedMips,
            double requestedMips,
            boolean isInMigration) {
        NativeStateHistoryEntry newState = new NativeStateHistoryEntry(
                time,
                allocatedMips,
                requestedMips,
                isInMigration);
        if (!getStateHistory().isEmpty()) {
            NativeStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
        getStateHistory().add(newState);
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
