/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.core;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.extend.NativeCloudlet;
import org.cloudbus.nativesim.extend.NativeVm;
import org.cloudbus.nativesim.request.Request;
import org.cloudbus.nativesim.request.RequestType;
import org.cloudbus.nativesim.service.Instance;
import org.cloudbus.nativesim.service.Service;
import org.cloudbus.nativesim.service.ServiceGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Exporter{
    /** The service graph. */
    protected static ServiceGraph serviceGraph;
    /** The requests list. */
    protected static List<Request> requestList = new ArrayList<>();

    protected static List<RequestType> requestTypes = new ArrayList<>();
    /** The services list. */
    protected static List<Service> serviceList = new ArrayList<>();
    /** The instances list. */
    protected static List<Instance> instanceList = new ArrayList<>();
    /** The vm list. */
    protected static List<NativeVm> vmList = new ArrayList<>();
    /** The cloudlet list. */
    protected static List<NativeCloudlet> cloudletList = new ArrayList<>();

    protected static double totalTime; //
    protected static Map<Request, Double> requestTimeMap;

    protected static int totalResponses; //返回的总响应数
    protected static int totalRequests;

    protected static double qps;

    protected static int failedRequests;
    protected static double failRate;

    protected static double requestsHandleTime;
    protected static double averageResponseTime;

    protected static long sloThreshold = 1000; // 1秒，可以根据实际情况调整;
    protected static double sloViolationRate;

    public static void calculateRequestStatistics(){

        totalRequests = requestList.size();

        qps = totalRequests / requestsHandleTime;

        failRate = ((double) failedRequests / totalRequests) * 100;

        calculateFailedRequests(requestList);

        calculateAverageResponseTime(requestList);

        calculateSloViolationRate(requestList);
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

    //计算平均响应时间
    public static void calculateAverageResponseTime(List<Request> requestsList) {
        assert !requestsList.isEmpty();

        long totalResponseTime = 0;
        for (Request request : requestsList) {
            totalResponseTime += (long) request.getResponseTime();
        }

//        averageResponseTime = (double) totalResponseTime /totalRequests;
        averageResponseTime = (double) totalTime /totalRequests;
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
