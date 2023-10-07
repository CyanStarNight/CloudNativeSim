package org.cloudbus.nativesim.util;

import lombok.Data;

/**
 * @author JingFeng Wu
 */
@Data
public class Vertex {
    String name;
    Edge firstIn;
    Edge firstOut;
    /**
     * in-degree
     */
    int in_degree;
    /**
     * out-degree
     */
    int out_degree;
    /**
     * etv: earliest time of vertex, ltv: latest time of vertex.(measured in microseconds.)
     */
    int etv, ltv; //etv(j) = max{etv(i)+cost[i][j]}; ltv(i) = max{ltv(j)-cost[i][j]} (use reverse topology sort);

    public Vertex(String name) {
        this.name = name;
    }

    public Vertex() {
    }

    public void buildIn_degree() {
        if (this.firstIn != null) {
            Edge edge = this.firstIn;
            this.in_degree++;
            while (edge.hLink != null) {
                edge = edge.hLink;
                this.in_degree++;
            }
        }
    }

    public void buildOut_degree() {
        if (this.firstOut != null) {
            Edge edge = this.firstOut;
            this.out_degree++;
            while (edge.tLink != null) {
                edge = edge.tLink;
                this.out_degree++;
            }
        }
    }

    public String getName() {
        return name;
    }
}
