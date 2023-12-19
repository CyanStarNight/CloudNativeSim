package org.cloudbus.nativesim.entity;

import lombok.*;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.nativesim.NativeController;

import java.util.*;

/**
 * @author JingFeng Wu
 * @describe the service chains with orthogonalList and maintain the critical path.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServiceGraph extends NativeEntity {//Attention：maybe extends DataCenter

    private List<Service> services; // the entity of Communications.
    private List<Communication> communications;
    private int num_commu,num_services;

    LinkedList<Communication> CriticalPath;
    double totalCriticalCost;
    int num_critics;
    
    private int[][] serviceMatrix; //Attention: matrix may bring some benefits.

    public ServiceGraph(int userId,String appName){
        super(userId,appName);
    }

/**Unit: Commons*/

//    public void buildCommunications(List<Communication> communications) {
//        this.communications = communications;
//        buildDegree();
//        setCriticalPath(findCriticalPath());
//    }

    public void buildDAGFromMatrix(int[][] matrix, String[] names) throws Exception {
        this.serviceMatrix = matrix;
        services = new ArrayList<>();
        communications = new ArrayList<>();
        // Initialize the service nodes list by the number of rows of the matrix.
        for(int i=0;i<matrix.length;i++){
            services.add(new Service(getUserId(),names[i]));
        }
        // Traverse the matrix and generate the orthogonal list.
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[0].length;j++){
                if(matrix[i][j]!=0){
                    // if there has a communication, add it to the orthogonal list.
                    Communication communication = new Communication(getUserId());
                    if (matrix[i][j]!=0) {
                        communication.setCost(matrix[i][j]);
                    }
                    communication.setOrigin(services.get(i));
                    communication.setDest(services.get(j));
                    // i build in-degree
                    buildNodeOut(services.get(i), communication);
                    // j build out-degree
                    buildNodeIn(services.get(j), communication);
                    communications.add(communication);
                }
            }
        }
        buildDegree();
        num_services = services.toArray().length;
        num_commu = communications.toArray().length;
    }
    public void buildNodeIn(Service service, Communication communication){
        if(service.getFirstIn()==null){
            service.setFirstIn(communication);
            return;
        }
        Communication tempCommunication = service.getFirstIn();
        while (tempCommunication.hLink!=null){
            tempCommunication = tempCommunication.hLink;
        }
        tempCommunication.hLink = communication;
    }

    public void buildNodeOut(Service service, Communication communication){
        if(service.getFirstOut()==null){
            service.setFirstOut(communication);
            return;
        }
        Communication tempCommunication = service.getFirstOut();
        while (tempCommunication.tLink!=null){
            tempCommunication = tempCommunication.tLink;
        }
        tempCommunication.tLink = communication;
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

    /**
     * print the total in-degree and out-degree.
     */
    public void printAllNodeIn(){
        System.out.println("In-Chain: ");
        for(Service service : services){
            System.out.println("    Service: "+ service.getName());
            System.out.println("    in-degree: "+ service.getInDegree());
            Communication firstIn = service.getFirstIn();
            while (firstIn!=null){
                System.out.println("    " + firstIn.getOrigin().getName()+"-->"+firstIn.getDest().getName()+" (cost="+firstIn.getCost()+")");
                firstIn = firstIn.hLink;

            }
            System.out.println();
        }
    }

    public void printAllNodeOut(){
        System.out.println("Out-Chain:");
        for(Service service : services){
            System.out.println("    Service: "+ service.getName());
            System.out.println("    out-degree: "+ service.getOutDegree());
            Communication firstOut = service.getFirstOut();
            while (firstOut!=null){
                System.out.println("    " + firstOut.getOrigin().getName()+"-->"+firstOut.getDest().getName()+" (cost="+firstOut.getCost()+")");
                firstOut = firstOut.tLink;
            }
            System.out.println();
        }
    }

    public void printAllCommunications(){
        System.out.println("Communications:"+ communications.toArray().length);
        for (Communication Communication:this.communications){
            System.out.println(" "+Communication.toString());
        }
    }

    public void printCriticalPath() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Critical Path ==========\n");
//        sb.append(CriticalPath.getFirst().getOriginName());
//        for (Communication Communication : CriticalPath) {
//            sb.append(" -> ").append(Communication.getDestName());
//        }
//        sb.append("\nCritical Communications:\n");
        for (Communication communication : CriticalPath) {
            sb.append(communication).append("\n");
        }
        sb.append("Total Critical Cost: ").append(totalCriticalCost);
        System.out.println(sb.toString());
    }

    public void init(NativeController controller){//TODO: 2023/12/9 DAG算法等要预防输入不正当（缺失或存在环路）
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

/**Unit: Log*/
    public void printServiceChain() {
        printAllNodeOut();
//        printCriticalPath();
    }

///**Unit: SimEntity*/
//    @Override
//    public void startEntity() {
//
//    }
//
//    @Override
//    public void processEvent(SimEvent simEvent) {
//
//    }
//
//    @Override
//    public void shutdownEntity() {
//
//    }
}
