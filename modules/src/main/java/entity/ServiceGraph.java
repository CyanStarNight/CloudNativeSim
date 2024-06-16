package entity;

import lombok.*;

import java.util.*;

/**
 * @describe the service graph with orthogonalList and maintain the critical path.
 */

@Data
public class ServiceGraph {
    private int userId;
    private UUID id;
    private Map<API, List<Service>> serviceChains = new HashMap<>();
    private Map<Service, Integer> inDegree = new HashMap<>();
    private Map<Service, Integer> outDegree = new HashMap<>();
    // the hierarchy of services
    private Map<Service, List<Service>> serviceHierarchy = new HashMap<>();
    private Map<Service, List<Service>> reverseServiceHierarchy = new HashMap<>();

    // Constructor initializes userId and generates a unique UUID
    public ServiceGraph(int userId) {
        this.userId = userId;
        this.id = UUID.randomUUID();
    }

    // Adds a service to the graph, optionally specifying a parent service
    public void addService(Service service, Service parentService) {
        // Initialize the service in the graphs if not already present
        if (!serviceHierarchy.containsKey(service)) {
            serviceHierarchy.put(service, new ArrayList<>());
            reverseServiceHierarchy.put(service, new ArrayList<>());
            inDegree.put(service, 0);
            outDegree.put(service, 0);
        }
        // If a parent service is specified, establish the parent-child relationship
        if (parentService != null) {
            serviceHierarchy.get(parentService).add(service);
            reverseServiceHierarchy.get(service).add(parentService);
            inDegree.put(service, inDegree.get(service) + 1);
            outDegree.put(parentService, outDegree.get(parentService) + 1);
        }
        service.setServiceGraph(this);
    }

    // Removes a service from the graph
    public void deleteService(Service service) {
        List<Service> parents = reverseServiceHierarchy.get(service);
        List<Service> children = serviceHierarchy.get(service);

        // Update parent services
        for (Service parent : parents) {
            serviceHierarchy.get(parent).remove(service);
            outDegree.put(parent, outDegree.get(parent) - 1);
        }

        // Update child services
        for (Service child : children) {
            reverseServiceHierarchy.get(child).remove(service);
            inDegree.put(child, inDegree.get(child) - 1);
        }

        // Remove the service from the graphs
        serviceHierarchy.remove(service);
        reverseServiceHierarchy.remove(service);
        inDegree.remove(service);
        outDegree.remove(service);
    }

    // Retrieves the parent services of a given service
    public List<Service> getParentServices(Service service) {
        return reverseServiceHierarchy.getOrDefault(service, Collections.emptyList());
    }

    // Retrieves the child services of a given service
    public List<Service> getCalls(Service service) {
        return serviceHierarchy.getOrDefault(service, Collections.emptyList());
    }

    // Retrieves the source services (services with in-degree of 0) in a given chain
    public List<Service> getSources(List<Service> chain) {
        List<Service> sources = new ArrayList<>();
        for (Service service : chain) {
            if (inDegree.get(service) == 0) {
                sources.add(service);
            }
        }
        return sources;
    }

    // Retrieves the sink services (services with out-degree of 0) in a given chain
    public List<Service> getSinks(List<Service> chain) {
        List<Service> sinks = new ArrayList<>();
        for (Service service : chain) {
            if (outDegree.get(service) == 0) {
                sinks.add(service);
            }
        }
        return sinks;
    }

    // Builds service chains for the given APIs and updates the serviceChains map
    public Map<API, List<Service>> buildServiceChains(List<API> apis) {
        Map<API, List<Service>> tempChains = new HashMap<>();

        // Populate tempChains with services for each API
        for (Service s : serviceHierarchy.keySet()) {
            for (API api : apis) {
                tempChains.computeIfAbsent(api, k -> new ArrayList<>()).add(s);
            }
        }

        // Sort each list in tempChains by in-degree
        for (Map.Entry<API, List<Service>> entry : tempChains.entrySet()) {
            List<Service> sortedList = entry.getValue();
            sortedList.sort(Comparator.comparingInt(inDegree::get));
            tempChains.put(entry.getKey(), sortedList);
        }

        // Update the serviceChains map and the API objects
        setServiceChains(tempChains);
        for (API api : tempChains.keySet()) {
            api.setServiceChain(tempChains.get(api));
        }

        assert !getServiceChains().isEmpty() : "Service Chains are empty.";
        return tempChains;
    }


    // Main method for example usage
    public static void main(String[] args) {
        // Create a ServiceGraph
        ServiceGraph serviceGraph = new ServiceGraph(1);
        Service parentService = new Service(1,"ParentService");
        Service childService = new Service(1,"ChildService");

        // Add services to the graph
        serviceGraph.addService(parentService, null);
        serviceGraph.addService(childService, parentService);

        // Get parent services
        List<Service> parentServices = serviceGraph.getParentServices(childService);
        for (Service parent : parentServices) {
            System.out.println("Parent Service: " + parent.getName());
        }

        // Get source services
        List<Service> sources = serviceGraph.getSources(Arrays.asList(parentService, childService));
        for (Service source : sources) {
            System.out.println("Source Service: " + source.getName());
        }

        // Get sink services
        List<Service> sinks = serviceGraph.getSinks(Arrays.asList(parentService, childService));
        for (Service sink : sinks) {
            System.out.println("Sink Service: " + sink.getName());
        }
    }

    public List<Service> getAllServices() {
        return getServiceHierarchy().keySet().stream().toList();
    }
}

