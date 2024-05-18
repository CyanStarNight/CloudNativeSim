/*
 * Copyright ©2024. Jingfeng Wu.
 */

package core;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.*;

import java.util.Calendar;

import static org.cloudbus.cloudsim.Log.printLine;

/**
 * @author JingFeng Wu
 */
@Getter
@Setter
public class CloudNativeSim extends CloudSim {
    private static final String NativeSim_VERSION_STRING  = "1.0";
    private static int cisId = -1;
    private static int shutdownId = -1;
    private static CloudInformationService cis = null;
    private static final int NOT_FOUND = -1;
    private static boolean traceFlag = false;
    private static Calendar calendar = null;


    public static double startSimulation() throws NullPointerException {
        printLine("Starting NativeSim" + "...");

        try {
            double clock = run();
            cisId = -1;
            shutdownId = -1;
            cis = null;
            calendar = null;
            traceFlag = false;
            return clock;
        } catch (IllegalArgumentException var2) {
            var2.printStackTrace();
            throw new NullPointerException("NativeSim.startSimulation() : Error - you haven't initialized NativeSim.");
        }
    }
//    public static double clock;
//    public static double clock() {
//        return clock;
//    }
//
//    public static double addClock(double time){
//        clock+=time;
//        return clock;
//    }

    public static void stopSimulation() throws NullPointerException {
        try {
            runStop();
        } catch (IllegalArgumentException var1) {
            throw new NullPointerException("NativeSim.stopCloudSimulation() : Error - can't stop Cloud Simulation.");
        }
    }


}
