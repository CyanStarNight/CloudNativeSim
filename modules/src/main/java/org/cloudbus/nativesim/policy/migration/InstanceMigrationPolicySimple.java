/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.policy.migration;

public class InstanceMigrationPolicySimple extends InstanceMigrationPolicy {

    /**
     * Update allocation of instances on PEs.
     */
    @SuppressWarnings("unchecked")
    protected void updatePeProvisioning() {
//        getPeMap().clear();
//        for (NativePe pe : getPeList()) {
//            pe.getPeProvisioner().deallocateMipsForAllInstances();
//        }
//
//        Iterator<NativePe> peIterator = (Iterator<NativePe>) getPeList().iterator();
//        NativePe pe = peIterator.next();
//        InstancePeProvisioner peProvisioner = pe.getPeProvisioner();
//        double availableMips = peProvisioner.getAvailableMips();
//
//        for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
//            String instanceUid = entry.getKey();
//            getPeMap().put(instanceUid, new LinkedList<NativePe>());
//
//            for (double mips : entry.getValue()) {
//                while (mips >= 0.1) {
//                    if (availableMips >= mips) {
//                        peProvisioner.allocateMipsForInstance(instanceUid, mips);
//                        getPeMap().get(instanceUid).add(pe);
//                        availableMips -= mips;
//                        break;
//                    } else {
//                        peProvisioner.allocateMipsForInstance(instanceUid, availableMips);
//                        getPeMap().get(instanceUid).add(pe);
//                        mips -= availableMips;
//                        if (mips <= 0.1) {
//                            break;
//                        }
//                        if (!peIterator.hasNext()) {
//                            Log.printLine("There is no enough MIPS (" + mips + ") to accommodate INSTANCE " + instanceUid);
//                            // System.exit(0);
//                        }
//                        pe = peIterator.next();
//                        peProvisioner = pe.getPeProvisioner();
//                        availableMips = peProvisioner.getAvailableMips();
//                    }
//                }
//            }
//        }
    }
}
