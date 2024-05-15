/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.allocation;

import lombok.Getter;
import lombok.Setter;
import core.Reporter;
import extend.NativePe;
import service.Instance;
import extend.NativeVm;
import service.Service;
import core.Status;

import java.util.*;

import static service.Instance.InstanceUidMap;


@Getter
@Setter
public class ServiceAllocationPolicySimple extends ServiceAllocationPolicy {

    private Map<String, Instance> serviceInstanceTable; // service name -> instance id

    private Map<String, NativeVm> instanceVmTable; // instance id -> vm

    private Map<NativeVm, List<Integer>> freePes; // vm id -> free Pes (num)


    public ServiceAllocationPolicySimple() {

        super();

        setServiceInstanceTable(new HashMap<>());

        setInstanceVmTable(new HashMap<>());

        setFreePes(new HashMap<>());

    }
    @Override
    public void init(List<? extends NativeVm> vmList){

        setVmList(vmList);

        getVmList().forEach(vm -> {
            List<Integer> freePes = new ArrayList<>(Collections.nCopies(vm.getNativePeList().size(), 1024));
            getFreePes().put(vm, freePes);
        });


    }

    @Override
    public boolean instantiateService(Service service){
        boolean result = false;

        List<String> s_labels = service.getLabels();
        // 多对多的映射labels
        for (Instance instance: InstanceUidMap.values()){

            for (String i_label:instance.getLabels()){

                if (s_labels.contains(i_label)) {

                    service.setBeingInstantiated(true);

                    service.getInstanceList().add(instance);

                    service.setStatus(Status.Created);

                    return true;
                }
            }
        }
        if (!result)
            System.out.println("[ServiceAllocation.instantiateService] Service #"+service.getName()+" can not be instantiated.");
        return result;
    }

    // 使用优先级队列实现基于MARF策略排序VM列表的方法
    protected List<? extends NativeVm> sortVmListByMARF(List<? extends NativeVm> vmList) {

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

        int requiredPes = instance.getRequests_share();

        List<Integer> vmFreePes = getFreePes().get(vm);

        int peId = -1;

        // 如果instance还未分配
        if (!getInstanceVmTable().containsKey(instance.getUid())){
            // 根据pe优先遍历
            for (int i = 0; i < vmFreePes.size(); i++) {

                double freePes = vmFreePes.get(i);

                if (requiredPes <= freePes){

                    result = vm.instanceCreate(instance,i);  // 指定pe分配资源

                    peId = i;
                }
            }

            if (result) {

                vmFreePes.set(peId, vmFreePes.get(peId) - requiredPes);

                getFreePes().replace(vm,vmFreePes);

                getInstanceVmTable().put(instance.getUid(), vm);

                instance.setVm(vm);

                vm.getInstanceList().add(instance);

                Reporter.printEvent(instance.getType()+" #"+instance.getId()+" has been allocated in Vm #"+vm.getId());

            }
        }
        return result;
    }

    @Override
    public boolean allocateVmForInstance(Instance instance) {

        boolean result = false;

        if (!getInstanceVmTable().containsKey(instance.getUid())) {

            for (NativeVm vm : sortVmListByMARF(getVmList())){

                if (allocateVmForInstance(instance, vm)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }



    private List<Integer> getVmFreePes(NativeVm vm) {
        return vm != null ? getFreePes().get(vm) : Collections.emptyList();
    }


    @Override
    public void deallocateVmForInstance(Instance instance) {

        NativeVm vm = getInstanceVmTable().remove(instance.getUid());

        getFreePes().remove(vm);

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
