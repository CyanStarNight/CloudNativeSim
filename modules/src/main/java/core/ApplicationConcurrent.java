///*
// * Copyright ©2024. Jingfeng Wu.
// */
//
//package core;
//
//import entity.*;
//import extend.CloudNativeSimTag;
//import extend.NativeVm;
//import lombok.Getter;
//import lombok.Setter;
//import org.cloudbus.cloudsim.core.SimEntity;
//import org.cloudbus.cloudsim.core.SimEvent;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import policy.allocation.ServiceAllocationPolicy;
//import policy.cloudletScheduler.NativeCloudletScheduler;
//import policy.migration.InstanceMigrationPolicy;
//import policy.scaling.ServiceScalingPolicy;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.locks.ReentrantLock;
//
//import static core.Exporter.*;
//import static core.Reporter.printEvent;
//import static entity.Instance.InstanceUidMap;
//import static org.cloudbus.cloudsim.Log.printLine;
//
//
//@Setter
//@Getter
//public class ApplicationConcurrent extends Application {
//    private final ExecutorService executorService;
//    private final ReentrantLock lock = new ReentrantLock();
//    public ApplicationConcurrent(String appName, int brokerId, ServiceAllocationPolicy serviceAllocationPolicy, InstanceMigrationPolicy instanceMigrationPolicy, ServiceScalingPolicy serviceScalingPolicy) {
//        super(appName, brokerId, serviceAllocationPolicy, instanceMigrationPolicy, serviceScalingPolicy);
//        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // Or any number you see fit
//    }
//
//    private void processRequestsDispatch(SimEvent ev) {
//        executorService.submit(() -> {
//            List<Request> requestsToDispatch = (List<Request>) ev.getData();
//            for (Request request : requestsToDispatch) {
//                handleRequestDispatch(request);
//            }
//        });
//    }
//    private void handleRequestDispatch(Request request) {
//        String apiName = request.getApiName();
//        List<Service> chain = serviceGraph.getServiceChains().get(apiName);
//        request.setServiceChain(chain);
//        Service source = chain.get(0);
//        List<NativeCloudlet> sourceCloudlets = source.createCloudlets(request, generator);
//        sendNow(getId(), CloudNativeSimTag.CLOUDLET_PROCESS, sourceCloudlets);
//    }
//
//    private void processCloudlets(SimEvent ev) {
//        executorService.submit(() -> {
//            List<NativeCloudlet> cloudlets = (List<NativeCloudlet>) ev.getData();
//            handleCloudletsProcessing(cloudlets);
//        });
//    }
//
//    private void handleCloudletsProcessing(List<NativeCloudlet> cloudlets) {
//        if (!cloudlets.isEmpty()) {
//            submitCloudlets(cloudlets);
//            NativeCloudlet behavior = cloudlets.get(0);
//            String serviceName = behavior.getServiceName();
//            Service service = Service.getService(serviceName);
//            Request request = behavior.getRequest();
//            String apiName = request.getApiName();
//            NativeCloudletScheduler scheduler = service.getCloudletScheduler();
//            scheduler.distributeCloudlets(cloudlets, service.getInstanceList());
//            scheduler.processCloudlets();
//            double totalTime = calculateTotalTime(cloudlets);
//            finishedCloudletNum += cloudlets.size();
//            processNextService(serviceName, apiName, request, totalTime);
//        }
//    }
//
//    private void processNextService(String serviceName, String apiName, Request request, double totalTime) {
//        List<Service> next = serviceGraph.getCalls(serviceName, apiName);
//        if (!next.isEmpty()) {
//            next.forEach(s -> schedule(totalTime, CloudNativeSimTag.CLOUDLET_PROCESS, s.createCloudlets(request, generator)));
//        } else {
//            updateRequestCriticalPath(request, totalTime);
//        }
//    }
//}
