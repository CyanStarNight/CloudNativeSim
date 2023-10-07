package org.cloudbus.nativesim.entity;

import lombok.Data;
import org.cloudbus.nativesim.util.Edge;
import org.cloudbus.nativesim.util.Graph;
import org.cloudbus.nativesim.util.Vertex;

import java.util.*;

/**
 * @author JingFeng Wu
 * @describe the service chains with orthogonalList and maintain the critical path.
 */
@Data
public class ServiceChain extends Graph {

    public List<Vertex> services; // the entity of communications.
    List<Pod> pods; // the entity of executions.
    public List<Edge> communications;

    public ServiceChain(){

    }

    // TODO: 2023/9/27 updateServiceChain: topologicalSort

    public void setServices(List<Vertex> services) {
        super.setVertices(services);
        this.services = services;
    }

    public void setCommunications(List<Edge> communications) { // TODO: 2023/10/7 重复调用setCommu 会产生问题
        super.setEdges(communications);
        this.communications = communications;
        super.buildDegree();
        super.setCriticalPath(findCriticalPath());

    }

    public void printServiceChain() {
        super.printAllNodeOut();
        super.printCriticalPath();
    }
}
