package org.cloudbus.nativesim;


import org.cloudbus.nativesim.core.Register;
import org.cloudbus.nativesim.request.Request;
import org.cloudbus.nativesim.service.ServiceGraph;
import org.junit.Test;

import java.util.List;

import static org.cloudbus.nativesim.core.Reporter.printChains;

public class TestFunctions {

    static String podsFile = "examples/src/org/resource/sockshop/instances.yaml";
    static String servicesFile = "examples/src/org/resource/sockshop/services.json";
    static String requestsFile = "examples/src/org/resource/sockshop/requests.json";
    static int userId = 1;
    static Register register = new Register(userId,"Pod",servicesFile,podsFile,requestsFile);
    static ServiceGraph serviceGraph = register.registerServiceGraph();
    static List<Request> requestsList = register.registerRequests();

    @Test
    public void testDispatcher() {
//
//        printChains(serviceGraph,null);
    }

    @Test
    public void testLoadBalance() {

    }


}