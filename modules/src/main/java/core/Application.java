/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;

import extend.CloudNativeSimTag;
import extend.NativeVm;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import policy.allocation.ServiceAllocationPolicy;
import policy.cloudletScheduler.NativeCloudletScheduler;
import policy.migration.InstanceMigrationPolicy;
import entity.Request;
import entity.API;
import entity.Instance;
import entity.RpcCloudlet;
import entity.Service;
import entity.ServiceGraph;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import policy.migration.InstanceMigrationPolicySimple;
import policy.scaling.ServiceScalingPolicy;

import static core.Reporter.printEvent;
import static entity.Instance.instanceUidMap;
import static org.cloudbus.cloudsim.Log.printLine;


@Setter
@Getter
public class Application extends SimEntity {
    /**
     * The id of datacenter broker.
     */
    private int brokerId;
    /**
     * The regional cis name.
     */
    private String regionalCisName;
    /**
     * The last process time.
     */
    private double lastProcessTime;
    /**
     * The scheduling interval.
     * 会影响利用率模型的更新,迁移和扩展的操作
     */
    private int schedulingInterval = 10;
    /**
     * The generator.
     */
    protected Generator generator;
    /**
     * The exporter.
     */
//    protected Exporter exporter;

    /**
     * The service graph.
     */
    protected ServiceGraph serviceGraph;
    /**
     * The requests list.
     */
    protected List<Request> requestList = new ArrayList<>();

    protected List<API> apis = new ArrayList<>();
    /**
     * The services list.
     */
    protected List<Service> serviceList = new ArrayList<>();
    /**
     * The instances list.
     */
    protected List<Instance> instanceList = new ArrayList<>();
    /**
     * The created vm list.
     */
    protected List<NativeVm> vmList = new ArrayList<>();
    /**
     * The cloudlet list.
     */
    protected List<RpcCloudlet> rpcCloudletList = new ArrayList<>();
    protected int finishedCloudletNum;
    /**
     * The allocation policy.
     */
    private ServiceAllocationPolicy serviceAllocationPolicy;
    /**
     * The migration policy.
     */
    private InstanceMigrationPolicy instanceMigrationPolicy;


    private static Logger logger = LoggerFactory.getLogger(Application.class);


    public Application(String appName, int brokerId,
                       ServiceAllocationPolicy serviceAllocationPolicy) {
        super(appName);
        setBrokerId(brokerId);
        setServiceAllocationPolicy(serviceAllocationPolicy);
        setInstanceMigrationPolicy(new InstanceMigrationPolicySimple());
    }


