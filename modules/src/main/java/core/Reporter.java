/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.asciithemes.TA_GridThemes;
import entity.*;
import extend.UsageData;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Cloudlet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static core.Exporter.*;
import static org.cloudbus.cloudsim.Log.printLine;


@Getter
@Setter
public class Reporter {

    public static String outputPath;

    public static String ifPrintNone(double num){
        DecimalFormat df = new DecimalFormat("0.00");
        if (num < 0 ) return "None";
        else return df.format(num);
    }

    public static void printHeader(String header) {
        String header_prefix = "============================";
        System.out.println("\n" + header_prefix + header + header_prefix + "\n");
    }

    public static void printPhase(String msg) {
        System.out.println("\n" + msg);
    }


    public static void printEvent(String msg) {

        System.out.printf("%.1f: ", CloudNativeSim.clock());
        printLine(msg);
    }

    public static void printCloudletList(List<? extends Cloudlet> list) {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getRenderer().setCWC((new CWC_LongestLine()));
        at.addRule();
        at.addRow("Cloudlet ID", "STATUS", "Datacenter ID", "VM ID", "Time", "Start Time", "Finish Time")
                .setPaddingLeftRight(2);

        at.addRule();

        DecimalFormat dft = new DecimalFormat("###.##");
        for (Cloudlet cloudlet : list) {
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                at.addRow(cloudlet.getCloudletId(),
                        "SUCCESS",
                        cloudlet.getResourceId(),
                        cloudlet.getVmId(),
                        dft.format(cloudlet.getActualCPUTime()),
                        dft.format(cloudlet.getExecStartTime()),
                        dft.format(cloudlet.getFinishTime()));
                at.addRule();
            }
        }

        System.out.println(at.render());
    }

    public static void printApiStatistics(API api) {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);
//        at.getRenderer().setCWC(new CWC_LongestLine());

        int numRequests = api.getRequests().size();
        DecimalFormat dft = new DecimalFormat("###.##");
        printLine();
        at.addRule();
        at.addRow("API Metrics For " + api.getName(), "Value");
        at.addRule();
//        at.addRow("Total Time", totalTime);
        at.addRow("Total Requests ", numRequests);
//        at.addRow("Failed Requests", failedRequests);
//        at.addRow("Failure Rate", dft.format(failRate) + "%");
        at.addRow("RPS ", dft.format(api.getAvgRps()));
        at.addRow("Average Delay ", dft.format(api.getAverageDelay()) + " seconds");
        at.addRow("SLO Violations Rate   ", dft.format((double) api.getSloViolations() / numRequests * 100) + "%");
        at.addRule();

        System.out.println(at.render());
    }

    public static void printApiStatistics(List<API> apis) {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);
//        at.getRenderer().setCWC(new CWC_LongestLine());
        Exporter.getApiStatistics(apis);
        DecimalFormat dft = new DecimalFormat("###.##");
        printLine();
        at.addRule();
        at.addRow("API Metrics", "Value");
        at.addRule();
//        at.addRow("Total Time", CloudNativeSim.clock());
        at.addRow("Total Requests", totalRequests);
//        at.addRow("Failed Requests", failedRequests);
        at.addRow("Failure Rate", dft.format(failedRate) + "%");
        at.addRow("RPS", dft.format(avgRps));
        at.addRow("Average Delay", dft.format(totalDelay / totalRequests) + " seconds");
        at.addRow("SLO Violation Rate", dft.format((double) sloViolations / totalRequests * 100) + "%");
        at.addRule();

        System.out.println(at.render());
    }

    public static void writeStatisticsToCsv(List<API> apis, String filePath) throws IOException {
        String fileName = "API_Statistics" + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + fileName))) {
            // Write the header
            writer.write("API Name,Total Requests,Average Delay (seconds),SLO Violation Rate,QPS\n");

            // Variables to sum for overall statistics
            Exporter.getApiStatistics(apis);

            DecimalFormat dft = new DecimalFormat("###.##");

            // Write data for each API
            for (API api : apis) {
                // Write each API's statistics
                writer.write(String.format("%s,%d,%s,%s,%s\n",
                        api.getName(),
                        api.getRequests().size(),
                        dft.format(api.getAverageDelay()),
                        dft.format((double) api.getSloViolations() / api.getRequests().size() * 100) + "%",
                        dft.format(api.getAvgRps())));
            }

            // Write overall statistics
            writer.write(String.format("Aggregate,%s,%s,%s,%s\n", totalRequests
                    , dft.format(totalDelay / totalRequests)
                    , dft.format((double) sloViolations / totalRequests * 100) + "%"
                    , dft.format(avgRps)));
        }
    }


    // Method to print service dependencies
    public static void printGlobalDependencies(ServiceGraph serviceGraph) {
        printChain(serviceGraph, null);
    }

    // Method to print chains for given APIs
    public static void printChains(ServiceGraph serviceGraph, List<API> apiList) {
        for (API api : apiList) {
            Reporter.printChain(serviceGraph, api.getName());
        }
        System.out.println("\n──────────────────────────────────────────────────────────────────────────────");
    }

    // Method to print a specific chain for a given API
    public static void printChain(ServiceGraph serviceGraph, String apiName) {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);

        String header;
        if (apiName == null) {
            header = "Graph Dependencies:";
        } else {
            header = "Chain For " + apiName + ":";
        }

        at.addRule();
        at.addRow(header);
        System.out.println(at.render());

        for (Service root : serviceGraph.getSources(new ArrayList<>(serviceGraph.getServiceChains().get(new API(apiName, null))))) {
            // Recursively print dependencies starting from each root node
            printDependenciesFromNode(serviceGraph, root, "  ", apiName);
        }
    }

    // Helper method to print dependencies starting from a given node
    public static void printDependenciesFromNode(ServiceGraph serviceGraph, Service node, String indent, String apiName) {
        if (node == null) return;
        // Print the current service
        if (node.getApiList().contains(apiName) || apiName == null) {
            System.out.println(indent + node.getName());
            // Recursively print dependencies for each child (dependent service)
            for (Service child : serviceGraph.getCalls(node)) {
                printDependenciesFromNode(serviceGraph, child, indent + "       |", apiName);
            }
        }
    }

    public static void printResourceUsage() {

        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.FULL);
