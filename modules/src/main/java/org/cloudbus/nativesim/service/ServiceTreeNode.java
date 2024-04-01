/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ServiceTreeNode {
    private Service service;
    private List<ServiceTreeNode> children = new ArrayList<>();
    public static Map<String, ServiceTreeNode> serviceNodeMap = new HashMap<>();
    private int inDegree = 0;
    private int outDegree = 0;

    public ServiceTreeNode(Service service) {
        this.service = service;
    }

    public String getServiceName() {
        return service.getName();
    }

    public void addChild(ServiceTreeNode child) {
        this.children.add(child);
        child.inDegree++;
        this.outDegree++;
    }

    public void removeChild(ServiceTreeNode child) {
        if (this.children.remove(child)) {
            child.inDegree--;
            this.outDegree--;
        }
    }
    public List<String> getAPI(){
        return service.getApiList();
    }

    public static ServiceTreeNode getTreeNode(String serviceName){
        return serviceNodeMap.get(serviceName);
    }
}
