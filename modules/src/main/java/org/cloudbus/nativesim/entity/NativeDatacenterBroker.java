package org.cloudbus.nativesim.entity;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Getter
@Setter
public class NativeDatacenterBroker extends DatacenterBroker {
    protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

    protected List<Instance> instanceList;
    protected List<Instance> instancesCreatedList;
    protected int instancesRequested;
    protected int instancesAcks;
    protected int instancesDestroyed;
    protected Map<Integer, Integer> instancesToDatacentersMap;

//    protected List<? extends NativeCloudlet> cloudletList;
//    /** The cloudlet submitted list. */
//    protected List<? extends NativeCloudlet> cloudletSubmittedList;
//    /** The cloudlet received list. */
//    protected List<? extends NativeCloudlet> cloudletReceivedList;

    public NativeDatacenterBroker(String name) throws Exception {
        super(name);
        setInstanceList(new ArrayList<Instance>());
        setInstancesCreatedList(new ArrayList<Instance>());
        setInstancesRequested(0);
        setInstancesAcks(0);
        setInstancesDestroyed(0);
        setInstancesToDatacentersMap(new HashMap<Integer, Integer>());
    }

//    public void submitInstanceList(List<? extends Instance> list) {
//        getInstanceList().addAll(list);
//    }
//
//    public void bindCloudletToInstance(int cloudletId, int instanceId) {
//        CloudletList.getById(getCloudletList(), cloudletId).setInstanceId(instanceId);
//    }

}
