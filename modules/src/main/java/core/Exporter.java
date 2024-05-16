package core;

import entity.API;
import entity.Request;
import entity.NativeCloudlet;
import entity.Instance;
import entity.Service;
import entity.ServiceGraph;
import extend.NativeVm;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Exporter {
    public static double exportInterval = 5.0;
    public static double previousTime = 0.0;
    public static ServiceGraph serviceGraph;
    public static List<Request> requestList = new ArrayList<>();
    public static List<API> requestCharacteristics = new ArrayList<>();
    public static List<Service> serviceList = new ArrayList<>();
    public static List<Instance> instanceList = new ArrayList<>();
    public static List<NativeVm> vmList = new ArrayList<>();
    public static List<NativeCloudlet> nativeCloudletList = new ArrayList<>();
    public static double totalTime;
    public static Map<Request, Double> requestTimeMap;
    public static int totalResponses;
    public static int totalRequests;
    public static double qps;
    public static int failedRequests;
    public static double failRate;
    public static double requestsHandleTime;
    public static double averageResponseTime;
    public static long sloThreshold = 1000; // 1秒，可以根据实际情况调整
    public static double sloViolationRate;

    // 历史数据列表
    public static List<Double> qpsHistory = new ArrayList<>();
    public static List<Integer> failedRequestsHistory = new ArrayList<>();
    public static List<Double> averageResponseTimeHistory = new ArrayList<>();
    public static List<Double> sloViolationRateHistory = new ArrayList<>();

    public static void calculateRequestStatistics() {
        totalRequests = requestList.size();
        failRate = ((double) failedRequests / totalRequests) * 100;

        calculateFailedRequests(requestList);
        calculateAverageResponseTime(requestList);
        calculateSloViolationRate(requestList);
    }

    public static void updateHistory(double clock, int requestCount) {
        if (clock - previousTime >= exportInterval) {
            qps = requestCount / exportInterval;
            qpsHistory.add(qps);
            calculateRequestStatistics();

            // 将当前时间点的统计数据添加到历史数据列表中
            failedRequestsHistory.add(failedRequests);
            averageResponseTimeHistory.add(averageResponseTime);
            sloViolationRateHistory.add(sloViolationRate);

            previousTime = clock; // 更新 previousTime
        }
    }

    // 计算失败的请求数量
    public static void calculateFailedRequests(List<Request> requestsList) {
        failedRequests = 0;
        for (Request request : requestsList) {
            if (request.getStatus() == Status.Failed) { // 假设失败的状态为 "failed"
                failedRequests++;
            }
        }
    }

    // 计算平均响应时间
    public static void calculateAverageResponseTime(List<Request> requestsList) {
        assert !requestsList.isEmpty();
        long totalResponseTime = 0;
        for (Request request : requestsList) {
            totalResponseTime += (long) request.getResponseTime();
        }
        averageResponseTime = (double) totalResponseTime / totalRequests;
    }

    // 计算SLO违规率
    public static void calculateSloViolationRate(List<Request> requestsList) {
        int violations = 0;
        for (Request requests : requestsList) {
            if (requests.getResponseTime() > sloThreshold) {
                violations++;
            }
        }
        sloViolationRate = (requestsList.isEmpty()) ? 0 : ((double) violations / requestsList.size()) * 100;
    }
}