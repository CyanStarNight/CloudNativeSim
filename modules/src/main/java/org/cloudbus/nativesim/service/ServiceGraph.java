package org.cloudbus.nativesim.service;

import lombok.*;

import java.util.*;

/**
 * @author JingFeng Wu
 * @describe the service graph with orthogonalList and maintain the critical path.
 */

@Data
public class ServiceGraph{
    private int userId;
    private UUID id; // Using UUID for unique identification
    private List<ServiceTreeNode> roots = new ArrayList<>();
    private Map<String,List<Service>> serviceChains = new HashMap<>();
    private Map<String, LinkedList<Service>> criticalPaths = new HashMap<>(); // The main path of a chain
    private double totalCriticalCost;

    public ServiceGraph(int userId) {
        this.userId = userId;
        this.id = UUID.randomUUID(); // Ensures unique ID
    }

    public void addService(Service service, Service parentService) {
        ServiceTreeNode node = new ServiceTreeNode(service);
        if (parentService == null) {
            roots.add(node);
        } else {
            findServiceNode(parentService, roots)
                    .ifPresent(parentNode -> parentNode.addChild(node));
        }
    }

    public void deleteService(Service service) {
        findAndRemoveServiceNode(service, roots, null);
    }

    private Optional<ServiceTreeNode> findServiceNode(Service service, List<ServiceTreeNode> nodes) {
        for (ServiceTreeNode node : nodes) {
            if (node.getService().equals(service)) {
                return Optional.of(node);
            }
            Optional<ServiceTreeNode> foundNode = findServiceNode(service, node.getChildren());
            if (foundNode.isPresent()) {
                return foundNode;
            }
        }
        return Optional.empty();
    }

    private void findAndRemoveServiceNode(Service service, List<ServiceTreeNode> nodes, ServiceTreeNode parentNode) {
        Iterator<ServiceTreeNode> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            ServiceTreeNode node = iterator.next();
            if (node.getService().equals(service)) {
                if (parentNode != null) parentNode.removeChild(node);
                iterator.remove();
            } else {
                findAndRemoveServiceNode(service, node.getChildren(), node);
            }
        }
    }

    // 更新节点度数
    private void updateDegrees() {
        // 重置所有节点的度数
        resetAllDegrees();
        // 更新所有节点的出度
        for (ServiceTreeNode root : roots) {
            updateOutDegrees(root);
        }
        // 更新所有节点的入度
        for (ServiceTreeNode root : roots) {
            updateInDegrees(root, new HashSet<>());
        }
    }

    // 重置所有服务节点的度数
    private void resetAllDegrees() {
        Queue<ServiceTreeNode> queue = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            ServiceTreeNode current = queue.poll();
            current.setInDegree(0);
            current.setOutDegree(0);
            queue.addAll(current.getChildren());
        }
    }

    // 更新出度
    private void updateOutDegrees(ServiceTreeNode node) {
        int outDegree = node.getChildren().size();
        node.setOutDegree(outDegree);
        for (ServiceTreeNode child : node.getChildren()) {
            updateOutDegrees(child);
        }
    }

    // 更新入度，使用Set防止重复计算
    private void updateInDegrees(ServiceTreeNode node, Set<ServiceTreeNode> visited) {
        if (!visited.contains(node)) {
            for (ServiceTreeNode child : node.getChildren()) {
                child.setInDegree(child.getInDegree() + 1);
                updateInDegrees(child, visited);
            }
            visited.add(node);
        }
    }

    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        Set<ServiceTreeNode> visited = new HashSet<>(); // Initialize visited set
        // Apply BFS or DFS from each root node to collect services
        for (ServiceTreeNode root : roots) {
            sortServicesFromNodeBFS(root, services, visited); // Or use DFS variant
        }
        return services;
    }


    // Modified BFS method to include visited tracking
    private void sortServicesFromNodeBFS(ServiceTreeNode node, List<Service> services, Set<ServiceTreeNode> visited) {
        if (node == null || visited.contains(node)) return; // Skip null or already visited nodes

        Queue<ServiceTreeNode> queue = new LinkedList<>();
        queue.add(node);
        visited.add(node); // Mark the start node as visited

        while (!queue.isEmpty()) {
            ServiceTreeNode current = queue.poll();
            services.add(current.getService()); // Collect the current node's service

            for (ServiceTreeNode child : current.getChildren()) {
                if (!visited.contains(child)) {
                    queue.add(child);
                    visited.add(child); // Mark as visited when enqueued
                }
            }
        }
    }


    // Modified DFS method to include visited tracking
    private void sortServicesFromNodeDFS(ServiceTreeNode node, List<Service> services, Set<ServiceTreeNode> visited) {
        if (node == null || visited.contains(node)) return; // Skip null or already visited nodes

        visited.add(node); // Mark the current node as visited
        services.add(node.getService()); // Collect the current node's service

        for (ServiceTreeNode child : node.getChildren()) {
            if (!visited.contains(child)) {
                sortServicesFromNodeDFS(child, services, visited); // Recursive call
            }
        }
    }

    // Method to collect all services into chains based on API
    public void buildServiceChains() { //TODO: chain需要验证 能否从source遍历整个chain
        serviceChains = new HashMap<>();
        Map<String, List<Service>> tempChains = new HashMap<>();

        for (Service s : getAllServices()) {
            for (String api : s.getApiList()) {
                tempChains.computeIfAbsent(api, k -> new ArrayList<>()).add(s);
            }
        }

        // Now sort each list in tempChains by in-degree and put it in serviceChains
        for (Map.Entry<String, List<Service>> entry : tempChains.entrySet()) {
            List<Service> sortedList = entry.getValue();
            // Sorting based on in-degree
            sortedList.sort(Comparator.comparingInt(Service::getInDegree));
            serviceChains.put(entry.getKey(), sortedList);
        }
    }


    public List<Service> getCalls(String serviceName, String api) {

        List<Service> chain = serviceChains.get(api);
        ServiceTreeNode serviceTreeNode = ServiceTreeNode.getTreeNode(serviceName);
        List<ServiceTreeNode> calledTreeNodes = serviceTreeNode.getChildren();

        List<Service> calledService = new ArrayList<>();
        for (ServiceTreeNode node : calledTreeNodes){
            Service s = node.getService();
            if (chain.contains(s))
                calledService.add(s);
        }

        return calledService;
    }
}
