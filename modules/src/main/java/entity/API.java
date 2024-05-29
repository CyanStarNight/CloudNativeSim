package entity;

import core.Status;
import lombok.Data;
import java.util.*;

/**
 * API类，用于管理API及其统计数据。
 */
@Data
public class API {
    public String name;
    public List<Request> requests = new ArrayList<>();
    public static Map<String, API> apiMap = new HashMap<>();
    public String method;
    public String url;
    public double weight = 1.0;
    protected List<Service> chain = new ArrayList<>();
    public double failedNum;
    public double sloThreshold = 7.0;
    // qps history
    public List<Double> qpsHistory = new ArrayList<>();

    public API(String name) {
        validateName(name);
        this.name = name;
        apiMap.put(name, this);
    }

    public API(String name, double weight) {
        this(name);
        this.weight = weight;
        apiMap.put(name, this);
    }

    public API(String method, String url) {
        validateMethodAndUrl(method, url);
        this.method = method;
        this.url = url;
        this.name = method + " " + url;
        apiMap.put(name, this);
    }

    public API(String method, String url, double weight) {
        this(method, url);
        this.weight = weight;
        apiMap.put(name, this);
    }

    public void updateQPSHistory(double clock, int requestCount, int requestInterval) {
        double qps = (double) requestCount / requestInterval;
        qpsHistory.add(qps);
    }

    public double getAvgQps() {
        return qpsHistory.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public double getAverageDelay() {
        return getRequests().stream()
                .mapToDouble(Request::getDelay)
                .average()
                .orElse(0.0);
    }

    // Method to calculate the SLO violation rate
    public int getSloViolations() {
        return (int)getRequests().stream()
                .filter(request -> request.getDelay() >= sloThreshold)
                .count();
    }

    public int getFailedNum() {
        return (int)getRequests().stream()
                .filter(request -> request.getStatus() == Status.Failed)
                .count();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("API name cannot be null or empty");
        }
    }

    private void validateMethodAndUrl(String method, String url) {
        if (method == null || url == null || method.trim().isEmpty() || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Method and URL cannot be null or empty");
        }
    }
}