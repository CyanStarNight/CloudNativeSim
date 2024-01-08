/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.core.predicates.Predicate;
import org.cloudbus.cloudsim.core.predicates.PredicateAny;
import org.cloudbus.cloudsim.core.predicates.PredicateNone;
import org.cloudbus.nativesim.network.Communication;
import org.cloudbus.nativesim.entity.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author JingFeng Wu
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class NativeSim extends CloudSim {
    private static final String NativeSim_VERSION_STRING  = "1.0";
    private static int cisId = -1;
    private static int shutdownId = -1;
    private static CloudInformationService cis = null;
    private static final int NOT_FOUND = -1;
    private static boolean traceFlag = false;
    private static Calendar calendar = null;
    private static double terminateAt = -1.0;
    private static double minTimeBetweenEvents = 0.1;
    private static List<SimEntity> entities;
    protected static FutureQueue future;
    protected static DeferredQueue deferred;
    private static double clock;
    private static boolean running;
    private static Map<String, SimEntity> entitiesByName;
    private static Map<Integer, Predicate> waitPredicates;
    private static boolean paused = false;
    private static long pauseAt = -1L;
    private static boolean abruptTerminate = false;
    public static final PredicateAny SIM_ANY = new PredicateAny();
    public static final PredicateNone SIM_NONE = new PredicateNone();


    //Attention: use UIDs to identify entities
    private List<ServiceGraph> globalGraphs = new ArrayList<>();
    private List<Service> globalServices = new ArrayList<>();
    private List<Pod> globalPods = new ArrayList<>();
    private List<Communication> globalCommunications = new ArrayList<>();
    private List<Container> globalContainers = new ArrayList<>();

    public static List<Controller> controllers = new ArrayList<>();//TODO: 2023/12/7 对于这些全局字段如何进行访问控制呢？

    public static double startSimulation() throws NullPointerException {
        Log.printLine("Starting NativeSim version" + NativeSim_VERSION_STRING);

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
            throw new NullPointerException("NativeSim.startCloudSimulation() : Error - you haven't initialized NativeSim.");
        }
    }



}
