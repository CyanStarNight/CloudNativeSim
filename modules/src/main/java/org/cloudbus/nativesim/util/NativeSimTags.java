/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.util;

import org.cloudbus.cloudsim.core.CloudSimTags;

/**
 * Enum for various static command tags that indicate a type of action that needs to be undertaken
 * by CloudSim entities when they receive or send events.
 * <b>NOTE:</b> CloudSim reserves negative numbers, 0 - 299, and 9600.
 */
public class NativeSimTags {
    public static final int Service_CREATE = 49;
    public static final int Service_CREATE_ACK = 50;
    public static final int Service_DESTROY = 51;
    public static final int Service_DESTROY_ACK = 52;
    public static final int Service_MIGRATE = 53;
    public static final int Service_MIGRATE_ACK = 54;
    public static final int Service_DATA_ADD = 55;
    public static final int Service_DATA_ADD_ACK = 56;
    public static final int Service_DATA_DEL = 57;
    public static final int Service_DATA_DEL_ACK = 58;
    public static final int Service_DATACENTER_EVENT = 59;
    public static final int Service_BROKER_EVENT = 60;


    public static final int Instance_CREATE = 61;
    public static final int Instance_CREATE_ACK = 62;
    public static final int Instance_DESTROY = 63;
    public static final int Instance_DESTROY_ACK = 64;
    public static final int Instance_MIGRATE = 65;
    public static final int Instance_MIGRATE_ACK = 66;
    public static final int Instance_DATA_ADD = 67;
    public static final int Instance_DATA_ADD_ACK = 68;
    public static final int Instance_DATA_DEL = 69;
    public static final int Instance_DATA_DEL_ACK = 70;
    public static final int Instance_DATACENTER_EVENT = 71;
    public static final int Instance_BROKER_EVENT = 72;

    public static final int Request_CREATE = 73;
    public static final int Request_CREATE_ACK = 74;

    public static final int Endpoint_CREATE = 75;
    public static final int Endpoint_CREATE_ACK = 76;

    public static final int ServiceGraph_CREATE = 77;
    public static final int ServiceGraph_CREATE_ACK = 78;
    public static final int ServiceGraph_DESTROY = 79;
    public static final int ServiceGraph_DESTROY_ACK = 80;
    
    private NativeSimTags() {
        throw new UnsupportedOperationException("CloudSim Tags cannot be instantiated");
    }
}