package org.cloudbus.nativesim.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.nativesim.entity.Instance;
import org.cloudbus.nativesim.entity.NativePe;
import org.cloudbus.nativesim.provisioner.InstancePeProvisioner;

import java.util.*;
@Getter
@Setter
public class InstanceSchedulerTimeShared extends InstanceScheduler {

    private Map<String, List<Double>> mipsMapRequested;
    private int pesInUse;

    public InstanceSchedulerTimeShared(List<NativePe> pelist) {
        super(pelist);
        setMipsMapRequested(new HashMap<String, List<Double>>());
    }

    @Override
    public boolean allocatePesForInstance(Instance instance, List<Double> mipsShareRequested) {
        if (instance.isInMigration()) {
            if (!getInstanceMigratingIn().contains(instance.getUid()) && !getInstanceMigratingOut().contains(instance.getUid())) {
                getInstanceMigratingOut().add(instance.getUid());
            }
        } else {
            if (getInstanceMigratingOut().contains(instance.getUid())) {
                getInstanceMigratingOut().remove(instance.getUid());
            }
        }
        boolean result = allocatePesForInstance(instance.getUid(), mipsShareRequested);
        updatePeProvisioning();
        return result;
    }

    protected boolean allocatePesForInstance(String instanceUid, List<Double> mipsShareRequested) {
        double totalRequestedMips = 0;
        double peMips = getPeCapacity();
        for (Double mips : mipsShareRequested) {
            // each virtual PE of a INSTANCE must require not more than the capacity of a physical PE
            if (mips > peMips) {
                return false;
            }
            totalRequestedMips += mips;
        }

        // This scheduler does not allow over-subscription
        if (getAvailableMips() < totalRequestedMips) {
            return false;
        }

        getMipsMapRequested().put(instanceUid, mipsShareRequested);
        setPesInUse(getPesInUse() + mipsShareRequested.size());

        if (getInstanceMigratingIn().contains(instanceUid)) {
            // the destination host only experience 10% of the migrating INSTANCE's MIPS
            totalRequestedMips *= 0.1;
        }

        List<Double> mipsShareAllocated = new ArrayList<Double>();
        for (Double mipsRequested : mipsShareRequested) {
            if (getInstanceMigratingOut().contains(instanceUid)) {
                // performance degradation due to migration = 10% MIPS
                mipsRequested *= 0.9;
            } else if (getInstanceMigratingIn().contains(instanceUid)) {
                // the destination host only experience 10% of the migrating INSTANCE's MIPS
                mipsRequested *= 0.1;
            }
            mipsShareAllocated.add(mipsRequested);
        }

        getMipsMap().put(instanceUid, mipsShareAllocated);
        setAvailableMips(getAvailableMips() - totalRequestedMips);

        return true;
    }

    /**
     * Update allocation of INSTANCEs on PEs.
     */
    protected void updatePeProvisioning() {
        getPeMap().clear();
        for (NativePe pe : getPeList()) {
            pe.getInstancePeProvisioner().deallocateMipsForAllInstances();
        }

        Iterator<NativePe> peIterator = (Iterator<NativePe>) getPeList().iterator();
        NativePe pe = peIterator.next();
        InstancePeProvisioner peProvisioner = pe.getInstancePeProvisioner();
        double availableMips = peProvisioner.getAvailableMips();

        for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
            String instanceUid = entry.getKey();
            getPeMap().put(instanceUid, new LinkedList<NativePe>());

            for (double mips : entry.getValue()) {
                while (mips >= 0.1) {
                    if (availableMips >= mips) {
                        peProvisioner.allocateMipsForInstance(instanceUid, mips);
                        getPeMap().get(instanceUid).add(pe);
                        availableMips -= mips;
                        break;
                    } else {
                        peProvisioner.allocateMipsForInstance(instanceUid, availableMips);
                        getPeMap().get(instanceUid).add(pe);
                        mips -= availableMips;
                        if (mips <= 0.1) {
                            break;
                        }
                        if (!peIterator.hasNext()) {
                            Log.printLine("There is no enough MIPS (" + mips + ") to accommodate INSTANCE " + instanceUid);
                            // System.exit(0);
                        }
                        pe = peIterator.next();
                        peProvisioner = pe.getInstancePeProvisioner();
                        availableMips = peProvisioner.getAvailableMips();
                    }
                }
            }
        }
    }


    @Override
    public void deallocatePesForInstance(Instance instance) {
        getMipsMapRequested().remove(instance.getUid());
        setPesInUse(0);
        getMipsMap().clear();
        setAvailableMips(PeList.getTotalMips(getPeList()));

        for (NativePe pe : getPeList()) {
            pe.getInstancePeProvisioner().deallocateMipsForInstance(instance);
        }

        for (Map.Entry<String, List<Double>> entry : getMipsMapRequested().entrySet()) {
            allocatePesForInstance(entry.getKey(), entry.getValue());
        }

        updatePeProvisioning();
    }


    @Override
    public void deallocatePesForAllInstances() {
        super.deallocatePesForAllInstances();
        getMipsMapRequested().clear();
        setPesInUse(0);
    }
    
    @Override
    public double getMaxAvailableMips() {
        return getAvailableMips();
    }


}
