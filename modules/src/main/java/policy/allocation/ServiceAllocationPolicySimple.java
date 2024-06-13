/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.allocation;

import lombok.Getter;
import lombok.Setter;
import core.Reporter;
import extend.NativePe;
import entity.Instance;
import extend.NativeVm;
import entity.Service;

import java.util.*;


@Getter
@Setter
public class ServiceAllocationPolicySimple extends ServiceAllocationPolicy {

    private Map<String, Instance> serviceInstanceTable; // service name -> instance id

    private Map<String, NativeVm> instanceVmTable; // instance id -> vm


    public ServiceAllocationPolicySimple() {
        super();
        setServiceInstanceTable(new HashMap<>());
        setInstanceVmTable(new HashMap<>());
    }

    @Override
    public void init(List<? extends NativeVm> vmList){
        setVmList(vmList);

    }


    // 使用优先级队列实现基于MinimumUtilization排序VM列表的方法
    protected List<? extends NativeVm> sortVmListByMinimumUtilization(List<? extends NativeVm> vmList) {

        // 定义一个优先级队列，根据PE的availableShare从大到小排序
        PriorityQueue<NativeVm> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(vm -> {
            double maxAvailableShare = vm.getNativePeList().stream()
                        .mapToDouble(NativePe::getAvailableShare)
                    .max()
                    .orElse(0.0);
            return -maxAvailableShare; // 使用负值实现从大到小排序
        }));

        // 将VM列表中的元素添加到优先级队列中
        priorityQueue.addAll(vmList);

        // 将排序后的元素放入列表中
        List<NativeVm> sortedVmList = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            sortedVmList.add(priorityQueue.poll());
        }

        return sortedVmList;
    }



    @Override
    public boolean allocateService(Service service) {

        assert service.isBeingInstantiated() : "Service #"+service.getName()+" has not been instantiated.";

        List<Instance> instanceList = service.getInstanceList();

        if (instanceList.stream().allMatch(this :: allocateVmForInstance)){

            Reporter.printEvent("Service #"+service.getName()+" has been successfully allocated.");

            service.setBeingAllocated(true);

            return true;
        }else {

            System.out.println("[ServiceAllocation.allocateService] Service #"+service.getName()+" failed to allocate.");

            return false;
        }
    }

    @Override
    public void deallocateService(Service service) {
        service.getInstanceList().forEach(this::deallocateVmForInstance);
        Service.serviceNameMap.remove(service.name);
    }

    public boolean allocateVmForInstance(Instance instance, NativeVm vm){

        boolean result = false;

        int requiredShare = instance.getRequests_share();

        // 如果instance还未分配
        if (!getInstanceVmTable().containsKey(instance.getUid())){
            // 根据pe优先遍历
            result = vm.instanceCreate(instance);

            if (result) {

                getInstanceVmTable().put(instance.getUid(), vm);

                instance.setVm(vm);

                vm.getInstanceList().add(instance);

                createdInstanceList.add(instance);

            }
        }
        return result;
    }

    @Override
    public boolean allocateVmForInstance(Instance instance) {

        boolean result = false;

        if (!getInstanceVmTable().containsKey(instance.getUid())) {

            for (NativeVm vm : sortVmListByMinimumUtilization(getVmList())){

                if (allocateVmForInstance(instance, vm)) {
                    result = true;
                    Reporter.printEvent(instance.getType()+" #"+instance.getName()+" has been allocated in Vm #"+vm.getId());
                    break;
                }
            }
        }
        return result;
    }




    @Override
    public void deallocateVmForInstance(Instance instance) {

        NativeVm vm = getInstanceVmTable().remove(instance.getUid());

        // release pe share
        int usedShare = instance.getUsedShare();

        instance.setUsedShare(0);

        NativePe pe = instance.getCurrentAllocatedPe();

        int peId = vm.getNativePeList().indexOf(pe);

        assert peId != -1;
        // remove mapping
        vm.instanceDestroy(instance);
    }

    @Override
    public NativeVm getVm(Instance instance) {
        return getInstanceVmTable().get(instance.getUid());
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Service> serviceList) {
        return null;
    }


}
