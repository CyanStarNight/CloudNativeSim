package org.cloudbus.nativesim;

import request.AppInterface;
import request.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RequestSimulator {
    private int num;
    private double growthRate;
    private AppInterface[] interfaces;
    private double interval;
    private long timeLimit;
    private int currentRequestId = 0;
    private Random random = new Random();

    public RequestSimulator(int num, double growthRate, AppInterface[] interfaces, double interval, long timeLimit) {
        this.num = num;
        this.growthRate = growthRate;
        this.interfaces = interfaces;
        this.interval = interval;
        this.timeLimit = timeLimit;
    }

    public List<Request> generateRequests() {
        List<Request> requests = new ArrayList<>();
        double avgNum = num / 2.0;
        double sumWeights = 0.0;
        for (AppInterface iface : interfaces) {
            sumWeights += iface.getWeight();
        }
        double lambda = sumWeights * avgNum * (1 / interval);
        long currentTime = 0;

        while (currentTime < timeLimit) {
            double interArrivalTime = -Math.log(1.0 - random.nextDouble()) / lambda;
            currentTime += interArrivalTime * 1000;  // Convert to milliseconds
            if (currentTime < timeLimit) {
                AppInterface requestInterface = interfaces[random.nextInt(interfaces.length)];
                Request request = new Request(currentRequestId++, requestInterface.API, currentTime);
                requests.add(request);
            }
        }

        return requests;
    }

    public static void main(String[] args) {
        AppInterface[] interfaces = {
                new AppInterface("Interface1", 0.5),
                new AppInterface("Interface2", 1.0),
                new AppInterface("Interface3", 1.5)
        };
        RequestSimulator simulator = new RequestSimulator(100, 1.0, interfaces, 1.0, 3600 * 1000);
        List<Request> requests = simulator.generateRequests();

        for (Request request : requests) {
            System.out.println("Request ID: " + request.getId() +
                    ", Interface: " + request.getPort().getAPI() +
                    ", Weight: " + request.getPort().getWeight() +
                    ", Timestamp: " + request.getStartTime());
        }
    }
}
