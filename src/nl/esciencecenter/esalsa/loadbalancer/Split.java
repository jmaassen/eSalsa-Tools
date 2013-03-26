package nl.esciencecenter.esalsa.loadbalancer;

import java.util.Collection;

import nl.esciencecenter.esalsa.util.Set;

public abstract class Split {
	
	/** The set to split */
	protected final Set set;
	
	/** The number of subsets to create. */
	protected final int parts;
	
	/**
	 * Create a Split for the given set. 
	 * 
	 * @param set the set to split.
	 * @param subsets the number of subsets to split the set into. 
	 */
	protected Split(Set set, int subsets) { 
		this.set = set;
		this.parts = subsets;
	}
	
	/**
	 * Perform the split of the set into subsets and store these in the provided Collection.
	 *  
	 * @param result
	 */
	public abstract void split(Collection<Set> result);
}
