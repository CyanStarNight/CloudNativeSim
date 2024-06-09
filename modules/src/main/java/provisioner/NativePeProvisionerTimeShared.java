/*
 * Copyright ©2024. Jingfeng Wu.
 */

package provisioner;

import lombok.Getter;
import lombok.Setter;


import org.apache.commons.lang3.tuple.Pair;
import entity.Instance;
import extend.NativePe;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class NativePeProvisionerTimeShared extends NativePeProvisioner {

	/** The pe table. */
//	private Map<String , Integer> shareTable;

	private Map<String, Pair< NativePe, Integer >> peTable; // instance Uid -> pe & share

	public NativePeProvisionerTimeShared() {
//		setShareTable(new HashMap<>());
		setPeTable(new HashMap<>());
	}

	public void updateInstancePe(Instance instance, NativePe pe ,int share){

		instance.setCurrentAllocatedPe(pe);
//		System.out.println(pe);
		if (pe == null) instance.setCurrentAllocatedMips(0);
		else instance.setCurrentAllocatedMips(pe.getMips());
		instance.setCurrentAllocatedCpuShare(share);

	}

	@Override
	public boolean allocatePeForInstance(Instance instance, int requestShare) {
		boolean result = false;
		NativePe allocatedPe;
		int allocatedShare;
		String instanceUid = instance.getUid();

		// 如果已经分配
		if(getPeTable().containsKey(instanceUid)){

			allocatedPe = getPeTable().get(instanceUid).getLeft();
			allocatedShare = getPeTable().get(instanceUid).getRight();

			if (requestShare <= allocatedShare + allocatedPe.getAvailableShare()){

				allocatedPe.addAvailableShare(allocatedShare-requestShare);
				getPeTable().replace(instanceUid, Pair.of(allocatedPe,requestShare));
				updateInstancePe(instance,allocatedPe,requestShare);

				return true;

			}else return false;

		}else {

			for (NativePe pe : getPeList()) {

				if (pe.getAvailableShare() >= requestShare) {

					getPeTable().put(instanceUid, Pair.of(pe, requestShare));
					pe.setAvailableShare(pe.getAvailableShare() - requestShare);

					updateInstancePe(instance,pe,requestShare);
					result = true;
					break;

				}
			}
		}

		return result;
	}


	@Override
	public void deallocatePeForInstance(Instance instance) {

		if(getPeTable().containsKey(instance.getUid())){

			Pair<NativePe,Integer> pair = getPeTable().remove(instance.getUid());
			NativePe allocatedPe = pair.getLeft();
			int allocatedShare = pair.getRight();
			allocatedPe.addAvailableShare(allocatedShare);

		}
		updateInstancePe(instance,null,0);

	}

	@Override
	public void deallocatePesForAllInstances(){

		getPeTable().clear();

		for (Instance instance : Instance.getAllInstances()){
			updateInstancePe(instance,null,0);
		}

		for (NativePe pe: getPeList()) pe.clear();
	}

}
