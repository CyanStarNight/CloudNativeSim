package core;

import entity.*;
import extend.UsageData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Exporter {
    // Total time for running
    public static double totalTime;
    public static double arrivalSession;
    public static int schedulingInterval = 10;
    // Graph representing the service structure
    public static ServiceGraph finalServiceGraph;
    public static List<Service> finalServiceList;
    public static List<Instance> finalInstanceList;
    // Count numbers
    public static int totalRequests = 0;
    public static int totalResponses = 0;
    public static double totalDelay = 0;
    public static int failedRequests = 0;
    public static double failedRate = 0;
    public static int sloViolations = 0;
    public static double avgRps;
    public static List<Double> rpsHistory = new ArrayList<>();

    public static int totalVms = 0;
    public static int totalCloudlets = 0;
    public static int totalServices = 0;
    public static int totalInstances = 0;

    // Previous time point for exporting data
    public static double previousTime = 0.0;
    // instance id -> (currentTime,usage)
    public static Map<String, List<UsageData>> usageOfCpuHistory = new HashMap<>();
    public static Map<String, List<UsageData>> usageOfRamHistory = new HashMap<>();
    public static Map<String, List<UsageData>> usageOfReceiveBwHistory = new HashMap<>();
    public static Map<String, List<UsageData>> usageOfTransmitBwHistory = new HashMap<>();

    public static void updateUsageHistory(Instance instance, double currentTime, double session) {

        String instanceUid = instance.getUid();
        // 更新或者初始化CPU使用历史记录
        updateUsageData(usageOfCpuHistory, instanceUid, currentTime,session,instance.getUsedShare());
        // 更新或者初始化RAM使用历史记录
        updateUsageData(usageOfRamHistory, instanceUid, currentTime,session,instance.getUsedRam());
        // 更新接收带宽使用历史记录
//            addUsageData(usageOfReceiveBwHistory, instanceUid, currentTime,instance.getUsedShare());
        // 更新传输带宽使用历史记录
//            addUsageData(usageOfTransmitBwHistory, instanceUid, currentTime,instance.getUsedShare());

        previousTime = currentTime;
    }

    public static void getApiStatistics(List<API> apis) {
        totalDelay = 0.0;
        sloViolations = 0;
        totalRequests = 0;
        for (API api : apis) {
            int num = api.getRequests().size();
            totalRequests += num;
            totalDelay += api.getAverageDelay()*num;
            sloViolations += api.getSloViolations();
        }

        avgRps = totalRequests / arrivalSession;

        failedRequests = totalRequests - totalResponses;

        failedRate = (double) failedRequests /totalRequests;
    }

    public static void updateGlobalRPSHistory(double clock, int requestCount, int requestInterval) {
        double rps = (double) requestCount / requestInterval;
        rpsHistory.add(rps);
    }

    public static void updateApiQpsHistory(double clock, List<Request> requestArrival, int requestInterval){
        Map<String, Integer> arrivals = new HashMap<>();
        for (Request request : requestArrival) {
            API api = request.getApi();
            arrivals.merge(api.getName(), 1, Integer::sum);
        }
        for (String apiName : arrivals.keySet()) {
            API api = API.apiMap.get(apiName);
            api.updateQPSHistory(CloudNativeSim.clock(), arrivals.get(apiName), requestInterval);
        }
    }


    public static void updateUsageData(Map<String, List<UsageData>> historyMap, String instanceId, double timestamp,double session, double usage) {
        List<UsageData> usageDataList = historyMap.get(instanceId);
        if (usageDataList == null) {
            usageDataList = new ArrayList<>();
            historyMap.put(instanceId, usageDataList);
        }
        usageDataList.add(new UsageData(timestamp,session, usage));
    }

    public static void exportUsageHistory(String instanceId){
        Instance instance = Instance.getInstance(instanceId);
        List<RpcCloudlet> completionCloudlets = instance.getCompletionCloudlets();

        if (completionCloudlets.isEmpty()) {
            System.out.println("No cloudlets completed for this instance.");
            return;
        }

        // 初始化变量
        double minStartTime = Double.MAX_VALUE;
        double maxEndTime = Double.MIN_VALUE;
        double totalShare = 0;

        // 遍历cloudlets计算最小开始时间、最大结束时间和总共的share
        for (RpcCloudlet cloudlet : completionCloudlets) {
            double startTime = cloudlet.getStartExecTime();
            double endTime = startTime+cloudlet.getExecTime();
            double share = cloudlet.getShare();

            if (startTime < minStartTime) {
                minStartTime = startTime;
            }
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
            totalShare += share;
        }

        // 输出结果
        System.out.println(instanceId+":");
        System.out.println("Minimum Start Time: " + minStartTime);
        System.out.println("Maximum End Time: " + maxEndTime);
        System.out.println("Total Share Used: " + totalShare);
    }


}
