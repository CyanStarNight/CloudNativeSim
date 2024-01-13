/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.util;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.asciithemes.TA_GridThemes;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.nativesim.entity.NativeVm;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Pod;
import org.cloudbus.nativesim.entity.ServiceGraph;
import org.cloudbus.nativesim.network.Communication;
import org.cloudbus.nativesim.network.Request;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import static org.cloudbus.nativesim.Controller.*;

/**
 * Logger used for performing logging of the simulation process. It provides the ability to
 * substitute the output stream by any OutputStream subclass.
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 * @todo To add a method to print formatted text, such as the
 * {@link String#format(java.lang.String, java.lang.Object...)} method.
 */
public class NativeLog extends Log {

    private static String header_prefix = "============================";

    public static void printCloudletList(List<Cloudlet> list) {
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

    public static void printRequestStatistics(List<Request> requestList, double totalTime) {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);

        int totalRequests = requestList.size();
        int failedRequests = calculateFailedRequests(requestList);
        double qps = totalRequests / totalTime;
        double failRate = ((double) failedRequests / totalRequests) * 100;
        double averageResponseTime = calculateAverageResponseTime(requestList);
        double sloViolationRate = calculateSloViolationRate(requestList);

        DecimalFormat dft = new DecimalFormat("###.##");
        printLine();
        at.addRule();
        at.addRow("Metric", "Value");
        at.addRule();
        at.addRow("Total Time", totalTime);
        at.addRow("Total Requests", totalRequests);
        at.addRow("Failed Requests", failedRequests);
        at.addRow("QPS (Requests per Second)", dft.format(qps));
        at.addRow("Failure Rate", dft.format(failRate) + "%");
        at.addRow("Average Response Time", dft.format(averageResponseTime) + " seconds");
        at.addRow("SLO Violation Rate", dft.format(sloViolationRate) + "%");
        at.addRule();

        System.out.println(at.render());

        // For VM utilization, you can create another AsciiTable instance and print it similarly.
    }

    public void printAllCommunications(List<Communication> communications){
        System.out.println("Communications:"+ communications.toArray().length);
        for (Communication Communication:communications){
            System.out.println(" "+Communication.toString());
        }
    }

    public static void printServiceGraphDetails(ServiceGraph serviceGraph) {
        printLine();
        System.out.println(header_prefix+"Service Dependencies"+header_prefix);
        serviceGraph.getServices().forEach(System.out::println);
        printCriticalPath(serviceGraph.getCriticalPath(), serviceGraph.getTotalCriticalCost());
    }

    public static void printServiceGraph(ServiceGraph serviceGraph){
        printLine();
        System.out.println(header_prefix+"Service Dependencies"+header_prefix);
    }

    public static void printCriticalPath(LinkedList<Communication> criticalPath, double totalCriticalCost) {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);
        at.addRule();
        at.addRow("Critical Path", "Cost");
        at.addRule();

        for (Communication communication : criticalPath) {
            at.addRow(communication.getOriginName() + " -> " + communication.getDestName(),
                    String.format("%.3f", communication.getCost()));
        }

        at.addRule();
        at.addRow(null, "Total Critical Cost:"+ String.format("%.3f", totalCriticalCost));
        at.addRule();

        String rend = at.render();
        System.out.println(rend);
    }

//    public static void printStatisticsToCSV(List<Container> podList, List<Double> utilizationList) throws IOException {
//        FileWriter csvWriter = new FileWriter("request_statistics.csv");
//
//        // 写入标题行
//        csvWriter.append("Pod/Service,VM ID,Utilization,Other Col1,Other Col2,Other Col3,Other Col4\n");
//
//        for (Pod pod: podList) {
//            int i = podList.indexOf(pod);
//            NativeVm tmpVm = pod.getVm();
//            // 写入数据行
//            csvWriter.append(pod.getName()).append(",")
//                    .append(String.valueOf(tmpVm.getId())).append(",")
//                    .append(String.format("%.2f", utilizationList.get(i))).append(",")
//                    .append("0").append(",")
//                    .append("0.00").append(",")
//                    .append("0.00").append(",")
//                    .append("0.00").append("\n");
//        }
//
//        csvWriter.flush();
//        csvWriter.close();
//    }


}
