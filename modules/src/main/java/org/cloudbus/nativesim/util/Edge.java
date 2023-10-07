package org.cloudbus.nativesim.util;

import lombok.Data;

/**
 * @author JingFeng Wu
 * Use orthogonalList to describe the graph, it's convient to get the in-degree and out-degree.
 */
@Data
public class Edge {
    /**
     * from tail vertex to head vertex.
     */
    Vertex tailVec;
    Vertex headVec;
    /**
     * the communication cost of this edge.
     */
    int cost = 0;
    /**
     * the edge which has the same head vertex.
     */
    Edge hLink;
    /**
     * the edge which has the same tail vertex.
     */
    Edge tLink;
    /**
     * ete: earliest time of edge, lte: latest time of edge.(measured in microseconds.)
     */
    int ete, lte; //etv:earliest time of vertex; ltv:latest time of vertex.(measured in microseconds)

    public Edge() {
    }

    @Override
    public String toString() {
        return tailVec.getName() +
                "===>" + headVec.getName() +
                " (" + cost + ")";
    }
}
