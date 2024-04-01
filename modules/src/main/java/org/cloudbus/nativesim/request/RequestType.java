/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.request;

import lombok.Data;
import org.cloudbus.nativesim.service.Instance;
import org.cloudbus.nativesim.service.Service;

import java.util.*;

@Data
public class RequestType {

    public static Map<String, RequestType> map = new HashMap<>(); // 将request和characteristics联系起来

    public String type;

    public String API;

    public int num; //这类请求的总数

    public static List<Service> chain;

    public RequestType(int num, String type, String API) {
        this.num = num;
        this.type = type;
        this.API = API;
    }

    public List<Instance> dispatchRequests(List<Instance> instanceList){
        // 选择合适的源服务实例
        // 如果要分发到不同实例上，克隆并重新分组requests
        // 分发请求到选定的实例
        return null;
    }

    public List<Service> findCalledServices(List<Service> serviceList){

        if (chain != null)
            return chain;

        List<Service> list = new ArrayList<>(serviceList.stream().
                filter(service -> service.getApiList().contains(API)).toList());

        return list;
    }


    @Override
    public String toString() {
        return "Requests{" +
                "type='" + type + '\'' +
                ", API='" + API + '\'' +
                ", num=" + num +
                '}';
    }

}


