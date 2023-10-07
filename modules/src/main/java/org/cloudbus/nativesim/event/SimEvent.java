package org.cloudbus.nativesim.event;

import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Pod;
import org.cloudbus.nativesim.entity.Service;
import org.cloudbus.nativesim.entity.ServiceChain;

import java.util.List;

/**
 * @author JingFeng Wu
 */

public class SimEvent {
    ServiceChain serviceChain;
//    List<Edge> communications;
    List<Service> services;
    List<Pod> pods;
    List<Container> containers;

    public SimEvent(){}

}