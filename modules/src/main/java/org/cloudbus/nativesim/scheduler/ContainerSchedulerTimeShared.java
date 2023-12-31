package org.cloudbus.nativesim.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.NativePe;
import org.cloudbus.nativesim.provisioner.NativePeProvisioner;

import java.util.*;
@Getter
@Setter
public class ContainerSchedulerTimeShared extends ContainerScheduler {
    /**
     * The mips map requested.
     */
    private Map<String, List<Double>> mipsMapRequested;

    /**
     * The pes in use.
     */
    private int pesInUse;

    /**
     * Instantiates a new container scheduler time shared.
     *
     * @param pelist the pelist
     */
    public ContainerSchedulerTimeShared(List<NativePe> pelist) {
        super(pelist);
        setMipsMapRequested(new HashMap<String, List<Double>>());
    }

    @Override
    public boolean allocatePesForContainer(Container container, List<Double> mipsShareRequested) {
        if (container.isInMigration()) {
            if (!getContainersMigratingIn().contains(container.getUid()) && !getContainersMigratingOut().contains(container.getUid())) {
                getContainersMigratingOut().add(container.getUid());
            }
        } else {
            if (getContainersMigratingOut().contains(container.getUid())) {
                getContainersMigratingOut().remove(container.getUid());
            }
        }
        boolean result = allocatePesForContainer(container.getUid(), mipsShareRequested);
        updatePeProvisioning();
        return result;
    }

    protected boolean allocatePesForContainer(String containerUid, List<Double> mipsShareRequested) {
        double totalRequestedMips = 0;
        double peMips = getPeCapacity();
        for (Double mips : mipsShareRequested) {
            // each virtual PE of a CONTAINER must require not more than the capacity of a physical PE
            if (mips > peMips) {
                return false;
            }
            totalRequestedMips += mips;
        }

        // This scheduler does not allow over-subscription
        if (getAvailableMips() < totalRequestedMips) {
            return false;
        }

        getMipsMapRequested().put(containerUid, mipsShareRequested);
        setPesInUse(getPesInUse() + mipsShareRequested.size());

        if (getContainersMigratingIn().contains(containerUid)) {
            // the destination host only experience 10% of the migrating CONTAINER's MIPS
            totalRequestedMips *= 0.1;
        }

        List<Double> mipsShareAllocated = new ArrayList<Double>();
        for (Double mipsRequested : mipsShareRequested) {
            if (getContainersMigratingOut().contains(containerUid)) {
                // performance degradation due to migration = 10% MIPS
                mipsRequested *= 0.9;
            } else if (getContainersMigratingIn().contains(containerUid)) {
                // the destination host only experience 10% of the migrating CONTAINER's MIPS
                mipsRequested *= 0.1;
            }
            mipsShareAllocated.add(mipsRequested);
        }

        getMipsMap().put(containerUid, mipsShareAllocated);
        setAvailableMips(getAvailableMips() - totalRequestedMips);

        return true;
    }

    /**
     * Update allocation of CONTAINERs on PEs.
     */
    protected void updatePeProvisioning() {
        getPeMap().clear();
        for (NativePe pe : getPeList()) {
            pe.getNativePeProvisioner().deallocateMipsForAllEntities();
        }

        Iterator<NativePe> peIterator = (Iterator<NativePe>) getPeList().iterator();
        NativePe pe = peIterator.next();
        NativePeProvisioner peProvisioner = pe.getNativePeProvisioner();
        double availableMips = peProvisioner.getAvailableMips();

        for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
            String containerUid = entry.getKey();
            getPeMap().put(containerUid, new LinkedList<NativePe>());

            for (double mips : entry.getValue()) {
                while (mips >= 0.1) {
                    if (availableMips >= mips) {
                        peProvisioner.allocateMipsForEntity(containerUid, mips);
                        getPeMap().get(containerUid).add(pe);
                        availableMips -= mips;
                        break;
                    } else {
                        peProvisioner.allocateMipsForEntity(containerUid, availableMips);
                        getPeMap().get(containerUid).add(pe);
                        mips -= availableMips;
                        if (mips <= 0.1) {
                            break;
                        }
                        if (!peIterator.hasNext()) {
                            Log.printLine("There is no enough MIPS (" + mips + ") to accommodate CONTAINER " + containerUid);
                            // System.exit(0);
                        }
                        pe = peIterator.next();
                        peProvisioner = pe.getNativePeProvisioner();
                        availableMips = peProvisioner.getAvailableMips();
                    }
                }
            }
        }
    }


    @Override
    public void deallocatePesForContainer(Container container) {
        getMipsMapRequested().remove(container.getUid());
        setPesInUse(0);
        getMipsMap().clear();
        setAvailableMips(PeList.getTotalMips(getPeList()));

        for (NativePe pe : getPeList()) {
            pe.getNativePeProvisioner().deallocateMipsForEntity(container);
        }

        for (Map.Entry<String, List<Double>> entry : getMipsMapRequested().entrySet()) {
            allocatePesForContainer(entry.getKey(), entry.getValue());
        }

        updatePeProvisioning();
    }

    /**
     * Releases PEs allocated to all the CONTAINERs.
     *
     * @pre $none
     * @post $none
     */
    @Override
    public void deallocatePesForAllContainers() {
        super.deallocatePesForAllContainers();
        getMipsMapRequested().clear();
        setPesInUse(0);
    }
    
    @Override
    public double getMaxAvailableMips() {
        return getAvailableMips();
    }


}
