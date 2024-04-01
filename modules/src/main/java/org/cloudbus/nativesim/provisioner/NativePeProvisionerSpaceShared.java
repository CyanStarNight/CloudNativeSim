//package org.cloudbus.nativesim.provisioner;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.cloudbus.nativesim.core.Logger;
//import org.cloudbus.nativesim.service.Instance;
//import org.cloudbus.nativesim.extend.NativePe;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Getter
//@Setter
//public class NativePeProvisionerSpaceShared extends NativePeProvisioner {
//
//	/** The pe table. */
//	private Map<String , Double> mipsTable; // instance Uid -> mips
//
//	public NativePeProvisionerSpaceShared() {
//		setMipsTable(new HashMap<>());
//	}
//
//	@Override
//	public boolean allocatePeForInstance(String instanceUid, int share) {
//
//		if (getMaxAvailableMips() < getMips() || share > 1024) {
//			return false;
//		}
//
//		for (NativePe pe:getPeList()){
//
//			double availableMips = pe.getAvailableMips();
//			double allocatedMips;
//			double
//
//			if (getMipsTable().containsKey(instanceUid)) {
//				allocatedMips = getMipsTable().get(instanceUid);
//			} else if (M) {
//				allocatedMips = getMips()*share/1024;
//			}
//
//
//			setAvailableMips(getAvailableMips() - share);
//			getMipsTable().put(instanceUid, allocatedMips);
//		}
//
//
//		return true;
//	}
//
//	@Override
//	public boolean allocatePeForInstance(Instance instance, int share) {
//		return allocatePeForInstance(instance.getUid(),share);
//	}
//
//
//	@Override
//	public void deallocatePesForAllInstances() {
//		super.deallocatePesForAllInstances();
//		getMipsTable().clear();
//	}
//
//
//	@Override
//	public void deallocatePeForInstance(Instance instance) {
//		if (getMipsTable().containsKey(instance.getUid())) {
//			for (double mips : getMipsTable().get(instance.getUid())) {
//				setAvailableMips(getAvailableMips() + mips);
//			}
//			getMipsTable().remove(instance.getUid());
//		}
//	}
//
//
//	public double getMaxAvailableMips(){
//		if (getPeList() == null) {
//			Logger.printLine("Pe list is empty");
//			return 0;
//		}
//
//		double max = 0.0;
//		for (NativePe pe : getPeList()) {
//			double tmp = pe.getMips()* pe.getAvailableShare();
//			if (tmp > max) {
//				max = tmp;
//			}
//		}
//
//		return max;
//	}
//
//}
