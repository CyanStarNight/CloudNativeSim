package org.cloudbus.nativesim;


import core.Exporter;
import core.Register;
import core.Generator;
import entity.Request;
import entity.ServiceGraph;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestFunctions {
    static String podsFile = "examples/src/sockshop/instances.yaml";
    static String servicesFile = "examples/src/sockshop/services.json";
    static int userId = 1;
    static Register register = new Register(userId,"Pod",servicesFile,podsFile);
    static ServiceGraph serviceGraph = register.registerServiceGraph();

    public static void generator_params(){
        Generator.finalClients = 500;
        Generator.spawnRate = 10;
        Generator.waitTimeSpan = new int[]{5, 15};
        Generator.timeLimit = 600;
        Generator.meanLength = 1000;
        Generator.stdDev = 200;
    }

    @Test
    public void testRegister(){
        System.out.println(register.registerAPIs());
        generator_params();
        Generator.printGeneratorParameters();
    }

    @Test
    public void testGenerate() throws IOException {
        generator_params();
        System.out.println(register.registerAPIs());
        System.out.println(Generator.timeLimit);
        Generator.initializeCumulativeWeights();
        double step = 1.0;

        for (double clock = 0; clock < Generator.timeLimit; clock += step) {
            List<Request> requests = Generator.generateRequests(clock);
            Exporter.requestList.addAll(requests);
            Exporter.updateHistory(clock, requests.size());
        }

        System.out.println("QPS History: " + Exporter.qpsHistory);
//        System.out.println("Failed Requests History: " + Exporter.failedRequestsHistory);
//        System.out.println("Average Response Time History: " + Exporter.averageResponseTimeHistory);
//        System.out.println("SLO Violation Rate History: " + Exporter.sloViolationRateHistory);
    }

//        // 将数据写入文件
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter("generate_request_data.csv"))) {
//            writer.write("Time,UserCount,RequestCount\n");
//            for (int i = 0; i < requestCounts.size(); i++) {
//                writer.write(i + "," + userCounts.get(i) + "," + requestCounts.get(i) + "\n");
//            }
//        }



    @Test
    public void testDispatcher() {
//
//        printChains(serviceGraph,null);
    }


}