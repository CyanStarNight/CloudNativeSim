/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.allocation;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.nativesim.entity.Instance;
import org.cloudbus.nativesim.entity.NativeVm;
import org.cloudbus.nativesim.entity.Pod;
import org.cloudbus.nativesim.entity.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ContainerServiceAllocationPolicySimple extends ServiceAllocationPolicy {

    private Map<String, NativeVm> instanceTable;
    private Map<String, Integer> usedPes;
    private List<Integer> freePes;

    public ContainerServiceAllocationPolicySimple(List<? extends NativeVm> vmList) {
        super(vmList);

        setFreePes(new ArrayList<Integer>());
        for (NativeVm vm : getVmList())
            getFreePes().add(vm.getNumberOfPes());

        setInstanceTable(new HashMap<String, NativeVm>());
        setUsedPes(new HashMap<String, Integer>());
    }

    @Override
    public boolean allocateVmForInstance(Instance instance) {
        int requiredPes = instance.getNumberOfPes();
        boolean result = false;
        int tries = 0;
        List<Integer> freePesTmp = new ArrayList<Integer>();
        freePesTmp.addAll(getFreePes());

        if (!getInstanceTable().containsKey(instance.getUid())) {
            do {
                int moreFree = Integer.MIN_VALUE;
                int idx = -1;

                // we want the host with less pes in use
                for (int i = 0; i < freePesTmp.size(); i++) {
                    if (freePesTmp.get(i) > moreFree) {
                        moreFree = freePesTmp.get(i);
                        idx = i;
                    }
                }

                NativeVm vm = getVmList().get(idx);
                result = vm.instanceCreate(instance);

                if (result) {
                    getInstanceTable().put(instance.getUid(), vm);
                    getUsedPes().put(instance.getUid(), requiredPes);
                    getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
                    break;
                } else {
                    freePesTmp.set(idx, Integer.MIN_VALUE);
                }
                tries++;
            } while (!result && tries < getFreePes().size());

        }

        return result;
    }

    public boolean allocateVmForInstance(Instance instance,NativeVm vm){
        int requiredPes = instance.getNumberOfPes();
        boolean result = false;
        List<Integer> freePesTmp = new ArrayList<Integer>();
        freePesTmp.addAll(getFreePes());
        if (!getInstanceTable().containsKey(instance.getUid())){
            int moreFree = Integer.MIN_VALUE;
            int idx = -1;
            // we want the host with less pes in use
            for (int i = 0; i < freePesTmp.size(); i++) {
                if (freePesTmp.get(i) > moreFree) {
                    moreFree = freePesTmp.get(i);
                    idx = i;
                }
            }

            result = vm.instanceCreate(instance);
            if (result) {
                getInstanceTable().put(instance.getUid(), vm);
                getUsedPes().put(instance.getUid(), requiredPes);
                getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
            } else {
                freePesTmp.set(idx, Integer.MIN_VALUE);
            }
        }
        return result;
    }

    @Override
    public void deallocateVmForInstance(Instance instance) {
        NativeVm vm = getInstanceTable().remove(instance.getUid());
        int idx = getVmList().indexOf(vm);
        int pes = getUsedPes().remove(vm.getUid());
        if (vm != null) {
            vm.instanceDestroy(instance);
            getFreePes().set(idx, getFreePes().get(idx) + pes);
        }
    }

    @Override
    public NativeVm getVm(Instance instance) {
        return getInstanceTable().get(instance.getUid());
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
