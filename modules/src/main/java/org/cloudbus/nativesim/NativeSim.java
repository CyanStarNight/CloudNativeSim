/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.nativesim.entity.*;
import org.cloudbus.nativesim.event.NativeEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JingFeng Wu
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class NativeSim extends CloudSim {
    private static final String NativeSim_VERSION = "1.0";

    //Attention: use UIDs to identify entities
    private List<ServiceGraph> globalGraphs = new ArrayList<>();
    private List<Service> globalServices = new ArrayList<>();
    private List<Pod> globalPods = new ArrayList<>();
    private List<Communication> globalCommunications = new ArrayList<>();
    private List<NativeContainer> globalContainers = new ArrayList<>();

    public static List<NativeController> controllers = new ArrayList<>();//TODO: 2023/12/7 对于这些全局字段如何进行访问控制呢？
    public static List<NativeEvent> events = new ArrayList<>();

    public static void connectWithController(NativeEvent event,int userId){
        event.setController(controllers.stream().
                filter(u -> userId == u.getUserId()).
                findFirst().orElse(null));

    }

}
