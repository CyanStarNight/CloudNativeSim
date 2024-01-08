/*
 * Copyright ©2023. Jingfeng Wu.
 */
package org.cloudbus.nativesim;

import org.cloudbus.cloudsim.*;
import org.cloudbus.nativesim.entity.*;
import org.cloudbus.nativesim.policy.allocation.ServiceAllocationPolicy;
import org.cloudbus.nativesim.policy.scaling.ServiceScalingPolicy;
import org.cloudbus.nativesim.provisioner.NativeBwProvisionerSimple;
import org.cloudbus.nativesim.provisioner.NativePeProvisionerSimple;
import org.cloudbus.nativesim.provisioner.NativeRamProvisionerSimple;
import org.cloudbus.nativesim.network.Request;
import org.cloudbus.nativesim.scheduler.NativeCloudletSchedulerTimeShared;
import org.cloudbus.nativesim.util.NativeLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author JingFeng Wu
 * @Data 2023/11/20
 */
public class SockShopExample{
    private static Controller controller;

    private static String deploymentFile = "examples/src/main/resource/sockshop/deployment.yaml";
    private static List<NativeVm> vmList;
    private static List<NativeCloudlet> cloudletList;
    private static List<Pod> podList;

    private static String dependencyFile = "examples/src/main/resource/sockshop/dependency.json";
    private static ServiceGraph serviceGraph;

    private static String requestsFile = "examples/src/main/resource/sockshop/requests.json";
    private static List<Request> requests;



    public static void main(String[] args) {
        NativeLog.printLine("Starting SockShopExample...");
        try {
            // 1: Create and register the entities and match them.
            int num_user = 1;
            int userId = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            NativeSim.init(num_user, calendar, trace_flag);
            controller = new Controller(userId, calendar);

            // 1.1: Create some machines to act as a datacenter.
            NativeDatacenter datacenter = createDatacenter("sockshop-DataCenter");
            DatacenterBroker broker = createBroker(userId);
            int brokerId = broker.getId(); // brokerId = userId
            vmList = createVms(2,brokerId);

            // 1.2: Create the service chains
            Register register = new Register(userId,controller);
            podList = register.registerDeployment(deploymentFile);
            serviceGraph = register.registerDependencies("sockshop",dependencyFile);
            requests = register.registerRequests(requestsFile);

            controller.initialize();

            cloudletList = controller.getLocalCloudlets();
            cloudletList.forEach(c -> c.setUserId(brokerId));

            // 2: Init the parameters and policies,and then submit to the brokers.
            // 2.1 Submit to the brokers
            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList); //CloudletList.subList(0, num_cloudlets)

            // 2.2 init the policies
            ServiceAllocationPolicy placementPolicy;
            ServiceScalingPolicy scalingPolicy;

            // 3: Start the simulation.
            NativeSim.startSimulation();

            // 4: Pause
            // 5: Update the status
            // 6. Print the logs
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            double total_time = NativeSim.clock();
            NativeLog.printCloudletList(newList);
            NativeLog.printRequestStatistics(requests,total_time);
            NativeLog.printServiceGraphDetails(serviceGraph);

            // x: End the simulation
            NativeSim.stopSimulation();
            NativeLog.printLine("SockShopExample finished!");

        } catch (Exception e) {
            e.printStackTrace();
            NativeLog.printLine("Errors happen.");
        }

    }

    private static List<NativeVm> createVms(int num, int brokerId){
        List<NativeVm> vms = new ArrayList<NativeVm>();
        //VM description
        double mips = 250;
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create two VMs: the first one belongs to user1
        for (int i = 0; i < num; i++) {
            vms.add(new NativeVm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new NativeCloudletSchedulerTimeShared()));
        }
        return vms;
    }

    private static NativeDatacenter createDatacenter(String name) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store our machine.
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<NativePe> peList = new ArrayList<NativePe>();

        double mips=1000;

        // 3. Create PEs and add these into a list.
        peList.add(new NativePe(0, new NativePeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        peList.add(new NativePe(1, new NativePeProvisionerSimple(mips)));
        //4. Create Host with its id and list of PEs and add them to the list of machines
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;


        //in this example, the VMAllocatonPolicy in use is SpaceShared. It means that only one VM
        //is allowed to run on each Pe. As each Host has only one Pe, only one VM can run on each Host.
        hostList.add(
                new Host(
                        0,
                        new NativeRamProvisionerSimple(ram) {
                        },
                        new NativeBwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerSpaceShared(peList)
                )
        );
        hostList.add(
                new Host(
                        1,
                        new NativeRamProvisionerSimple(ram),
                        new NativeBwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerSpaceShared(peList)
                )
        );


        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        NativeDatacenter datacenter = null;
        try {
            datacenter = new NativeDatacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker(int id){

        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker"+id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }


}
