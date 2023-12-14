package org.cloudbus.nativesim;

import lombok.Data;
import org.cloudbus.nativesim.entity.Service;
import org.junit.Test;
import org.cloudbus.nativesim.util.*;
import java.util.*;

/**
 * @author JingFeng Wu
 */
@Data
public class ServiceGraphTest {
    String configFileName ="modules/test/resource/sockshop-complete-demo.yaml";
    String communicationFileName = "modules/test/resource/communication.yaml";
    List<String> deployment=null;
    String[] names = {
            "uniqueID","nginx",
//            "nginx", "uniqueID",
            "video", "text", "userTag",
            "composePost", "writeTimeline","memcached","mongoDB"};
    int len = names.length;
    int[][] matrix = {
//            {0, 1, 1, 1, 1, 0, 0, 0, 0},
//            {0, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0, 0},
            {1, 0, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 2, 0, 0, 0},
            {0, 0, 0, 0, 0, 4, 0, 0, 0},
            {0, 0, 0, 0, 0, 3, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 3},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}};
    @Test
    public void test_Matrix_to_Graph() throws Exception {

        Graph graph = new Graph(matrix, names);
        graph.printCriticalPath();
        System.out.println();
        graph.printAllNodeOut();
//        graph.printAllNodeIn();
//        graph.printAllEdges();
    }

    @Test
    public void test_ServiceChain(){
        List<Service> services = new ArrayList<>();
        for (int i = 0 ;i < len ; i++){
            Service service = new Service();
            service.getRealtime_resources().setMips(1000);
            service.getRealtime_resources().setBw(100);
            service.getRealtime_resources().setRam(1024);
            service.setName(names[i]);
            service.setId(i);
            service.setNumReplicas(i);
            services.add(service);
        }
    }
}

