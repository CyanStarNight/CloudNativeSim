/*
 * Copyright ©2024. Jingfeng Wu.
 */

package entity;

import lombok.*;

import java.util.*;

/**
 * @author JingFeng Wu
 * @describe the service graph with orthogonalList and maintain the critical path.
 */

@Data
public class ServiceGraph{
    private int userId;
    // Using UUID for unique identification
    private UUID id;
    // source service nodes
    private List<ChainNode> roots = new ArrayList<>();
    // api -> chain
    private Map<String,List<Service>> serviceChains = new HashMap<>();

    public ServiceGraph(int userId) {
        this.userId = userId;
        this.id = UUID.randomUUID(); // Ensures unique ID
    }

    public void addService(Service service, Service parentService) {
        ChainNode node = new ChainNode(service);
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

    private Optional<ChainNode> findServiceNode(Service service, List<ChainNode> nodes) {
        for (ChainNode node : nodes) {
            if (node.getService().equals(service)) {
                return Optional.of(node);
            }
            Optional<ChainNode> foundNode = findServiceNode(service, node.getChildren());
            if (foundNode.isPresent()) {
                return foundNode;
            }
        }
        return Optional.empty();
    }

    private void findAndRemoveServiceNode(Service service, List<ChainNode> nodes, ChainNode parentNode) {
        Iterator<ChainNode> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            ChainNode node = iterator.next();
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
        for (ChainNode root : roots) {
            updateOutDegrees(root);
        }
        // 更新所有节点的入度
        for (ChainNode root : roots) {
            updateInDegrees(root, new HashSet<>());
        }
    }

    // 重置所有服务节点的度数
    private void resetAllDegrees() {
        Queue<ChainNode> queue = new LinkedList<>(roots);
        while (!queue.isEmpty()) {
            ChainNode current = queue.poll();
            current.setInDegree(0);
            current.setOutDegree(0);
            queue.addAll(current.getChildren());
        }
    }

    // 更新出度
    private void updateOutDegrees(ChainNode node) {
        int outDegree = node.getChildren().size();
        node.setOutDegree(outDegree);
        for (ChainNode child : node.getChildren()) {
            updateOutDegrees(child);
        }
    }

    // 更新入度，使用Set防止重复计算
    private void updateInDegrees(ChainNode node, Set<ChainNode> visited) {
        if (!visited.contains(node)) {
            for (ChainNode child : node.getChildren()) {
                child.setInDegree(child.getInDegree() + 1);
                updateInDegrees(child, visited);
            }
            visited.add(node);
        }
    }

    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        Set<ChainNode> visited = new HashSet<>(); // Initialize visited set
        // Apply BFS or DFS from each root node to collect services
        for (ChainNode root : roots) {
            sortServicesFromNodeBFS(root, services, visited); // Or use DFS variant
        }
        return services;
    }


    // Modified BFS method to include visited tracking
    private void sortServicesFromNodeBFS(ChainNode node, List<Service> services, Set<ChainNode> visited) {
        if (node == null || visited.contains(node)) return; // Skip null or already visited nodes

        Queue<ChainNode> queue = new LinkedList<>();
        queue.add(node);
        visited.add(node); // Mark the start node as visited

        while (!queue.isEmpty()) {
            ChainNode current = queue.poll();
            services.add(current.getService()); // Collect the current node's service

            for (ChainNode child : current.getChildren()) {
                if (!visited.contains(child)) {
                    queue.add(child);
                    visited.add(child); // Mark as visited when enqueued
                }
            }
        }
    }


    // Modified DFS method to include visited tracking
    private void sortServicesFromNodeDFS(ChainNode node, List<Service> services, Set<ChainNode> visited) {
        if (node == null || visited.contains(node)) return; // Skip null or already visited nodes

        visited.add(node); // Mark the current node as visited
        services.add(node.getService()); // Collect the current node's service

        for (ChainNode child : node.getChildren()) {
            if (!visited.contains(child)) {
                sortServicesFromNodeDFS(child, services, visited); // Recursive call
            }
        }
    }

    // Method to collect all services into chains based on API
    public Map<String,List<Service>> buildServiceChains() {
        setServiceChains(new HashMap<>());
        Map<String, List<Service>> tempChains = new HashMap<>();

        // 将同一个API的服务放入一个新的 ArrayList 中,key 为 api。
        for (Service s : getAllServices()) {
            for (String api : s.getApiList()) {
                // 如果 tempChains 中不存在 key 为 api 的条目,则创建一个新的
                tempChains.computeIfAbsent(api, k -> new ArrayList<>()).add(s);
            }
        }
        // Now sort each list in tempChains by in-degree and put it in serviceChains
        for (Map.Entry<String, List<Service>> entry : tempChains.entrySet()) {
            List<Service> sortedList = entry.getValue();
            // Sorting based on in-degree
            sortedList.sort(Comparator.comparingInt(Service::getInDegree));
            tempChains.replace(entry.getKey(), sortedList);
        }
        setServiceChains(tempChains);

        assert !getServiceChains().isEmpty():"Service Chains are empty.";
        return tempChains;
    }


    public List<Service> getCalls(String serviceName, String api) {

        List<Service> chain = serviceChains.get(api);
        ChainNode chainNode = ChainNode.getTreeNode(serviceName);
        List<ChainNode> calledTreeNodes = chainNode.getChildren();

        List<Service> calledService = new ArrayList<>();
        for (ChainNode node : calledTreeNodes){
            Service s = node.getService();
            if (chain.contains(s))
                calledService.add(s);
        }

        return calledService;
    }
}
