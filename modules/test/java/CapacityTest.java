/*
 * Copyright ©2024. Jingfeng Wu.
 */

import core.*;
import entity.API;
import entity.Instance;
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
import policy.cloudletScheduler.NativeCloudletSchedulerDynamicWorkload;
import policy.migration.InstanceMigrationPolicySimple;
import policy.scaling.HorizontalScalingPolicy;
import provisioner.NativePeProvisionerTimeShared;
import provisioner.NativeRamProvisionerSimple;
import provisioner.VmBwProvisionerSimple;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static org.cloudbus.cloudsim.Log.printLine;

public class CapacityTest {
    private static List<NativeVm> vmList;
    private static int mips = 250; // MIPS
    private static String arch = "x86"; // system architecture, 标志着指令平均长度4B
    static String podsFile = "modules/test/config/capacityTest.yaml";
    static String servicesFile = "modules/test/config/capacityTest.json";
    static String outputPath = "modules/test/resource/";
    // generator configuration for requests and cloudlets
    static int finalClients = 10;
    static int requestCount = 1000;
    static int spawnRate = 100;
    static int[] waitTimeSpan = new int[]{3, 10};

    // 设定任务平均大小,下面两种表述是等价的:
    static int meanLength = 100; // 单位是百万条指令(M)
    static int stdDevLength = 20;
    static int timeLimit = 1000000;

    private static String csvFilePath = "modules/test/resource/capacityTests.csv";



    @Test
    public void test() {
        printLine("Starting CapacityTests...");
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
            Application app = new Application("capacity", brokerId,
                    new ServiceAllocationPolicySimple(),
                    new InstanceMigrationPolicySimple(),
                    new HorizontalScalingPolicy());
            // register
            Register register = new Register(userId,"Pod",servicesFile,podsFile);
            broker.submitRegister(register);
            // apis
            List<API> apis = register.registerAPIs();
            app.submitAPIs(apis);
            // services

            int serviceCount = 50000;
            ServiceGraph graph = register.registerServiceGraphTest(serviceCount);
            app.submitServiceGraph(graph);
            List<Service> services = graph.getAllServices();

            app.submitServiceList(services);



            // generator
            Generator generator = new Generator(apis,finalClients, spawnRate, waitTimeSpan, timeLimit,meanLength,stdDevLength);
            generator.setNumLimit(requestCount);
            app.submitGenerator(generator);

            // instance
            List<Instance> instances = register.registerAllInstances();
            app.submitInstanceList(instances);


            // cloudlet scheduler
            services.forEach(service -> service.setCloudletScheduler(new NativeCloudletSchedulerDynamicWorkload()));

            CloudNativeSim.startSimulation();

            // x: End the simulation
            CloudNativeSim.stopSimulation();

            // test 1:
            int instanceCount = instances.size(); // 调整yaml文件中的replica数量
            serviceCount = services.size();
            requestCount = app.getRequestList().size();
            appendToCsv(serviceCount, (double) instanceCount /serviceCount, instanceCount, requestCount, requestCount*serviceCount);

            Reporter.printPhase("SockShopExample finished!");

        } catch (Exception e) {
            e.printStackTrace();
            printLine("Errors happen.");
        }

    }

    private static long startTime = System.nanoTime();  // 记录开始时间

    public static void appendToCsv(int serviceCount, double ips, int instanceCount, int requestCount, int cloudletsCount) {
        // 创建File对象
        File file = new File(csvFilePath);
        boolean isNewFile = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                writer.write("Service Count,IPS,Instance Count,Request Count,Cloudlets Count,Elapsed Time (seconds),Max Memory (MB)\n"); // 如果是新文件则写入头部信息
            }
            // 计算运行时间（秒）
            long endTime = System.nanoTime();
            double elapsedTime = (endTime - startTime) / 1.0e9;  // 转换为秒

            // 获取运行时最大内存使用量
            long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024; // 转换为兆字节

            // 构造要写入的数据行
            String line = String.format("%d,%.2f,%d,%d,%d,%.3f,%d\n",
                    serviceCount, ips, instanceCount, requestCount, cloudletsCount, elapsedTime, maxMemory);
            writer.write(line); // 写入测试数据
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
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
