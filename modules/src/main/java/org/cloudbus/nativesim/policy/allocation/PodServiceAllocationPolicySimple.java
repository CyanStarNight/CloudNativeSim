/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.allocation;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.NativeVm;
import org.cloudbus.nativesim.entity.Pod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PodServiceAllocationPolicySimple extends ContainerServiceAllocationPolicySimple {
    private Map<String, NativeVm> podTable;
    private Map<String, Integer> usedPes;
    private List<Integer> freePes;

    public PodServiceAllocationPolicySimple(List<? extends NativeVm> list) {
        super(list);
    }

    public boolean allocateVmForPod(Pod pod, NativeVm vm){// the containers in pod will be allocated to the same Vm
        return pod.getContainerList().stream().allMatch(container -> allocateVmForInstance(container, vm));
    }

    public void deallocateVmForPod(Pod pod){
        pod.getContainerList().forEach(this::deallocateVmForInstance);
        pod.setVm(null);
    }

    public boolean allocateVmForPod(Pod pod) {
        int requiredPes = pod.getNumberOfPes();
        boolean result = false;
        int tries = 0;
        List<Integer> freePesTmp = new ArrayList<Integer>();
        for (Integer freePes : getFreePes()) {
            freePesTmp.add(freePes);
        }

        if (!getPodTable().containsKey(pod.getUid())) {
            do {
                int moreFree = Integer.MIN_VALUE;
                int idx = -1;

                for (int i = 0; i < freePesTmp.size(); i++) {
                    if (freePesTmp.get(i) > moreFree) {
                        moreFree = freePesTmp.get(i);
                        idx = i;
                    }
                }

                NativeVm vm = getVmList().get(idx);
                result = vm.instanceCreate(pod);

                if (result) {
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

    public List<Map<String, Object>> optimizePodAllocation(List<? extends Pod> podList) {
        return null;
    }

    public NativeVm getVm(Pod pod) {
        return getPodTable().get(pod.getUid());
    }

    public NativeVm getVm(int podId, int userId) {
        return getPodTable().get(Pod.getUid(userId,podId));
    }

}
