package com.magerman.nrpc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ValueComparator.
 */
public class ValueComparator implements Comparator<Long> {

    /** The base. */
    Map<Long, Line> base;

    /**
     * Instantiates a new value comparator.
     * 
     * @param allLines
     *            the all lines
     */
    public ValueComparator(final HashMap<Long, Line> allLines) {
	this.base = allLines;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public final int compare(final Long a, final Long b) {
	if (base.get(a).getDurationInMilliseconds() >= base.get(b)
		.getDurationInMilliseconds()) {
	    return -1;
	} else {
	    return 1;
	} // returning 0 would merge keys
    }
}
