package core;

import entity.API;
import entity.Instance;
import entity.ServiceGraph;
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
    // Total time for handling requests
    public static double totalTime;
    public static int schedulingInterval = 10;
    // Graph representing the service structure
    public static ServiceGraph serviceGraph;
    // Count numbers
    public static int totalRequests = 0;
    public static int totalResponses = 0;
    public static double totalDelay = 0;
    public static int failedRequests = 0;
    public static int sloViolations = 0;
    public static double avgQps;
    public static List<Double> qpsHistory = new ArrayList<>();

    public static int totalVms = 0;
    public static int totalCloudlets = 0;
    public static int totalServices = 0;
    public static int totalInstances = 0;

    // Previous time point for exporting data
    public static double previousTime = 0.0;
    // instance id ->  utilization
    public static Map<String, List<UsageData>> usageOfCpuHistory = new HashMap<>();
    public static Map<String, List<UsageData>> usageOfRamHistory = new HashMap<>();
    public static Map<String, List<UsageData>> usageOfReceiveBwHistory = new HashMap<>();
    public static Map<String, List<UsageData>> usageOfTransmitBwHistory = new HashMap<>();


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
        avgQps = qpsHistory.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public static void updateQPSHistory(double clock, int requestCount, int requestInterval) {
        double qps = (double) requestCount / requestInterval;
        qpsHistory.add(qps);
    }


    public static void addUsageData(Map<String, List<UsageData>> historyMap, String instanceId, double timestamp, double usage) {
        List<UsageData> usageDataList = historyMap.get(instanceId);
        if (usageDataList == null) {
            usageDataList = new ArrayList<>();
            historyMap.put(instanceId, usageDataList);
        }
        usageDataList.add(new UsageData(timestamp, usage));
    }


}