    @SuppressWarnings("unchecked")
    @Override
    public void processEvent(SimEvent ev) {
        try {
            switch (ev.getTag()) {

                case CloudNativeSimTag.APP_CHARACTERISTICS:
                    processAppCharacteristics(ev);
                    break;

                case CloudNativeSimTag.GET_NODES:
                    submitVmList((List<NativeVm>) ev.getData());
                    break;
                case CloudNativeSimTag.START_CLIENTS:
                    startClients();
                    break;

                case CloudNativeSimTag.SERVICE_ALLOCATE:
                    processServiceAllocate(ev);
                    break;

                case CloudNativeSimTag.SERVICE_SCALING:
                    processServiceScaling(ev);
                    break;

                case CloudNativeSimTag.SERVICE_UPDATE: //TODO: update需要细化，是改什么资源？怎么改？
                    break;

                case CloudNativeSimTag.SERVICE_DESTROY:
                    processServiceDestroy(ev);
                    break;

                // Request
                case CloudNativeSimTag.REQUEST_GENERATE:
                    processRequestGenerate(ev);
                    break;

                case CloudNativeSimTag.REQUEST_DISPATCH:
                    processRequestsDispatch(ev);
                    break;

                // Cloudlet
                case CloudNativeSimTag.CLOUDLET_PROCESS:
                    processCloudlets(ev);
                    break;

                case CloudNativeSimTag.INSTANCE_MIGRATE:
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
    public void startEntity() {
        printLine(getName() + " app is starting...");
        // check if datacenter are deployed
        schedule(brokerId, 0.2, CloudNativeSimTag.CHECK_DC_ALLOCATED);
        // check if services are deployed
    }

    @Override
    public void shutdownEntity() {
        Exporter.totalTime = CloudNativeSim.clock();
        Exporter.finalServiceList = serviceList;
        Exporter.finalInstanceList = serviceList.stream()
                .flatMap(service -> service.getInstanceList().stream())
                .collect(Collectors.toList());
        printEvent(getFinishedCloudletNum()+ " cloudlets have been finished");
    }

    /**
     * The print interval.
     * 会影响打印的步长,
     */
    private int printInterval = 100;
    private double lastPrintTime = 0;

    public void printByInterval(String msg) {
        double currentTime = CloudNativeSim.clock();
        if (currentTime - lastPrintTime >= printInterval) {
            printEvent(msg);
            lastPrintTime = currentTime;
        }
    }


    /* 资源画像：1.通过文件将资源进行注册，定义资源的初始配置。2.根据初始配置提交。 */
    private void processAppCharacteristics(SimEvent ev) throws Exception {
        // 检查是否已经注册
        assert !getApis().isEmpty():"APIs have not been registered.";
        assert !getInstanceList().isEmpty():"Instances have not been registered.";
        assert getServiceGraph() != null:"ServiceGraph has not been registered.";
        assert !getServiceList().isEmpty():"Services have been not registered.";
        // 构建service chains和api之间的联系
        serviceGraph.buildServiceChains(getApis());

        // 匹配实例并处理失败情况
        List<Service> instantiatedServices = getServiceList().stream()
                .filter(service -> {
                    boolean match = matchInstances(service);
                    if (!match) {
                        System.out.println("Match failed for service: " + service);
                    }
                    return match;
                }).toList();
        setServiceList(instantiatedServices);
        // 画像完成后打印输出
        printEvent("The resource characteristics of the application has been completed.");
        // 启动服务部署，落实资源画像
        send(getId(), 0.1, CloudNativeSimTag.SERVICE_ALLOCATE, serviceList);
    }


    public boolean matchInstances(Service service){ //TODO: 未参与实例化的instance怎么办？这个函数放这里合适吗？
        boolean result = false;

        List<String> s_labels = service.getLabels();
        // 构建多对多的映射labels
        for (Instance instance: instanceUidMap.values()){
            for (String i_label:instance.getLabels()){
                if (s_labels.contains(i_label)) {
                    result = true;
                    service.setBeingInstantiated(true);
                    service.getInstanceList().add(instance);
                    service.setStatus(Status.Ready);
                    instance.getServiceList().add(service.name);
                }
            }
        }

        if (!result)
            System.out.println("[ServiceAllocation.instantiateService] Service #"+service.getName()+" can not be instantiated.");
        return result;
    }

    private void startClients() {
        double startTime = CloudNativeSim.clock();
        generator.previousTime = startTime;
        generator.initializeCumulativeWeights();
        printLine();
        printEvent("clients are starting...");
        // 每隔一个interval生成请求
        double sendTime = startTime + 0.1;
        //TODO: 如果不限制timeLimit怎么办
        for (int tick = 1; tick <= generator.timeLimit; tick ++) {
            sendTime = startTime + tick * requestInterval;
            scheduleFirst(sendTime, CloudNativeSimTag.REQUEST_GENERATE);
            // 通过整型取余来检查利用率, 确定是否需要scaling
            if ( tick % schedulingInterval == 0){
                schedule(sendTime, CloudNativeSimTag.SERVICE_SCALING);
            }
        }
        Exporter.arrivalSession = sendTime - startTime; //requestInterval的整数倍,正常情况下等于timeLimit
    }

    // 选择负载均衡的方式
    private void processServiceScaling(SimEvent ev) {
        // log
//        printEvent("start scaling...");
        // 判断实例是否需要scaling，所以遍历实例
        for(Service service:serviceList){
            ServiceScalingPolicy scalingPolicy = service.getServiceScalingPolicy();
            //TODO: 如果实例跟服务一对多的关系怎么办？要避免重复伸缩
            if(scalingPolicy.needScaling(service)) {
                // scaling
                scalingPolicy.scaling(service);
                // 更新实例
                List<Instance> newInstances = scalingPolicy.getNewInstances();
                getInstanceList().addAll(newInstances);
            }
        }
    }



    @SuppressWarnings("unchecked")
    private void processServiceAllocate(SimEvent ev) {
        // 初始化部署协议和待部署的服务列表
        serviceAllocationPolicy.init(getVmList());
        List<Service> servicesToAllocate = (List<Service>) ev.getData();
        // 遍历并部署服务
        for (Service service : servicesToAllocate) {
            assert service.getStatus() == Status.Ready; // 实例化
            serviceAllocationPolicy.allocateService(service); // 部署
            List<Instance> createdInstances = service.getInstanceList();
            // 注入Policy的参数

            service.getCloudletScheduler().setSchedulingInterval(schedulingInterval);

            service.getServiceScalingPolicy().setServiceAllocationPolicy(serviceAllocationPolicy);
            service.getServiceScalingPolicy().setVmList(getServiceAllocationPolicy().getVmList());
        }

        // 提交已部署的实例
        setInstanceList(serviceAllocationPolicy.getCreatedInstanceList());
        send(0.1, CloudNativeSimTag.START_CLIENTS);

    }


    /**
     * The requests arrival interval.
     * 请求每隔一段时间一次性到达,设置为1,即每秒到达.
     */
    private int requestInterval = 1;
    int count = 1;

    private void processRequestGenerate(SimEvent ev) {

        int totalRequests = getRequestList().size();
        // 计算超量值
        int excess = totalRequests - generator.numLimit;
        // 检查是否超出了请求限制
        if (excess > 0) {
            // 确保不会有负索引
            int startRemoveIndex = Math.max(0, totalRequests - excess);
            // 直接在原列表上进行删除
            getRequestList().subList(startRemoveIndex, totalRequests).clear();

            return;
        }
        // 生成
        List<Request> requestArrival = generator.generateRequests(CloudNativeSim.clock());

        // 绑定API
        requestArrival.forEach(request -> request.getApi().getRequests().add(request));
        // 提交
        submitRequestList(requestArrival);

        // 分发
        sendNow( CloudNativeSimTag.REQUEST_DISPATCH, requestArrival);
        // 更新全局QPS
        Exporter.updateGlobalRPSHistory(CloudNativeSim.clock(), requestArrival.size(), requestInterval);
        // 更新每个API的QPS
        Exporter.updateApiQpsHistory(CloudNativeSim.clock(), requestArrival, requestInterval);
            // 打印输出
        printByInterval(String.format("%d requests have arrived. ", requestArrival.size()));

    }


    /* 请求分发到对应服务 */
    @SuppressWarnings("unchecked")
    private void processRequestsDispatch(SimEvent ev) {
        List<Request> requests = (List<Request>) ev.getData();
        Map<Service,List<RpcCloudlet>> cloudletMap = new HashMap<>();
        for (Request request : requests) {
            API api = request.getApi();
            // 查找service chain
            List<Service> chain = api.getServiceChain();
            assert chain != null;
            request.setServiceChain(chain);
            // 选择chain的源服务
            List<Service> sources = serviceGraph.getSources(chain);
            for (Service source : sources) {
                // 源服务创建cloudlets
                RpcCloudlet sourceCloudlet = source.createCloudlet(request,generator);
                cloudletMap.computeIfAbsent(source,k->new ArrayList<>());
                cloudletMap.get(source).add(sourceCloudlet);
            }
        }
        // 分流到各个服务
        for (Service service:cloudletMap.keySet()){
            List<RpcCloudlet> rpcCloudlets = cloudletMap.get(service);
            Object[] data = new Object[]{service,rpcCloudlets};
            sendNow(getId(), CloudNativeSimTag.CLOUDLET_PROCESS, data);
        }

        // printByInterval("requests have been dispatched, start cloudlets.");
    }

    @SuppressWarnings("unchecked")
    private void processCloudlets(SimEvent ev) {
        Object[] data = (Object[]) ev.getData();
        // 这些cloudlets都来源于相同的服务
        Service service = (Service) data[0];
        List<RpcCloudlet> cloudlets = (List<RpcCloudlet>) data[1];
        if (cloudlets == null || cloudlets.isEmpty()) {
            throw new IllegalArgumentException("Cloudlet list cannot be null or empty");
        }
        submitCloudlets(cloudlets);

        // 获取变量
        NativeCloudletScheduler scheduler = service.getCloudletScheduler();

        // 接收
        scheduler.addToWaitingQueue(cloudlets);

        // instance处理当前stage
        while (!scheduler.getWaitingQueue().isEmpty()) {
            scheduler.schedule();
            scheduler.processCloudlets();//更新每个cloudlet的等待时间和执行时间
        }

        List<RpcCloudlet> completed = cloudlets.stream()
                .filter(rpcCloudlet -> rpcCloudlet.getStatus()==Status.Success).toList();
        // cloudlet挨个被执行，stage的时延等于它们的总和
        double spawnTime = completed.stream()
                .mapToDouble(cloudlet -> cloudlet.getWaitTime() + cloudlet.getExecTime())
                .max().orElse(0.0);
        //TODO: 执行失败？
        if (spawnTime == 0.0) return;

        finishedCloudletNum += completed.size();

        Map<Service,List<RpcCloudlet>> cloudletMap = new HashMap<>();
        // 更新请求
        for (RpcCloudlet cloudlet : cloudlets) {
            Request request = cloudlet.getRequest();
            List<Service> chain = request.getServiceChain();
            // update delay
            double session = cloudlet.getWaitTime() + cloudlet.getExecTime();
            List<Service> parents = service.getParentServicesInChain(chain);

            double currentMaxDelay = parents.stream()
                    //TODO: 为什么nodeDelay.get(s)有的时候会出现null?
                    .mapToDouble(s -> request.getNodeDelay().getOrDefault(s,0.0))
                    .max().orElse(0.);
            request.addDelay(service, session+currentMaxDelay);

            // 查询子服务
            List<Service> next = service.getChildServicesInChain(chain);
            // 链路中下级服务非空,衍生并分流
            if (!next.isEmpty()) {
                next.forEach(s -> cloudletMap.computeIfAbsent(s,k -> new ArrayList<>())
                        .add(s.createCloudlet(request,generator)));
            } else {
                // 更新请求的关键路径
                updateResponseTimeByCriticalPath(request);
            }
        }
        // 发送
        for (Service s: cloudletMap.keySet())
            schedule(spawnTime, CloudNativeSimTag.CLOUDLET_PROCESS,
                    new Object[]{s,cloudletMap.get(s)});

    }

    private void updateResponseTimeByCriticalPath(Request request){
        List<Service> serviceChain = request.getServiceChain();
        // service -> delay
        Map<Service,Double> nodeDelay = request.getNodeDelay();
        // 计算尾节点的最大延迟
        List<Service> sinks = serviceGraph.getSinks(serviceChain);
        double maxResponseTime = sinks.stream()
                .mapToDouble(service -> nodeDelay.getOrDefault(service, 0.0))
                .max()
                .orElse(0.0);

        request.setResponseTime(maxResponseTime);
    }

    private void processServiceMigrate(SimEvent ev) {

        serviceAllocationPolicy.init(getVmList());

        Instance instanceToMigrating = (Instance) ev.getData();

        instanceMigrationPolicy.migrateInstance(instanceToMigrating);
    }

//    private void processServiceScaling(SimEvent ev) {
//
//        Service serviceToScaling = (Service) ev.getData();
//
//        serviceToScaling.getServiceScalingPolicy().scaling();
//    }


    private void processServiceDestroy(SimEvent ev) {
        Service serviceToDestroy = (Service) ev.getData();
        serviceAllocationPolicy.deallocateService(serviceToDestroy);
        serviceGraph.deleteService(serviceToDestroy);
    }




    protected void processOtherEvent(SimEvent ev) {
        if (ev == null) {
            printLine(getName() + ".processOtherEvent(): Error - an event is null.");
        }
    }

    public void submitServiceList(List<Service> list) {
        getServiceList().addAll(list);
    }

    public void submitService(Service service) {
        getServiceList().add(service);
    }

    public void submitRequestList(List<Request> list) {
        getRequestList().addAll(list);
    }

    public void submitServiceGraph(ServiceGraph serviceGraph) {
        setServiceGraph(serviceGraph);
        Exporter.finalServiceGraph = serviceGraph;
    }


    public void submitInstanceList(List<? extends Instance> list) {
        getInstanceList().addAll(list);
    }

    public void submitInstance(Instance instance) {
        getInstanceList().add(instance);
    }

    public void submitVmList(List<NativeVm> vmList) {
        getVmList().addAll(vmList);
    }

    public void submitVm(NativeVm vm) {
        getVmList().add(vm);
    }

    public void submitCloudlets(List<RpcCloudlet> rpcCloudlets) {
        getRpcCloudletList().addAll(rpcCloudlets);
    }

    public void submitAPIs(List<API> apis) {
        getApis().addAll(apis);
    }

    public void submitGenerator(Generator generator) {
        setGenerator(generator);
    }

    private void send(double delay, int cloudSimTag, Object data) {
        send(getId(), delay, cloudSimTag, data);
    }

    private void send(double delay, int cloudSimTag) {
        send(getId(), delay, cloudSimTag);
    }

    private void sendNow(int cloudSimTag, Object data) {
        sendNow(getId(), cloudSimTag, data);
    }

    private void sendNow(int cloudSimTag) {
        sendNow(getId(), cloudSimTag);
    }

    private void schedule(double delay, int cloudSimTag, Object data) {
        schedule(getId(), delay, cloudSimTag, data);
    }

    private void schedule(double delay, int cloudSimTag) {
        schedule(getId(), delay, cloudSimTag);
    }


    private void scheduleFirst(double delay, int cloudSimTag) {
        scheduleFirst(getId(), delay, cloudSimTag);
    }

    private void scheduleFirst(double delay, int cloudSimTag, Object data) {
        scheduleFirst(getId(), delay, cloudSimTag,data);
    }

    public void submitSchedulingInterval(int schedulingInterval) {
        Exporter.schedulingInterval = schedulingInterval;
    }
}
