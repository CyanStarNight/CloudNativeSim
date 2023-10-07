package org.cloudbus.nativesim;

import org.cloudbus.nativesim.event.NativeSim;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author JingFeng Wu
 */

public class SockShopExample{

    public static void main(String[] args) {
        // Step 1: Init the simulation and register the inputs and policies.
        int num_user = 1;
        Calendar calendar = Calendar.getInstance();
        NativeSim simulation = new NativeSim();
        simulation.init(num_user,calendar);

        String config_file = "modules/test/resource/sockshop-complete-demo.yaml";
        String commu_file = "modules/test/resource/communication.yaml";
        int[] costs = {1,2,3,4,5,6,7,8,9,10,11,12,13,14};
        List<String> deployment=null;
        simulation.register(config_file,commu_file,deployment,costs);
        simulation.getServiceChain().printServiceChain();
        // Step 2: Start the simulation with some defined parameters.


        // Sep 3: Print the log.
    }
}
