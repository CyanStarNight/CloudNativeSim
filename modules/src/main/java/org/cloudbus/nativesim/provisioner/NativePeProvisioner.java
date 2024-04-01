package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cloudbus.nativesim.service.Instance;
import org.cloudbus.nativesim.extend.NativePe;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public abstract class NativePeProvisioner {

	/** The peList. */
	private List<? extends NativePe> peList;

	/** The mips of vm. */
	private double mips;

	public void init(List<? extends NativePe> peList){
		setPeList(peList);
		setMips(peList.get(0).getMips());
	}

	/**
	 * Allocates PEs for a instance.
	 */

	public abstract boolean allocatePeForInstance(Instance instance, int share);

	/**
	 * Releases PEs allocated to instance.
	 */
	public abstract void deallocatePeForInstance(Instance instance);

	/**
	 * Gets the pes allocated for instance.
	 */
	public NativePe getPeAllocatedForInstance(Instance instance) {
		return instance.getCurrentAllocatedPe();
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given instance.
	 */
	public abstract void deallocatePesForAllInstances();


	public double getTotalMips(List<? extends NativePe> peList) {
		double totalMips = 0;

		NativePe pe;
		for(Iterator i$ = peList.iterator(); i$.hasNext(); totalMips += pe.getMips()) {
			pe = (NativePe)i$.next();
		}

		return totalMips;
	}



}
