/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;

import extend.NativeSimTag;
import extend.NativeVm;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import policy.allocation.ServiceAllocationPolicy;
import policy.migration.InstanceMigrationPolicy;
import policy.scaling.ServiceScalingPolicy;
import request.Request;
import request.AppInterface;
import service.Instance;
import service.NativeCloudlet;
import service.Service;
import service.ServiceGraph;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.cloudbus.cloudsim.Log.printLine;


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
    private double checkingInterval;

    /** The service graph. */
    protected ServiceGraph serviceGraph;
    /** The requests list. */
    protected List<Request> requestList = new ArrayList<>();

    protected List<AppInterface> ports = new ArrayList<>();
    /** The services list. */
    protected List<Service> serviceList = new ArrayList<>();
    /** The instances list. */
    protected List<Instance> instanceList = new ArrayList<>();
    /** The vm list. */
    protected List<NativeVm> vmList = new ArrayList<>();
    /** The cloudlet list. */
    protected List<NativeCloudlet> nativeCloudletList = new ArrayList<>();

    /** The allocation policy. */
    private ServiceAllocationPolicy serviceAllocationPolicy;
    /** The migration policy. */
    private InstanceMigrationPolicy instanceMigrationPolicy;
    /** The scaling policy. */
    private ServiceScalingPolicy serviceScalingPolicy;

    private static Logger logger =  LoggerFactory.getLogger(Application.class);

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
        try {
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
                    processRequestsDistribution(ev);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    /* 资源画像：1.通过文件将资源进行注册，定义资源的初始配置。2.根据初始配置提交。 */
    private void processAppCharacteristics(SimEvent ev) throws Exception {
        // register对象非空检查
        if (ev.getData() == null)
            throw new Exception("Error: register object is null.");
        Register register = (Register) ev.getData();

        // 自底向上地提交资源并完善画像
        assert !getRequestList().isEmpty();
        System.out.println(NativeSim.clock() + ": " + "Requests have been registered.");

        //提交实例
        submitInstanceList(register.registerInstanceList());
        assert !getInstanceList().isEmpty();
        System.out.println(NativeSim.clock() + ": " + "Instances have been registered.");

        // 提交服务和调用图
        submitServiceGraph(register.registerServiceGraph());
        assert getServiceGraph() != null;
        List<Service> serviceList = getServiceGraph().getAllServices();
        submitServiceList(serviceList);
        assert !getServiceList().isEmpty();
        System.out.println(NativeSim.clock() + ": " + "Services have been registered.");
        System.out.println(NativeSim.clock() + ": " + "ServiceGraph_" + serviceGraph.getId()+" has been registered.");

        // 提交请求
        submitRequestList(register.registerRequests());
        // 提交接口并映射到调用链
        submitPorts(AppInterface.getPorts());
        for (AppInterface i : getPorts()) {
            // 查找请求对应的service chain
            i.setChain(serviceGraph.getServiceChains().get(i.API));
        }
        // 画像完成后打印输出
        System.out.println(NativeSim.clock() + ": " + "The resource characteristics of the application has been completed.\n");

        // 启动服务部署和请求分发，落实资源画像
        send(getId(),0.1, NativeSimTag.SERVICE_ALLOCATE,serviceList);
        send(getId(),0.2, NativeSimTag.REQUEST_DISPATCH, getRequestList());
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


    @SuppressWarnings("unchecked")
    private void processServiceAllocate(SimEvent ev) {
        // 初始化部署协议和待部署的服务列表
        serviceAllocationPolicy.init(getVmList());
        List<Service> servicesToAllocate = (List<Service>)ev.getData();
        // 遍历并部署服务
        for (Service service : servicesToAllocate){
            serviceAllocationPolicy.instantiateService(service); // 实例化
            serviceAllocationPolicy.allocateService(service); // 部署
        }
        // 部署完成后，启动周期检查，触发服务迁移和扩展
        schedule(getId(),5.0, NativeSimTag.STATE_CHECK);
    }

/* 请求分发的目标：1.唤醒服务调用链。2.选择请求处理的实例。3.一批批地发送处理请求的事件。 4.更新RequestPort对象的统计指标和队列 */
    @SuppressWarnings("unchecked")
    private void processRequestsDistribution(SimEvent ev) {
        List<Request> requests = (List<Request>) ev.getData();
        // 遍历请求列表
        int num = 1;
        for (int i = 0; i < num; i++) { //TODO: 请求的批大小不对
            Request request = requests.get(i);
            String API = request.API;
            // 获取请求映射的源服务
            Service source = request.getChain().get(0);
            // 源服务创建属于这个request的cloudlets,数量为服务的端点数
            int endpoints = source.getApiMap().get(API);
            List<NativeCloudlet> source_Native_cloudlets = source.createCloudlets(request,endpoints);
            // 提交cloudlets
            submitCloudlets(source_Native_cloudlets);
            // 发送处理任务的事件
            sendNow(getId(), NativeSimTag.CLOUDLET_PROCESS, source_Native_cloudlets);
        }
        // 请求分发完成，但还没开始执行
        Reporter.printEvent("\nRequests have been dispatched.");
        printLine();
    }


/* 批量处理某些请求的cloudlets */
    @SuppressWarnings("unchecked")
    private void processCloudlets(SimEvent ev){
//        获取变量
        List<NativeCloudlet> nativeCloudlets = (List<NativeCloudlet>) ev.getData();
        String serviceName = nativeCloudlets.get(0).getServiceName();
        Service service = Service.getService(serviceName);
        String API = nativeCloudlets.get(0).getAPI();
        Request request = nativeCloudlets.get(0).getRequest();
        List<Service> calledService = serviceGraph.getCalls(serviceName,API);
//        service处理cloudlets：
        for (Instance instance: service.getInstanceList()) {
//        遍历实例，让每个实例处理cloudlets
            instance.getCloudletScheduler().processCloudlets();
            // 返回已完成的cloudlets
            List<NativeCloudlet> finishedNativeCloudlets = instance.getCloudletScheduler().getFinishedQueue().stream().toList();
            double delay = finishedNativeCloudlets.stream().mapToDouble(NativeCloudlet::getTotalTime) // 获取每个cloudlet的执行时间
                    .max() // 找出最大的执行时间
                    .orElse(0.0); // 如果没有cloudlets，返回0.0作为默认值;

            if (!finishedNativeCloudlets.isEmpty()) {// 被调用服务创建并发送cloudlets
                int finishedNum = finishedNativeCloudlets.size();
                calledService.forEach(s -> send(getId(), delay, NativeSimTag.CLOUDLET_PROCESS, s.createCloudlets(request, finishedNum)));
                Reporter.printEvent(finishedNum+" cloudlets have been finished");
            }
        }
    }


    /**
     * 计算已完成的原生云任务的最大延迟时间。
     *
     * @param finishedNativeCloudlets 完成的原生云任务列表，不应为空。
     * @return 返回已完成的原生云任务中的最大延迟时间，如果列表为空，则返回0.0。
     */
    private double calculateMaxDelay(List<NativeCloudlet> finishedNativeCloudlets) {
        // 使用Stream API来计算列表中云任务的最长总时间
        return finishedNativeCloudlets.stream()
                .mapToDouble(NativeCloudlet::getTotalTime) // 将云任务映射到其总时间，并以双精度浮点数处理
                .max() // 查找最大的总时间
                .orElse(0.0); // 如果列表为空，返回0.0
    }


    /** Migrate */
    private void processServiceMigrate(SimEvent ev) {

        serviceAllocationPolicy.init(getVmList());

        Instance instanceToMigrating = (Instance) ev.getData();

        instanceMigrationPolicy.migrateInstance(instanceToMigrating);
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



    private void sendToExporter(){
        Exporter.serviceGraph = getServiceGraph();
        Exporter.requestList = getRequestList();
        Exporter.vmList = getVmList();
        Exporter.instanceList = getInstanceList();
        Exporter.serviceList = getServiceList();
        Exporter.nativeCloudletList = getNativeCloudletList();

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
    public void submitPorts(List<AppInterface> list){
        getPorts().addAll(list);
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
    public void submitCloudlets(List<NativeCloudlet> nativeCloudlets) {
        getNativeCloudletList().addAll(nativeCloudlets);
    }
    public void submitSLA(int meanLen, int stdDev){
        NativeCloudlet.meanLength = meanLen;
        NativeCloudlet.stdDev = stdDev;
    }

}
