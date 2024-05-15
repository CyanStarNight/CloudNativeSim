/*
 * Copyright ©2024. Jingfeng Wu.
 */

package service;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ChainNode {
    private Service service;
    private List<ChainNode> children = new ArrayList<>();
    public static Map<String, ChainNode> serviceNodeMap = new HashMap<>();
    private int inDegree = 0;
    private int outDegree = 0;

    public ChainNode(Service service) {
        this.service = service;
    }

    public String getServiceName() {
        return service.getName();
    }

    public void addChild(ChainNode child) {
        this.children.add(child);
        child.inDegree++;
        this.outDegree++;
    }

    public void removeChild(ChainNode child) {
        if (this.children.remove(child)) {
            child.inDegree--;
            this.outDegree--;
        }
    }
    public List<String> getAPI(){
        return service.getApiList();
    }

    public static ChainNode getTreeNode(String serviceName){
        return serviceNodeMap.get(serviceName);
    }
}
