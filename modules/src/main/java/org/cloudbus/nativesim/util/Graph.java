package org.cloudbus.nativesim.util;

import lombok.Data;

import java.util.List;
import java.util.*;

@Data
public class Graph{
    // TODO: 2023/7/21 支持矩阵和十字链表相互转换
    int[][] matrix;
    List<Vertex> vertices;
    List<Edge> edges;
    int num_vertices,num_edges;
    LinkedList<Edge> CriticalPath;
    int totalCriticalCost;

    public Graph(int[][] matrix,String[] names) throws Exception {
        buildOrthogonalListFromMatrix(matrix,names);
        findCriticalPath();
    }

    public Graph(List<Vertex> vertices) {
        this.vertices = vertices;
        this.num_vertices = vertices.toArray().length;
    }

    public Graph() {
    }

    /**
     * Build orthogonalList from matrix.
     */
    // TODO: 2023/7/22 目前不支持动态维护
    public void buildOrthogonalListFromMatrix(int[][] matrix, String[] names) throws Exception {
        this.matrix = matrix;
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        // Initialize the vertex nodes list by the number of rows of the matrix.
        for(int i=0;i<matrix.length;i++){
            vertices.add(new Vertex(names[i]));
        }
        // Traverse the matrix and generate the orthogonal list.
        for(int i=0;i<matrix.length;i++){
            for(int j=0;j<matrix[0].length;j++){
                if(matrix[i][j]!=0){
                    // if there has an edge, add it to the orthogonal list.
                    Edge edge = new Edge();
                    if (matrix[i][j]!=0) {
                        edge.cost=matrix[i][j];
                    }
                    edge.tailVec = vertices.get(i);
                    edge.headVec = vertices.get(j);
                    // i build in-degree
                    buildNodeOutEdge(vertices.get(i), edge);
                    // j build out-degree
                    buildNodeInEdge(vertices.get(j), edge);
                    edges.add(edge);
                }
            }
        }
        buildDegree();
        num_vertices = vertices.toArray().length;
        num_edges = edges.toArray().length;
    }

    public void buildNodeInEdge(Vertex vertex, Edge edge){
        if(vertex.firstIn==null){
            vertex.firstIn = edge;
            return;
        }
        Edge tempEdge = vertex.firstIn;
        while (tempEdge.hLink!=null){
            tempEdge = tempEdge.hLink;
        }
        tempEdge.hLink = edge;
    }

    public void buildNodeOutEdge(Vertex vertex, Edge edge){
        if(vertex.firstOut==null){
            vertex.firstOut = edge;;
            return;
        }
        Edge tempEdge = vertex.firstOut;
        while (tempEdge.tLink!=null){
            tempEdge = tempEdge.tLink;
        }
        tempEdge.tLink = edge;
    }

    public void buildDegree(){
        for(Vertex tmpVertex: vertices){
            tmpVertex.buildOut_degree();
            tmpVertex.buildIn_degree();
        }
    }

    public LinkedList<Edge> findCriticalPath() {
        // Step 1: Initialize the earliest times (etv)
        for (Vertex v : vertices) {
            v.etv = 0;
        }

        // Step 2: Forward pass to calculate etv
        for (Vertex v : vertices) {
            if (v.in_degree == 0) {
                computeEtv(v);
            }
        }

        int maxEtv = 0;
        for (Vertex v : vertices) {
            maxEtv = Math.max(maxEtv, v.etv);
        }

        // Step 3: Initialize the latest times (ltv)
        for (Vertex v : vertices) {
            v.ltv = maxEtv;
        }

        // Step 4: Backward pass to calculate ltv
        for (Vertex v : vertices) {
            if (v.out_degree == 0) {
                computeLtv(v);
            }
        }

        // Step 5: Calculate ete and lte for each edge, and identify the critical path
        CriticalPath = new LinkedList<>();
        totalCriticalCost = 0;
        for (Edge e : edges) {
            e.ete = e.tailVec.etv;
            e.lte = e.headVec.ltv - e.cost;

            if (e.ete == e.lte) {
                CriticalPath.add(e);
                totalCriticalCost += e.cost;
            }
        }
        buildDegree();

        return CriticalPath;
    }

    private void computeEtv(Vertex v) {
        for (Edge e = v.firstOut; e != null; e = e.tLink) {
            e.headVec.etv = Math.max(e.headVec.etv, v.etv + e.cost);
            if (--e.headVec.in_degree == 0) {
                computeEtv(e.headVec);
            }
        }
    }

    private void computeLtv(Vertex v) {
        for (Edge e = v.firstIn; e != null; e = e.hLink) {
            e.tailVec.ltv = Math.min(e.tailVec.ltv, v.ltv - e.cost);
            if (--e.tailVec.out_degree == 0) {
                computeLtv(e.tailVec);
            }
        }
    }

    /**
     * print the total in-degree and out-degree.
     */
    public void printAllNodeIn(){
        System.out.println("In-Chain: ");
        for(Vertex vertex : vertices){
            System.out.println("    Vertex: "+ vertex.getName());
            System.out.println("    in-degree: "+ vertex.getIn_degree());
            Edge firstIn = vertex.firstIn;
            while (firstIn!=null){
                System.out.println("    " + firstIn.tailVec.getName()+"-->"+firstIn.headVec.getName()+" (cost="+firstIn.cost+")");
                firstIn = firstIn.hLink;

            }
            System.out.println();
        }
    }

    public void printAllNodeOut(){
        System.out.println("Out-Chain:");
        for(Vertex vertex : vertices){
            System.out.println("    Vertex: "+ vertex.getName());
            System.out.println("    out-degree: "+ vertex.getOut_degree());
            Edge firstOut = vertex.firstOut;
            while (firstOut!=null){
                System.out.println("    " + firstOut.tailVec.getName()+"-->"+firstOut.headVec.getName()+" (cost="+firstOut.cost+")");
                firstOut = firstOut.tLink;
            }
            System.out.println();
        }
    }

    public void printAllEdges(){
        System.out.println("Edges:"+ edges.toArray().length);
        for (Edge edge:this.edges){
            System.out.println(" "+edge.toString());
        }
    }

    public void printCriticalPath() {
        StringBuilder sb = new StringBuilder();
        sb.append("Critical Path: ");
        for (Edge edge : CriticalPath) {
            sb.append(edge.tailVec.getName()).append(" -> ");
        }
        sb.append(CriticalPath.getLast().headVec.getName());
        sb.append("\n");
        sb.append("Critical Edges:\n");
        for (Edge edge : CriticalPath) {
            sb.append(edge.tailVec.getName()).append(" -> ")
                    .append(edge.headVec.getName()).append(" (")
                    .append(edge.cost).append(")\n");
        }
        sb.append("Total Critical Cost: ").append(totalCriticalCost);
        System.out.println(sb.toString());
    }

    public Vertex findVertex(String name){
        assert vertices.stream().anyMatch(u -> name.equals(u.getName()));
        return vertices.stream().filter(u -> name.equals(u.getName())).toList().get(0);
    }
}


