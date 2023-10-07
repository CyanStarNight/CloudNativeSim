package org.cloudbus.nativesim.entity;

import lombok.NonNull;
import org.cloudbus.nativesim.util.Type;

import java.util.Map;

/**
 * @author JingFeng Wu
 * the memeber of containers will be specified in file inputs.
 */
public class Container {
    private String  id;

    public String name;

    public Type type;

    private Resources limits_resources;

    private Resources requests_resources;
    
    public Container() {
    }

    @NonNull
    public static Container Registry(Map map){
        Container container = new Container();

//        container.setContainerName(Tools.getValue(map,"metadata.name"));
//        container.setLabels(Tools.getValue(map,"metadata.labels"));
//        container.setPorts(Tools.getValue(map,"spec.ports"));
//        container.setType(Tools.getValue(map,"spec.type"));

        return container;
    }

}