package org.maxicp.model;

import org.maxicp.model.impls.IntVarImpl;
import org.maxicp.model.impls.ModelImpl;

public final class Factory {
    private Factory() {}

    static public Model model() {
        return new ModelImpl();
    }

    /**
     * Create an array of n IntVars with domain between 0 and domSize-1, inclusive.
     * @param n size of the array, number of IntVars
     * @param domSize size of the domains. Domains are [0, domsize-1]
     */
    static public IntVar[] intVarArray(int n, int domSize) {
        IntVar[] out = new IntVar[n];
        for(int i = 0; i < n; i++)
            out[i] = new IntVarImpl(0, domSize-1);
        return out;
    }
}
