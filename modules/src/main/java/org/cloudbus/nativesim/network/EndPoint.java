/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.network;

import lombok.Data;
import org.cloudbus.nativesim.entity.Service;

import java.util.LinkedList;

@Data
public class EndPoint { // end point functions
    int userId;
    String name;
    LinkedList<Service> Call;

    public EndPoint(int userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}
