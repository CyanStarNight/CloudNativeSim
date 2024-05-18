package org.cloudbus.nativesim;


import core.Exporter;
import core.Register;
import core.Generator;
import entity.Request;
import entity.ServiceGraph;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

public class TestFunctions {
    static String podsFile = "examples/src/sockshop/instances.yaml";
    static String servicesFile = "examples/src/sockshop/services.json";
    static int userId = 1;
    static Register register = new Register(userId,"Pod",servicesFile,podsFile);
    static ServiceGraph serviceGraph = register.registerServiceGraph();

    public static void generator_params(){
        Generator.finalClients = 1000;
        Generator.spawnRate = 10;
        Generator.waitTimeSpan = new int[]{3, 5};
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
        register.registerAPIs();
        System.out.println(Generator.APIs);
        System.out.println(Generator.timeLimit);
        Generator.initializeCumulativeWeights();
        double step = 1.0;

        String filePath = "modules/test/resource/";
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath+"requests"+"_"+Generator.finalClients+"_"+Generator.spawnRate+"_"+ Arrays.toString(Generator.waitTimeSpan) +".csv"))) {
            writer.write("Time,UserCount,RequestArrival,TotalRequests,QPS\n");

            for (double clock = 0; clock < Generator.timeLimit; clock += step) {
                List<Request> requestArrival = Generator.generateRequests(clock);
                Exporter.requestList.addAll(requestArrival);

                Exporter.updateQPSHistory(clock, requestArrival.size(), 1);

                int userCount = Generator.currentClients;
                // 在每隔1s打印的情况下,requestArrivalNum=qps
                int requestArrivalNum = requestArrival.size();
                int totalRequests = Exporter.requestList.size();
                double qps = Exporter.qps;

                writer.write(String.format("%.2f,%d,%d,%d,%.2f\n", clock, userCount, requestArrivalNum, totalRequests,qps));
            }
        }

        System.out.println("QPS History: " + Exporter.qpsHistory);
    }




    @Test
    public void testDispatcher() {
//
//        printChains(serviceGraph,null);
    }


}