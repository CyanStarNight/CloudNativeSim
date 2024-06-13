package core;

import entity.API;
import entity.Request;
import lombok.Data;

import java.util.*;

@Data
public class Generator {
    public double previousTime;
    // final user number
    public int finalClients = -1;
    // current user number
    public int currentClients = -1;
    // clients waiting to generate requests
    public List<Integer> clientsWaitingStatus = new ArrayList<>();
    // 用户增长速率
    public int spawnRate;
    // 请求间隔
    public int[] waitTimeSpan = new int[]{5, 15};
    // 指定rps生成请求
    public int finalRps = -1;
    // 指定生成方式
    public String generatorType = "Default";
    // 请求生成的时间限制,默认无限制
    public double timeLimit = Integer.MAX_VALUE;
    // 请求生成数量的限制,默认无限制
    public int numLimit = Integer.MAX_VALUE;
    // API列表,每个API对象创建时会自动加入. 每个API有自己的权重
    public List<API> APIs = new ArrayList<>();
    // Cloudlet的全局参数定义，下面两种表述是等价的(4B的倍数关系)
    // 单位是百万条指令(M)
    public int meanLength;
    public int stdDevLength;
    // 单位是MB
    public float meanSize;
    public float stdDevSize;
    // Store cumulative weights for optimized API selection
    public double[] cumulativeWeights;
    // 随即种子
    public Random random;

    public Generator(int finalClients, int spawnRate, int[] waitTimeSpan) {
        this.finalClients = finalClients;
        this.currentClients = 0;
        this.spawnRate = spawnRate;
        this.waitTimeSpan = waitTimeSpan;
    }

    public Generator(int finalClients, int spawnRate, int[] waitTimeSpan, int timeLimit) {
        this.finalClients = finalClients;
        this.currentClients = 0;
        this.spawnRate = spawnRate;
        this.waitTimeSpan = waitTimeSpan;
        this.timeLimit = timeLimit;
    }

    /* 全参数*/
    public Generator(List<API> APIs, int finalClients, int spawnRate, int[] waitTimeSpan, int timeLimit, int meanLength, int stdDevLength) {
        this.finalClients = finalClients;
        this.spawnRate = spawnRate;
        this.waitTimeSpan = waitTimeSpan;
        this.timeLimit = timeLimit;
        this.APIs = APIs;
        this.meanLength = meanLength;
        this.stdDevLength = stdDevLength;
        this.meanSize = meanLength*4;
        this.stdDevSize = stdDevLength*4;
        random = new Random();
        initializeCumulativeWeights();
    }

    public Generator(List<API> APIs, int finalRps, int timeLimit, float meanSize, float stdDevSize) {
        this.finalRps = finalRps;
        this.generatorType = "ByRps";
        this.timeLimit = timeLimit;
        this.APIs = APIs;
        this.meanSize = meanSize;
        this.stdDevSize = stdDevSize;
        this.meanLength = (int) meanSize/4;
        this.stdDevLength = (int) stdDevSize/4;
        random = new Random();
        initializeCumulativeWeights();
    }

    public int generateCloudletLength() {
        int length;
        do {
            length = (int) (meanLength + random.nextGaussian() * stdDevLength);
        } while (length <= 0);
        return length;
    }


    // Initialize cumulative weights
    public void initializeCumulativeWeights() {
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

    public List<Request> generateRequests(double clock) {
        // 判断选择哪个请求生成方式
        switch (generatorType){
            case "ByRps":
                return generateRequestsWithRps(clock);
            default:
                return generateRequestsWithClients(clock);
        }
    }

    public List<Request> generateRequestsWithClients(double clock) {
        assert finalClients > 0;
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

    public List<Request> generateRequestsWithRps(double clock) {
        assert finalRps > 0;
        List<Request> generateList = new ArrayList<>();
        // 整数时间间隔
        double gap = clock - previousTime;
        // generate requests
        for (int i = 0; i < gap * finalRps; i++) {
            // 根据API权重随机选择一个API
            API selectedAPI = getRandomAPI();
            assert selectedAPI != null;
            // 创建新的请求
            Request newRequest = new Request(selectedAPI, clock);
            generateList.add(newRequest);
        }

        // 更新previous time
        previousTime = clock;
        return generateList;
    }


    public API getRandomAPI() {
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


    public void printGeneratorParameters() {
        System.out.println("// Final user number");
        System.out.println("public int finalClients = " + finalClients + ";");
        System.out.println("// Current user number");
        System.out.println("public int currentClients = " + currentClients + ";");
        System.out.println("// Spawn rate");
        System.out.println("public int spawnRate = " + spawnRate + ";");
        System.out.println("// Request interval");
        System.out.println("public int[] waitTime = {" + waitTimeSpan[0] + ", " + waitTimeSpan[1] + "};");
        System.out.println("// Request generation time limit");
        System.out.println("public int timeLimit = " + timeLimit + ";");
        System.out.println("// API list");
        System.out.println("public List<API> APIs = " + APIs + ";");
        System.out.println("// Random seed");
        System.out.println("public Random random = new Random();");
        System.out.println("// Cloudlet global parameters, in KB");
        System.out.println("public int meanLength = " + meanLength + ";");
        System.out.println("public int stdDevLength = " + stdDevLength + ";");
    }


}
