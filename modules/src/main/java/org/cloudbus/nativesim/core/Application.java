/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.core;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.nativesim.extend.NativeCloudlet;
import org.cloudbus.nativesim.extend.NativeSimTag;
import org.cloudbus.nativesim.extend.NativeVm;
import org.cloudbus.nativesim.policy.allocation.ServiceAllocationPolicy;
import org.cloudbus.nativesim.policy.migration.InstanceMigrationPolicy;
import org.cloudbus.nativesim.policy.scaling.ServiceScalingPolicy;
import org.cloudbus.nativesim.request.Request;
import org.cloudbus.nativesim.request.RequestType;
import org.cloudbus.nativesim.service.Instance;
import org.cloudbus.nativesim.service.Service;
import org.cloudbus.nativesim.service.ServiceGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.cloudbus.cloudsim.Log.printLine;
import static org.cloudbus.nativesim.core.Exporter.calculateRequestStatistics;
import static org.cloudbus.nativesim.core.Reporter.printEvent;

@Setter @Getter
public class Application extends SimEntity {

    private int brokerId;
    /** The regional cis name. */
    private String regionalCisName;
    /** The last process time. */
    private double lastProcessTime;
    /** The scheduling interval. */
    private double schedulingInterval;
    /** The checking interval. */
    private double checkingInterval; //TODO: 考虑做死循环检查
    /** The service level object for response. */
    private int slo; //TODO: slo可以丰富一下

    /** The service graph. */
    protected ServiceGraph serviceGraph;
    /** The requests list. */
    protected List<Request> requestList = new ArrayList<>();

    protected List<RequestType> requestTypes = new ArrayList<>();
    /** The services list. */
    protected List<Service> serviceList = new ArrayList<>();
    /** The instances list. */
    protected List<Instance> instanceList = new ArrayList<>();
    /** The vm list. */
    protected List<NativeVm> vmList = new ArrayList<>();
    /** The cloudlet list. */
    protected List<NativeCloudlet> cloudletList = new ArrayList<>();

    /** The allocation policy. */
    private ServiceAllocationPolicy serviceAllocationPolicy;
    /** The migration policy. */
    private InstanceMigrationPolicy instanceMigrationPolicy;
    /** The scaling policy. */
    private ServiceScalingPolicy serviceScalingPolicy;


    public Application(String appName, int brokerId,
                       ServiceAllocationPolicy serviceAllocationPolicy,
                       InstanceMigrationPolicy instanceMigrationPolicy,
                       ServiceScalingPolicy serviceScalingPolicy) {
        super(appName);
        setBrokerId(brokerId);
        setServiceAllocationPolicy(serviceAllocationPolicy);
        setInstanceMigrationPolicy(instanceMigrationPolicy);
        setServiceScalingPolicy(serviceScalingPolicy);
    }



