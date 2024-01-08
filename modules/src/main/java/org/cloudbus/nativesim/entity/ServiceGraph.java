package org.cloudbus.nativesim.entity;

import lombok.*;
import org.cloudbus.nativesim.Controller;
import org.cloudbus.nativesim.network.Communication;

import java.util.*;

/**
 * @author JingFeng Wu
 * @describe the service chains with orthogonalList and maintain the critical path.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServiceGraph extends NativeEntity { // Attention：maybe extends DataCenter

    Controller controller;
    private List<Service> services; // the entity of Communications.
    private List<Communication> communications;
    private int num_commu,num_services;

    private LinkedList<Communication> serviceChains;
    double totalChainCost;

    LinkedList<Communication> criticalPath;
    double totalCriticalCost;
    int num_critics;
    
    private int[][] serviceMatrix; // Attention: matrix may bring some benefits.

    public ServiceGraph(int userId,String appName, Controller controller){
        super(userId,appName);
        this.controller = controller;
    }

    public ServiceGraph(int userId) {
        super(userId);
    }

    public void buildNodeIn(Service service, Communication communication){
        if(service.getFirstIn()==null){
            service.setFirstIn(communication);
            return;
        }
        Communication tempCommunication = service.getFirstIn();
        while (tempCommunication.getHLink()!=null){
            tempCommunication = tempCommunication.getHLink();
        }
        tempCommunication.setHLink(communication);
    }

    public void buildNodeOut(Service service, Communication communication){
        if(service.getFirstOut()==null){
            service.setFirstOut(communication);
            return;
        }
        Communication tempCommunication = service.getFirstOut();
        while (tempCommunication.getTLink()!=null){
            tempCommunication = tempCommunication.getTLink();
        }
        tempCommunication.setTLink(communication);
    }

    public void buildDegree(){
        for(Service tmpService: services){
            tmpService.buildOut_degree();
            tmpService.buildIn_degree();
        }
    }
    public void buildCosts(){
        for (Communication communication : communications){
            communication.calculate_cost();
        }
    }

    public LinkedList<Communication> findCriticalPath() {
        List<String> originNames = new ArrayList<>();
        List<String> destNames = new ArrayList<>();

        // Forward pass to calculate etv
        services.stream().filter(service -> service.getInDegree() == 0.0).forEach(Service::buildEtv);

        double maxEtv = services.stream().mapToDouble(Service::getEtv).max().orElse(0.0);

        // Step 3: Initialize the latest times (ltv)
        services.forEach(v -> v.setLtv(maxEtv));

        // Step 4: Backward pass to calculate ltv
        services.stream().filter(v -> v.getOutDegree() == 0.0).forEach(Service::buildLtv);

        // Step 5: Calculate ete and lte for each Communication, and identify the critical path
        LinkedList<Communication> criticalPath = new LinkedList<>();
        double totalCriticalCost = 0.0;

        for (Communication e : communications) {
            double ete = e.getOrigin().getEtv();
            double lte = e.getDest().getLtv() - e.getCost();
//            System.out.println(ete + " " + lte);

            if (ete >= 0.0 && lte >= 0.0 && (ete - lte < 1e-9)
                    && !originNames.contains(e.getOriginName()) && !destNames.contains(e.getDestName())) {

                originNames.add(e.getOriginName());
                destNames.add(e.getDestName());
                criticalPath.add(e);
                totalCriticalCost += e.getCost();
            }
        }

        buildDegree();
        setCriticalPath(criticalPath);
        setTotalCriticalCost(totalCriticalCost);

        return criticalPath;
    }



    public void init(){//TODO: 2023/12/9 DAG算法等要预防输入不正当（缺失或存在环路）
        setServices(controller.getLocalServices());
        List<Communication> commus = controller.getLocalCommunications();
        setCommunications(commus);
        Service origin,dest;
        for(Communication c:commus){
            origin = controller.selectServicesByName(c.getOriginName());
            dest = controller.selectServicesByName(c.getDestName());
            c.setOrigin(origin);
            c.setDest(dest);
            buildNodeOut(origin,c);
            buildNodeIn(dest,c);
        }
        buildDegree();
        buildCosts();
        findCriticalPath();
    }

    public void setId(){
        super.setId(getUserId());
    }


}
