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
import java.util.List;
import java.util.Map;

import static core.Exporter.*;
import static org.cloudbus.cloudsim.Log.printLine;


@Getter
@Setter
public class Reporter {

    public static String outputPath;

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
        at.addRow("QPS ", dft.format(api.getAvgQps()));
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
//        at.addRow("Failure Rate", dft.format(failRate) + "%");
        at.addRow("QPS", dft.format(avgQps));
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
                        dft.format(api.getAvgQps())));
            }

            // Write overall statistics
            writer.write("Total,,,,\n");
            writer.write(String.format("Total Requests,%d,,,\n", totalRequests));
            writer.write(String.format("Average Delay (seconds),,%s,,\n", dft.format(totalDelay / totalRequests)));
            writer.write(String.format("Overall SLO Violation Rate,,,%s,\n", dft.format((double) sloViolations / totalRequests * 100) + "%"));
            writer.write(String.format("Average QPS,,,,%s\n", dft.format(avgQps)));
        }
    }


    // Method to print service dependencies
    public static void printGlobalDependencies(ServiceGraph serviceGraph) {
        printChain(serviceGraph, null);
    }

    public static void printChains(ServiceGraph serviceGraph, List<API> apiList) {
        for (API api : apiList) {
            Reporter.printChain(serviceGraph, api.getName());
        }
        System.out.println("\n──────────────────────────────────────────────────────────────────────────────");
    }

    public static void printChain(ServiceGraph serviceGraph, String API) {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);

        String header;
        if (API == null) header = "Graph Dependencies:";
        else header = "Chain For " + API + ":";

        at.addRule();
        at.addRow(header);
//        at.addRule();
        System.out.println(at.render());

        for (ChainNode root : serviceGraph.getRoots()) {
            // 递归
            printDependenciesFromNode(root, "  ", API);
        }
//        System.out.println("──────────────────────────────────────────────────────────────────────────────");
    }

    // Helper method to print dependencies starting from a given node
    public static void printDependenciesFromNode(ChainNode node, String indent, String API) {
        if (node == null) return;
        // Print the current service
        Service currentService = node.getService();
        if (currentService.getApiList().contains(API) || API == null) {
            System.out.println(indent + currentService);
            // Recursively print dependencies for each child (dependent service)
            for (ChainNode child : node.getChildren()) {
                printDependenciesFromNode(child, indent + "       |", API);
            }
        }
    }

    public static void printResourceUsage() {
        DecimalFormat df = new DecimalFormat("0.00");

        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.FULL);
//        at.getRenderer().setCWC(new CWC_LongestLine());

        // 打印标题
        at.addRule();
        at.addRow("Instance ID", "CPU Average", "RAM Average");  // 只显示平均值
        at.addRule();

        for (String instanceUid : usageOfCpuHistory.keySet()) {
            // 获取CPU和RAM的平均使用率
            double cpuAverage = getAverageUsage(usageOfCpuHistory.get(instanceUid));
            double ramAverage = getAverageUsage(usageOfRamHistory.get(instanceUid));
            double rbwAverage = getAverageUsage(usageOfReceiveBwHistory.get(instanceUid));
            double tbwAverage = getAverageUsage(usageOfTransmitBwHistory.get(instanceUid));

            // 添加行数据
            at.addRow(instanceUid, df.format(cpuAverage), df.format(ramAverage)
//                    , df.format(rbwAverage), df.format(tbwAverage)
            );
            at.addRule();
        }

        System.out.println(at.render());
    }

    private static double getAverageUsage(List<UsageData> usageDataList) {
        return usageDataList.stream().mapToDouble(UsageData::getUsage).average().orElse(0.0);
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
            writer.write("Instance ID,Max,Min,Average");

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
        writeUsage(usageOfReceiveBwHistory, "ReceiveBW", outputPath);
        writeUsage(usageOfTransmitBwHistory, "TransmitBW", outputPath);
    }
}
