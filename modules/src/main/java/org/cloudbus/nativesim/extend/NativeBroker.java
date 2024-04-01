/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.extend;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.nativesim.core.NativeSim;
import org.cloudbus.nativesim.core.Register;
import org.cloudbus.nativesim.core.Reporter;

import java.util.ArrayList;
import java.util.List;

import static org.cloudbus.cloudsim.Log.printLine;

@Getter
@Setter
public class NativeBroker extends DatacenterBroker {

    int appId;

    private Register register;

    public NativeBroker(String name) throws Exception {
        super(name);
    }

    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // Check datacenter allocated
            case NativeSimTag.CHECK_DC_ALLOCATED:
                appId = ev.getSource();
                processCheckAllocated();
                break;

            default:
                super.processEvent(ev);
                break;
        }
    }

    @Override
    public void startEntity() {
        printLine(getName() + " is starting...");
        schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
    }

    private void processCheckAllocated() {
        boolean result = false;
        if (getVmsCreatedList() != null){
            extendVms(getVmsCreatedList());
            result = true;
            printLine("\n"+ NativeSim.clock() + ": " + "Datacenters have been allocated.");
        }
        if (result){
            sendNow(appId,NativeSimTag.GET_NODES,getVmsCreatedList());
            if (register!=null) {
                sendNow(appId, NativeSimTag.APP_CHARACTERISTICS, getRegister());
            }
        }

    }

    private void extendVms(List<NativeVm> createdVMs){
        for (NativeVm vm:createdVMs){
            //分配nativePe
            List<NativePe> peList = new ArrayList<>();

            assert vm.getNumberOfPes() == vm.getCurrentAllocatedMips().size();
            for (int i = 0; i < vm.getNumberOfPes(); i++) {
                //默认vm所有pe的mips都相等
                peList.add(new NativePe(i,vm.getMips()));
            }
            vm.setNativePeList(peList);
            vm.getNativePeProvisioner().init(peList);

            vm.getVmBwProvisioner().init(vm.getCurrentAllocatedBw());
            vm.getNativeRamProvisioner().init(vm.getCurrentAllocatedRam());

        }

    }

    public void submitRegister(Register register){
//        getRegisterList().add(register);
        setRegister(register);
    }
}
