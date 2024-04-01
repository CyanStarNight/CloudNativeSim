/*
 * Copyright ©2024. Jingfeng Wu.
 */
package org.cloudbus.nativesim;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.nativesim.core.*;
import org.cloudbus.nativesim.extend.NativeBroker;
import org.cloudbus.nativesim.core.NativeSim;
import org.cloudbus.nativesim.extend.NativeVm;
import org.cloudbus.nativesim.policy.allocation.ServiceAllocationPolicySimple;
import org.cloudbus.nativesim.policy.migration.InstanceMigrationPolicySimple;
import org.cloudbus.nativesim.policy.scaling.HorizontalScalingPolicy;
import org.cloudbus.nativesim.provisioner.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static org.cloudbus.cloudsim.Log.printLine;

/**
 * @Author JingFeng Wu
 * @Data 2023/11/20
 */
public class SockShopExample{

    private static List<NativeVm> vmList;
    private static final int slo = 1;
    private static final int meanLength = 100000; // 平均 Cloudlet 长度, 可根据实际情况调整
    private static final int stdDev = 2000; // 标准差
    static String podsFile = "examples/src/org/resource/sockshop/instances.yaml";
    static String servicesFile = "examples/src/org/resource/sockshop/services.json";
    static String requestsFile = "examples/src/org/resource/sockshop/requests.json";

    public static void main(String[] args) {
        printLine("Starting SockShopExample...");
        try {
            // 1: initialized
            int num_user = 1;
            int userId = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            NativeSim.init(num_user, calendar, trace_flag);

            // create
            int numHosts = 1;
            Datacenter datacenter = createDatacenter("Datacenter#0",numHosts);
            NativeBroker broker = createBroker(userId);
            int brokerId = broker.getId(); // SimEntity ID
            Application app = new Application("sockshop", brokerId,
                    new ServiceAllocationPolicySimple(),
                    new InstanceMigrationPolicySimple(),
                    new HorizontalScalingPolicy()); // horizontalScaling

            app.submitSLA(slo,meanLength,stdDev);
            // 2: submit
            String instanceType = "Pod";
            Register register = new Register(userId,instanceType,servicesFile,podsFile,requestsFile);
            broker.submitRegister(register);

            vmList = createVms(3,brokerId);
            broker.submitVmList(vmList);

            // 3: Start
            NativeSim.startSimulation();

            // 4: Pause

            // x: End the simulation
            NativeSim.stopSimulation();

            Reporter.printServiceGraph();
            Reporter.printRequestStatistics();
            Reporter.printResourceUsage();

            Reporter.printPhase("SockShopExample finished!");

        } catch (Exception e) {
            e.printStackTrace();
            printLine("Errors happen.");
        }

    }


    private static List<NativeVm> createVms(int num, int brokerId){
        List<NativeVm> vms = new ArrayList<NativeVm>();
        //VM description
        int mips = 250;
        long size = 10000; //image size (MB)
        int ram = 4096; //vm memory (MB)
        long bw = 10000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create two VMs: the first one belongs to user1
        for (int i = 0; i < num; i++) {
            vms.add(new NativeVm(i, brokerId, mips, pesNumber, ram, bw, size, vmm,
                    new NativePeProvisionerTimeShared(),
                    new NativeRamProvisionerSimple(),
                    new VmBwProvisionerSimple()));
        }
        return vms;
    }

    private static List<Host> createHosts(int numHosts, int ram, long storage, int bw, double mips) {
        List<Host> hostList = new ArrayList<>();

        // Create PEs and add them to the list
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < mips; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        // Create hosts with their ids and list of PEs and add them to the list of machines
        for (int i = 0; i < numHosts; i++) {
            hostList.add(new Host(
                    i,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerSpaceShared(peList)
            ));
        }

        return hostList;
    }

    private static Datacenter createDatacenter(String name, int numHosts) {
        List<Host> hostList = createHosts(numHosts, 16224, 1000000, 30000, 1000);

        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        // Finally, create a PowerDatacenter object
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics,
                    new VmAllocationPolicySimple(hostList),
                    storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }


    private static NativeBroker createBroker(int id){

        NativeBroker broker = null;
        try {
            broker = new NativeBroker("Broker"+id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

}
