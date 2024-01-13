/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.allocation;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.NativeVm;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Pod;
import org.cloudbus.nativesim.entity.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ContainerAllocationSimple extends ContainerAllocationPolicy {

    private Map<String, NativeVm> podTable;
    private Map<String, NativeVm> containerTable;
    private Map<String, Integer> usedPes;
    private List<Integer> freePes;

    public ContainerAllocationSimple(List<? extends NativeVm> list) {
        super(list);

        setFreePes(new ArrayList<Integer>());
        for (NativeVm vm : getVmList())
            getFreePes().add(vm.getNumberOfPes());

        setPodTable(new HashMap<String, NativeVm>());
        setUsedPes(new HashMap<String, Integer>());
    }

    @Override
    public boolean allocateVmForContainer(Container container) {
        return false;
    }

    @Override
    public boolean allocateVmForContainer(Container container, NativeVm vm) {
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeContainerAllocation(List<? extends Container> containerList) {
        return null;
    }

    @Override
    public void deallocateVmForContainer(Container container) {

    }

    @Override
    public NativeVm getVm(Container container) {
        return null;
    }

    @Override
    public NativeVm getVmForContainer(int containerId, int userId) {
        return null;
    }

    @Override
    public boolean allocateVmForPod(Pod pod) {
        int requiredPes = pod.getNumberOfPes();
        boolean result = false;
        int tries = 0;
        List<Integer> freePesTmp = new ArrayList<Integer>();
        for (Integer freePes : getFreePes()) {
            freePesTmp.add(freePes);
        }

        if (!getPodTable().containsKey(pod.getUid())) { // if this vm was not created
            do {// we're still trying until we find a host or until we try all of them
                int moreFree = Integer.MIN_VALUE;
                int idx = -1;

                // we want the host with fewer pes in use
                for (int i = 0; i < freePesTmp.size(); i++) {
                    if (freePesTmp.get(i) > moreFree) {
                        moreFree = freePesTmp.get(i);
                        idx = i;
                    }
                }

                NativeVm vm = getVmList().get(idx);
                result = vm.podCreate(pod);

                if (result) { // if vm were succesfully created in the host
                    getPodTable().put(vm.getUid(), vm);
                    getUsedPes().put(vm.getUid(), requiredPes);
                    getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
                    result = true;
                    break;
                } else {
                    freePesTmp.set(idx, Integer.MIN_VALUE);
                }
                tries++;
            } while (!result && tries < getFreePes().size());

        }

        return result;
    }

    @Override
    public List<Map<String, Object>> optimizePodAllocation(List<? extends Pod> podList) {
        return null;
    }
    @Override
    public NativeVm getVm(Pod pod) {
        return null;
    }

    @Override
    public NativeVm getVmForPod(int podId, int userId) {
        return null;
    }
    @Override
    public boolean allocateService(Service service) {
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Service> serviceList) {
        return null;
    }

}
