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
    // Interval for exporting data
    public static double exportInterval = 1.0;
    // Previous time point for exporting data
    public static double previousTime = 0.0;
    // Graph representing the service structure
    public static ServiceGraph serviceGraph;
    // List of requests
    public static List<Request> requestList = new ArrayList<>();
    // List of API characteristics of requests
    public static List<API> requestCharacteristics = new ArrayList<>();
    // List of services
    public static List<Service> serviceList = new ArrayList<>();
    // List of instances
    public static List<Instance> instanceList = new ArrayList<>();
    // List of virtual machines (VMs)
    public static List<NativeVm> vmList = new ArrayList<>();
    // List of cloudlets
    public static List<NativeCloudlet> nativeCloudletList = new ArrayList<>();
    // Total time for handling requests
    public static double totalTime;
    // Map storing request and corresponding handling time
    public static Map<Request, Double> requestTimeMap;
    // Total number of responses
    public static int totalResponses;
    // Total number of requests
    public static int totalRequests;
    // Queries per second (QPS)
    public static double qps;
    public static double avgQps;
    // Number of failed requests
    public static int failedRequests;
    // Failure rate of requests
    public static double failRate;
    // Total time for handling requests
    public static double requestsHandleTime;
    // Average response time for requests
    public static double averageResponseTime;
    // Service Level Objective (SLO) threshold in milliseconds (1 second, adjustable)
    public static long sloThreshold = 5;
    // SLO violation rate
    public static double sloViolationRate;

    // History lists for storing historical data
    public static List<Double> qpsHistory = new ArrayList<>();
    public static List<Integer> failedRequestsHistory = new ArrayList<>();
    public static List<Double> averageResponseTimeHistory = new ArrayList<>();
    public static List<Double> sloViolationRateHistory = new ArrayList<>();

    // Method to calculate request statistics
    public static void calculateRequestStatistics() {
        totalRequests = requestList.size();
        failRate = ((double) failedRequests / totalRequests) * 100;

        calculateFailedRequests(requestList);
        calculateAverageResponseTime(requestList);
        calculateSloViolationRate(requestList);
    }

    // Method to update history data based on the current time and request count
    public static void updateQPSHistory(double clock, int requestCount,int requestInterval) {
        if (clock - previousTime >= requestInterval) {
            qps = (double) requestCount / requestInterval;
            qpsHistory.add(qps);
            calculateRequestStatistics();

            // Add current time point statistics to historical data lists
            failedRequestsHistory.add(failedRequests);

            previousTime = clock; // Update previousTime
        }
    }

    // Method to calculate the number of failed requests
    public static void calculateFailedRequests(List<Request> requestsList) {
        failedRequests = 0;
        for (Request request : requestsList) {
            if (request.getStatus() == Status.Failed) { // Assuming the failed status is "Failed"
                failedRequests++;
            }
        }
    }

    public static void calculateAverageQPS(){
        avgQps = qpsHistory.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    // Method to calculate the average response time
    public static void calculateAverageResponseTime(List<Request> requestsList) {
        assert !requestsList.isEmpty();
        long totalResponseTime = 0;
        for (Request request : requestsList) {
            totalResponseTime += (long) request.getResponseTime();
        }
        averageResponseTime = (double) totalResponseTime / totalRequests;
    }

    // Method to calculate the SLO violation rate
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
