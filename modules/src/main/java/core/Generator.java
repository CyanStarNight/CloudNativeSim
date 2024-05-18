/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;

import entity.API;
import entity.Request;
import lombok.Data;

import java.util.*;

@Data
public class Generator {
    public static double previousTime;
    // final user number
    public static int finalClients;
    // current user number
    public static int currentClients;
    // clients waiting to generate requests
    public static List<Integer> clientsWaitingStatus = new ArrayList<>();
    // 用户增长速率
    public static int spawnRate;
    // 请求间隔
    public static int[] waitTimeSpan;
    // 请求生成的时间限制
    public static int timeLimit;
    // API列表,每个API对象创建时会自动加入. 每个API有自己的权重
    public static List<API> APIs = new ArrayList<>();
    // 随即种子
    public static Random random = new Random();
    // Cloudlet的全局参数定义，单位为KB
    public static int meanLength;
    public static int stdDev;

    // Store cumulative weights for optimized API selection
    private static double[] cumulativeWeights;

    // Initialize cumulative weights
    public static void initializeCumulativeWeights() {
        if (APIs.isEmpty()) {
            throw new IllegalStateException("APIs list is empty. Please initialize the APIs list before using the Generator.");
        }
        double totalWeight = 0.0;
        cumulativeWeights = new double[APIs.size()];
        for (int i = 0; i < APIs.size(); i++) {
            totalWeight += APIs.get(i).getWeight();
            cumulativeWeights[i] = totalWeight;
        }
    }

    public static List<Request> generateRequests(double clock) {
        List<Request> generateList = new ArrayList<>();
        // 整数时间间隔
        int gap = (int) (clock - previousTime);

        // 判定是否增长clients
        if (currentClients < finalClients) {
            // spawn clients num
            int newClients = spawnRate * gap;
            // truncate
            newClients = Math.min(newClients, finalClients - currentClients);
            // add newClients to waiting list
            for (int i = 0; i < newClients; i++) {
                clientsWaitingStatus.add(0); // 新客户的等待时间都为0
            }
            // update currentClients
            currentClients += newClients;
        }

        // generate requests
        for (int i = 0; i < clientsWaitingStatus.size(); i++) {
            int waitStatus = clientsWaitingStatus.get(i);
            // 如果这个客户可以开始生成请求了
            if (waitStatus - gap <= 0) {
                // 根据API权重随机选择一个API
                API selectedAPI = getRandomAPI();
                assert selectedAPI != null;
                // 创建新的请求
                Request newRequest = new Request(selectedAPI, clock);
                generateList.add(newRequest);

                // 进入随机等待,等待时间在waitTime区间内
                int waitTime = random.nextInt(waitTimeSpan[1] - waitTimeSpan[0]) + waitTimeSpan[0];
                clientsWaitingStatus.set(i, waitTime);
            } else {
                // 如果这个客户还没开始生成请求,等待时长减gap
                clientsWaitingStatus.set(i, waitStatus - gap);
            }
        }

        // 更新previous time
        previousTime = clock;
        return generateList;
    }

    private static API getRandomAPI() {
        // 生成一个随机数 randomWeight，范围是 [0, totalWeight)
        double randomWeight = random.nextDouble() * cumulativeWeights[cumulativeWeights.length - 1];
        // 使用线性查找来选择API
        for (int i = 0; i < cumulativeWeights.length; i++) {
            if (randomWeight < cumulativeWeights[i]) {
                return APIs.get(i);
            }
        }
        return APIs.get(APIs.size() - 1); // fallback, should not reach here
    }


    public static int generateCloudletLength() {
        // 创建 Random 实例
        Random random = new Random();
        // 生成正态分布的 Cloudlet 长度
        return (int) (meanLength + random.nextGaussian() * stdDev);
    }

    public static void printGeneratorParameters() {
        System.out.println("// Final user number");
        System.out.println("public static int finalClients = " + finalClients + ";");
        System.out.println("// Current user number");
        System.out.println("public static int currentClients = " + currentClients + ";");
        System.out.println("// Spawn rate");
        System.out.println("public static double spawnRate = " + spawnRate + ";");
        System.out.println("// Request interval");
        System.out.println("public static int[] waitTime = {" + waitTimeSpan[0] + ", " + waitTimeSpan[1] + "};");
        System.out.println("// Request generation time limit");
        System.out.println("public static int timeLimit = " + timeLimit + ";");
        System.out.println("// API list");
        System.out.println("public static List<API> APIs = " + APIs + ";");
        System.out.println("// Random seed");
        System.out.println("public static Random random = new Random();");
        System.out.println("// Cloudlet global parameters, in KB");
        System.out.println("public static int meanLength = " + meanLength + ";");
        System.out.println("public static int stdDev = " + stdDev + ";");
    }


}
