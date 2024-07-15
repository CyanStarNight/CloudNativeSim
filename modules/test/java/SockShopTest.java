/*
 * Copyright ©2024. Jingfeng Wu.
 */

import core.*;
import entity.API;
import entity.Service;
import entity.ServiceGraph;
import extend.NativeBroker;
import extend.NativeVm;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.junit.Test;
import policy.allocation.ServiceAllocationPolicySimple;
import policy.cloudletScheduler.NativeCloudletSchedulerBestEffort;
import policy.cloudletScheduler.NativeCloudletSchedulerSimple;
import policy.scaling.HorizontalScalingPolicy;
import policy.scaling.NoneScalingPolicy;
import policy.scaling.VerticalScalingPolicy;
import provisioner.NativePeProvisionerTimeShared;
import provisioner.NativeRamProvisionerSimple;
import provisioner.VmBwProvisionerSimple;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static org.cloudbus.cloudsim.Log.printLine;

/**
 * @Author JingFeng Wu
 * @Data 2023/11/20
 */
public class SockShopTest {
    // vm configuration
    private static List<NativeVm> vmList;
    private static int mips = 250; // MIPS
    private static String arch = "x86"; // system architecture, 标志着指令平均长度4B
    static String podsFile = "examples/src/sockshop/instances.yaml";
    static String servicesFile = "examples/src/sockshop/services.json";
    // generator configuration for requests and cloudlets
    static int finalClients = 300;
    static int spawnRate = 30;
    static int[] waitTimeSpan = new int[]{5, 15};
    static int rps = 50;
    static int timeLimit = 600;
    // 设定任务平均大小,下面两种表述是等价的:
    static int meanLength = 10; // 单位是百万条指令(M),任务规模 = 4*length
    static int stdDevLength = 5;


    @Test
    public void test() {
        printLine("Starting SockShopExample...");
        try {
            // initialized
            // data centers' users
            int num_user = 1;
            int userId = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudNativeSim.init(num_user, calendar, trace_flag);

            // create datacenters and brokers
            int numHosts = 1;
            Datacenter datacenter = createDatacenter("Datacenter#0",numHosts);
            NativeBroker broker = createBroker(userId);
            // get SimEntity ID
            int brokerId = broker.getId();
            vmList = createVms(3,brokerId);
            broker.submitVmList(vmList);
            // create application & define policies
            Application app = new Application("sockshop", brokerId, new ServiceAllocationPolicySimple());
            int schedulingInterval = 10;
            app.submitSchedulingInterval(schedulingInterval);
            // register
            Register register = new Register(userId,"Pod",servicesFile,podsFile);
            broker.submitRegister(register);
            // apis
            List<API> apis = register.registerAPIs();
            app.submitAPIs(apis);
            // services
            ServiceGraph graph = register.registerServiceGraph();
            app.submitServiceGraph(graph);
            List<Service> services = graph.getAllServices();
            // set policy
            for (Service service : services){
                service.setCloudletScheduler(new NativeCloudletSchedulerSimple());
                service.setServiceScalingPolicy(new HorizontalScalingPolicy());
            }
            app.submitServiceList(services);
            // generator by rps
//            Generator generator = new Generator(apis,rps, timeLimit, meanLength,stdDevLength);
            Generator generator = new Generator(apis,finalClients, spawnRate, waitTimeSpan, timeLimit,meanLength,stdDevLength);
            app.submitGenerator(generator);
            // instance
            app.submitInstanceList(register.registerAllInstances());
            CloudNativeSim.startSimulation();

            // x: End the simulation
            CloudNativeSim.stopSimulation();

            // report
//            apis.forEach(Reporter::printApiStatistics);
            Reporter.printApiStatistics(apis);
//            writeStatisticsToCsv(apis,outputPath);
            Reporter.printResourceUsage(true,"modules/test/resource/");

            Reporter.printPhase("SockShopExample finished!");

        } catch (Exception e) {
            e.printStackTrace();
            printLine("Errors happen.");
        }

    }


    private static List<NativeVm> createVms(int num, int brokerId){
        List<NativeVm> vms = new ArrayList<NativeVm>();
        //VM description
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
