/*
 * Copyright ©2024. Jingfeng Wu.
 */

package extend;

public class CloudNativeSimTag {
    private static final int BASE = 50;

    public static final int
            CHECK_DC_ALLOCATED  = BASE + 1 ,
            GET_NODES = BASE + 2 ,
            APP_CHARACTERISTICS = BASE + 3,
            REQUEST_GENERATE = BASE + 4 ,
            SERVICE_DESTROY = BASE + 5 ,
            INSTANCE_MIGRATE = BASE + 6 ,
            SERVICE_UPDATE = BASE + 7 ,
            SERVICE_SCALING = BASE + 8,
            SERVICE_ALLOCATE = BASE+9 ,
            REQUEST_DISPATCH = BASE + 10,
            INSTANCE_CREATE = BASE + 12 ,
            INSTANCE_DESTROY = BASE + 13 ,
            INSTANCE_UPDATE = BASE + 14 ,
            Instance_MIGRATE = BASE + 15,
            START_CLIENTS = 16,
            CLOUDLET_SUBMIT = BASE + 20 ,
            CLOUDLET_CANCEL = BASE + 21,
            CLOUDLET_PROCESS = BASE + 22 ,
            CLOUDLET_PAUSE = BASE + 23 ,
            CLOUDLET_RESUME = BASE + 24 ,
            CLOUDLET_MOVE = BASE + 25 ,
            CLOUDLET_RETURN = BASE + 26,
            UPDATE_USAGE =27,
            STATE_CHECK = BASE +30;

    private CloudNativeSimTag() {
        throw new UnsupportedOperationException("NativeSim Tags cannot be instantiated");
    }
}