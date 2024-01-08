package org.cloudbus.nativesim.policy.allocation;

/**
 * @author JingFeng Wu
 */
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobinAllocation {
    private final Queue<Server> servers;
    private Server currentServer;

    public RoundRobinAllocation(List<Server> serverList) {
        // 使用 LinkedList 作为循环队列
        this.servers = new LinkedList<>(serverList);
        this.currentServer = null;
    }

    public Server getNextServer() {
        // 如果当前服务器为空或者到达队列尾部，重新从队列头部开始
        if (currentServer == null || !servers.contains(currentServer)) {
            currentServer = servers.poll();
            servers.add(currentServer);
        } else {
            servers.add(servers.poll()); // 将当前服务器移动到队列的尾部
            currentServer = servers.peek();
        }
        return currentServer;
    }

    public static class Server {
        private String serverName;

        public Server(String serverName) {
            this.serverName = serverName;
        }

        @Override
        public String toString() {
            return "Server{" + "serverName='" + serverName + '\'' + '}';
        }
    }

    public static void main(String[] args) {
        List<Server> serverList = Arrays.asList(new Server("Server1"), new Server("Server2"), new Server("Server3"));
        RoundRobinAllocation scheduler = new RoundRobinAllocation(serverList);

        for (int i = 0; i < 10; i++) {
            System.out.println("Allocating to: " + scheduler.getNextServer());
        }
    }
}