//        at.getRenderer().setCWC(new CWC_LongestLine());

        // 打印标题
        at.addRule();
        at.addRow(" Instance Name", "CPU Usage Average", "RAM Usage Average");  // 只显示平均值
        at.addRule();


        for (Instance instance : finalInstanceList) {

            String instanceUid = instance.getUid();
            // 获取CPU和RAM的平均使用率
            double cpuAverage = getAverageUsage(usageOfCpuHistory.get(instanceUid));
            double ramAverage = getAverageUsage(usageOfRamHistory.get(instanceUid));
//            double rbwAverage = getAverageUsage(usageOfReceiveBwHistory.get(instanceUid));
//            double tbwAverage = getAverageUsage(usageOfTransmitBwHistory.get(instanceUid));
            // 添加行数据
            at.addRow(instance.getName(), ifPrintNone(cpuAverage), ifPrintNone(ramAverage)
//                    , ifPrintNone(rbwAverage), ifPrintNone(tbwAverage)
            );
            at.addRule();
        }

        System.out.println(at.render());
    }



    // 计算指定instance的平均利用率：因为用量历史是随着stage更新的，计算平均值需要乘以时间再除以总时长
    private static double getAverageUsage(List<UsageData> usageDataList) {
        // 代表这个实例从未被使用过
        if(usageDataList == null || usageDataList.isEmpty()) return -1;

        double sum = 0;
        double totalSession = 0;
        for (UsageData data : usageDataList) {
            sum += data.getSession() * data.getUsage();
            totalSession += data.getSession();
        }

        UsageData last = usageDataList.get(usageDataList.size() - 1);
        UsageData first = usageDataList.get(0);
        double processSession = last.getTimestamp() + last.getSession() - first.getTimestamp();
        return sum / processSession;
//        return sum / usageDataList.size();
    }


    private static void addResourceUsageRow(AsciiTable at, String instanceId, String resourceType, List<UsageData> usageDataList, DecimalFormat df) {
        double max = usageDataList.stream().mapToDouble(UsageData::getUsage).max().orElse(0.0);
        double min = usageDataList.stream().mapToDouble(UsageData::getUsage).min().orElse(0.0);
        double average = usageDataList.stream().mapToDouble(UsageData::getUsage).average().orElse(0.0);

        at.addRow(instanceId, resourceType, df.format(max), df.format(min), df.format(average));
    }


    private static void writeUsage(Map<String, List<UsageData>> usageHistory, String resourceType, String outputPath) throws IOException {
        DecimalFormat df = new DecimalFormat("0.00");
        String fileName = outputPath + resourceType + "_Usage_History.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // 构建并写入标题行
            writer.write("Instance ID,Max,Min,Average");
            if (!usageHistory.isEmpty()) {
                List<UsageData> firstList = usageHistory.values().iterator().next();
                for (int i = 1; i <= firstList.size(); i++) {
                    writer.write("," + (i * schedulingInterval));
                }
            }

            // 写入每个实例的数据
            for (Map.Entry<String, List<UsageData>> entry : usageHistory.entrySet()) {
                String instanceId = entry.getKey();
                List<UsageData> history = entry.getValue();
                double max = history.stream().mapToDouble(UsageData::getUsage).max().orElse(0.0);
                double min = history.stream().mapToDouble(UsageData::getUsage).min().orElse(0.0);
                double average = history.stream().mapToDouble(UsageData::getUsage).average().orElse(0.0);

                writer.write(String.format("\n%s,%s,%s,%s", instanceId, df.format(max), df.format(min), df.format(average)));

                // 写入实际的每个时间点的数据
                for (UsageData data : history) {
                    writer.write("," + df.format(data.getUsage()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV for " + resourceType + ": " + e.getMessage());
            throw e;  // 把异常再次抛出以确保调用者知晓发生了错误
        }
    }


    public static void writeResourceUsageToCSV(String outputPath) throws IOException {
        writeUsage(usageOfCpuHistory, "CPU", outputPath);
        writeUsage(usageOfRamHistory, "RAM", outputPath);
//        writeUsage(usageOfReceiveBwHistory, "ReceiveBW", outputPath);
//        writeUsage(usageOfTransmitBwHistory, "TransmitBW", outputPath);
    }
}
