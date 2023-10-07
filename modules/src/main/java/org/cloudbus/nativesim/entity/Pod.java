package org.cloudbus.nativesim.entity;

import java.util.ArrayList;
import java.util.Map;

import lombok.NonNull;

/**
 * @author JingFeng Wu
 */
public class Pod {

    private ArrayList<Container> ContainerList;
    private int numContainers;

    public Pod() {}

    @NonNull
    public static Pod PodRegistry(Map<String,Object> map){
        Pod pod = new Pod();

//        pod.setPodName(Tools.getValue(map,"metadata.name"));
//        pod.setPorts(Tools.getValue(map,"spec.ports"));
//        pod.setType(Tools.getValue(map,"spec.type"));

        pod.CombinePod2Service();

        return pod;
    }
    void CombinePod2Service(){

    }
}
