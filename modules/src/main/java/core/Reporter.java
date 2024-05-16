/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import de.vandermeer.asciithemes.TA_GridThemes;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Cloudlet;
import entity.Service;
import entity.ChainNode;

import java.text.DecimalFormat;
import java.util.List;

import static org.cloudbus.cloudsim.Log.printLine;
import static core.Exporter.*;

@Getter
@Setter
public class Reporter{

    public static void printHeader(String header){
        String header_prefix = "============================";
        System.out.println("\n"+ header_prefix +header+ header_prefix +"\n");
    }

    public static void printPhase(String msg){
        System.out.println("\n"+msg);
    }


    public static void printEvent(String msg){

        System.out.printf("%.1f: ", NativeSim.clock());
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

    public static void printRequestStatistics() {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);

        DecimalFormat dft = new DecimalFormat("###.##");
        printLine();
        at.addRule();
        at.addRow("APP Metrics", "Value");
        at.addRule();
        at.addRow("Total Time", totalTime);
        at.addRow("Total Requests", totalRequests);
//        at.addRow("Failed Requests", failedRequests);
//        at.addRow("QPS (Requests per Second)", dft.format(qps));
        at.addRow("Failure Rate", dft.format(failRate) + "%");
        at.addRow("Average Response Time", dft.format(averageResponseTime) + " seconds");
        at.addRow("SLO Violation Rate", dft.format(sloViolationRate) + "%");
        at.addRule();

        System.out.println(at.render());

        // For VM utilization, you can create another AsciiTable instance and print it similarly.
    }


    // Method to print service dependencies
    public static void printServiceGraph() {
        printChains(null);
    }

    public static void printChains(String API) {
        printLine();
        AsciiTable at = new AsciiTable();
        at.getContext().setGridTheme(TA_GridThemes.HORIZONTAL);

        String header;
        if (API == null) header = "Service Graph Dependencies:";
        else header = "Service Chain For "+API;

        at.addRule();
        at.addRow(header);
        at.addRule();
        System.out.println(at.render());

        for (ChainNode root : serviceGraph.getRoots()) {
            printDependenciesFromNode(root, " ",API);
        }
        System.out.println("──────────────────────────────────────────────────────────────────────────────");
    }

    // Helper method to print dependencies starting from a given node
    public static void printDependenciesFromNode(ChainNode node, String indent, String API) {
        if (node == null) return;
        // Print the current service
        Service currentService = node.getService();
        if (currentService.getApiList().contains(API) || API == null) {
            System.out.println(indent + currentService + " - out-degree: " + node.getChildren().size());

            // Recursively print dependencies for each child (dependent service)
            for (ChainNode child : node.getChildren()) {
                printDependenciesFromNode(child, indent + "       |", API);
            }
        }
    }

    public static void printResourceUsage(){

    }

}