    @SuppressWarnings("unchecked")
    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {

            case NativeSimTag.APP_CHARACTERISTICS:
                processAppCharacteristics(ev);
                break;

            case NativeSimTag.STATE_CHECK:
                processStateCheck();
                break;

            case NativeSimTag.GET_NODES:
                submitVmList((List<NativeVm>) ev.getData());
                break;

            // Service
            case NativeSimTag.SERVICE_ALLOCATE:
                processServiceAllocate(ev);
                break;

            case NativeSimTag.SERVICE_SCALING:
                processServiceScaling(ev);
                break;

            case NativeSimTag.SERVICE_UPDATE: //TODO: update需要细化，是改什么资源？怎么改？
                break;

            case NativeSimTag.SERVICE_DESTROY:
                processServiceDestroy(ev);
                break;

            // Request
            case NativeSimTag.REQUEST_DISPATCH:
                processRequestsDispatch(ev);
                break;


            // Cloudlet
            case NativeSimTag.CLOUDLET_PROCESS:
                processCloudlets(ev);
                break;

            case NativeSimTag.INSTANCE_MIGRATE:
                processServiceMigrate(ev);
                break;

            // other tags are processed by cloudsim
            default:
                processOtherEvent(ev);
                break;
        }
    }


    @Override
    public void startEntity() { // 从注册开始，然后部署，接着循环检查monitor，满足某个条件开始scaling
        printLine(getName() + " app is starting...");
        // check the datacenter allocated;
        schedule(brokerId, 0.2, NativeSimTag.CHECK_DC_ALLOCATED);
    }


    @Override
    public void shutdownEntity() {
        sendToExporter();
    }

    /* Characteristic */
    private void processAppCharacteristics(SimEvent ev) {
        if (ev.getData() == null) {
            System.out.println(ev);
            System.out.println("Error: register object is null.");
//            return; // 或者进行一些错误处理逻辑
        }

        Register register = (Register) ev.getData();

        submitRequestList(register.registerRequests()); //TODO: 考虑只保留二者中的一个
        submitRequestTypes(RequestType.map.values().stream().toList());
        assert !getRequestList().isEmpty();
        System.out.println(NativeSim.clock() + ": " + "Requests have been registered.");

        submitInstanceList(register.registerInstanceList());
        assert !getInstanceList().isEmpty();
        System.out.println(NativeSim.clock() + ": " + "Instances have been registered.");

        submitServiceGraph(register.registerServiceGraph());
        assert getServiceGraph() != null;

        List<Service> serviceList = getServiceGraph().getAllServices();
        submitServiceList(serviceList);
        assert !getServiceList().isEmpty();
        System.out.println(NativeSim.clock() + ": " + "Services have been registered.");

        System.out.println(NativeSim.clock() + ": " + "ServiceGraph_" + serviceGraph.getId()+" has been registered.");

        System.out.println(NativeSim.clock() + ": " + "The resource characteristics of the application has been completed.\n");

        send(getId(),0.1, NativeSimTag.SERVICE_ALLOCATE,serviceList);

        send(getId(),0.2, NativeSimTag.REQUEST_DISPATCH, getRequestTypes());

        schedule(getId(),5.0, NativeSimTag.STATE_CHECK);
    }

    private void processStateCheck() {
        // 检查service需不需要扩展
        for (Service service : serviceList) {
            if (serviceScalingPolicy.needScaling(service)) //检查SLO
                sendNow(getId(), NativeSimTag.SERVICE_SCALING, service);
        }

        // 检查instance需不需要迁移
        for (Instance instance : instanceList){
            if (InstanceMigrationPolicy.needMigrate(instance))//检查load balance
                 sendNow(getId(),NativeSimTag.INSTANCE_MIGRATE, instance);
        }

    }

    //TODO: 请求的分发还没有实现排队
    @SuppressWarnings("unchecked")
    private void processRequestsDispatch(SimEvent ev) {
        printLine();

        List<RequestType> typeList = (List<RequestType>) ev.getData();

        for (RequestType type : typeList) {
            // 查找service chain
            List<Service> chain = serviceGraph.getServiceChains().get(type.API);
            // 选择chain的源服务
            Service source = chain.get(0);
            // 源服务创建cloudlets
            List<NativeCloudlet> source_cloudlets = source.createCloudlets(type.getAPI(),type.getNum());

            printEvent(source_cloudlets.get(0).getAPI()+" API Cloudlets have been start.");
            sendNow(getId(),NativeSimTag.CLOUDLET_PROCESS,source_cloudlets);

        }

        requestList.forEach(r -> r.setStartTime(NativeSim.clock()));
        printEvent("Requests have been dispatched.");

        printLine();
    }



    /** Policy */
    @SuppressWarnings("unchecked")
    private void processServiceAllocate(SimEvent ev) {

        serviceAllocationPolicy.init(getVmList());

        List<Service> servicesToAllocate = (List<Service>)ev.getData();

        for (Service service : servicesToAllocate){

            serviceAllocationPolicy.instantiateService(service); // 实例化

            serviceAllocationPolicy.allocateService(service); // 部署

        }
    }

    /** Migrate */
    private void processServiceMigrate(SimEvent ev) {

        serviceAllocationPolicy.init(getVmList());

        Instance instanceToMigrating = (Instance) ev.getData();

        instanceMigrationPolicy.migrateInstance(instanceToMigrating);//TODO: 有时候需要迁移到指定的vm
    }

    private void processServiceScaling(SimEvent ev) {

        Service serviceToScaling = (Service) ev.getData();

        serviceScalingPolicy.scalingService(serviceToScaling);
    }


    private void processServiceDestroy(SimEvent ev) {
        Service serviceToDestroy = (Service) ev.getData();
        serviceAllocationPolicy.deallocateService(serviceToDestroy);
        serviceGraph.deleteService(serviceToDestroy);
    }

    public void distributeCloudlets(List<NativeCloudlet> cloudlets) {

        assert !cloudlets.isEmpty();
        submitCloudlets(cloudlets);

        for (NativeCloudlet cloudlet : cloudlets){
            Service service = Service.getService(cloudlets.get(0).getServiceName());
            List<Instance> instanceList = getInstanceList();
            // 按cloudlet数量重新从小到大排序
            instanceList.sort(Comparator.comparingInt(i -> i.getCloudletScheduler().getCloudletWaitingList().size()));

            Instance selectedInstance = instanceList.get(0);
            cloudlet.setInstanceUid(selectedInstance.getUid());
            selectedInstance.getCloudletScheduler().receiveCloudlet(cloudlet);
        }
    }

    //TODO: 逻辑不完备：目前是按批返回cloudlets，可能会有遗失，而且时间精度会受到影响
    @SuppressWarnings("unchecked")
    private void processCloudlets(SimEvent ev){
        // 来源于同一个服务/请求的cloudlets
        List<NativeCloudlet> cloudlets = (List<NativeCloudlet>) ev.getData();

        distributeCloudlets(cloudlets); // 由于事件发送的异步性，其实是一边distribute，一边execute

        String serviceName = cloudlets.get(0).getServiceName();
        Service service = Service.getService(serviceName);
        String API = cloudlets.get(0).getAPI();

        List<Service> calledService = serviceGraph.getCalls(serviceName,API); //TODO: 能不能放在外部，只维护指针

        for (Instance instance: service.getInstanceList()) {

            instance.getCloudletScheduler().processCloudlets(); // instance处理cloudlets

            // 返回已完成的cloudlets
            List<NativeCloudlet> finishedCloudlets = instance.getCloudletScheduler().getCloudletFinishedList();
            double delay = finishedCloudlets.stream().mapToDouble(NativeCloudlet::getTotalTime) // 获取每个cloudlet的执行时间
                    .max() // 找出最大的执行时间
                    .orElse(0.0); // 如果没有cloudlets，返回0.0作为默认值;

            if (!finishedCloudlets.isEmpty()) {// 被调用服务创建并发送cloudlets
                int finishedNum = finishedCloudlets.size();
                calledService.forEach(s -> send(getId(), delay, NativeSimTag.CLOUDLET_PROCESS, s.createCloudlets(API, finishedNum)));
                printEvent(finishedNum+" cloudlets have been finished");
            }

        }
    }

    private void sendToExporter(){
        Exporter.serviceGraph = getServiceGraph();
        Exporter.requestList = getRequestList();
        Exporter.vmList = getVmList();
        Exporter.instanceList = getInstanceList();
        Exporter.serviceList = getServiceList();
        Exporter.cloudletList = getCloudletList();

        Exporter.totalTime = NativeSim.clock();
        Exporter.calculateRequestStatistics();
    }

    protected void processOtherEvent(SimEvent ev) {
        if (ev == null) {
            printLine(getName() + ".processOtherEvent(): Error - an event is null.");
        }
    }

    public void submitServiceList(List<Service> list){
        getServiceList().addAll(list);
    }
    public void submitService(Service service) {
        getServiceList().add(service);
    }
    public void submitRequestList(List<Request> list){
        getRequestList().addAll(list);
    }
    public void submitRequestTypes(List<RequestType> list){
        getRequestTypes().addAll(list);
    }
    public void submitServiceGraph(ServiceGraph serviceGraph){
        setServiceGraph(serviceGraph);
    }
    public void submitInstanceList(List<? extends Instance> list){
        getInstanceList().addAll(list);
    }
    public void submitInstance(Instance instance){
        getInstanceList().add(instance);
    }
    public void submitVmList(List<NativeVm> vmList){
        getVmList().addAll(vmList);
    }
    public void submitVm(NativeVm vm){
        getVmList().add(vm);
    }
    public void submitCloudlets(List<NativeCloudlet> cloudlets) {
        getCloudletList().addAll(cloudlets);
    }
    public void submitSLA(int slo, int meanLen, int stdDev){
        setSlo(slo);
        NativeCloudlet.meanLength = meanLen;
        NativeCloudlet.stdDev = stdDev;
    }

}
