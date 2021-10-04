package org.maxicp.model;

import org.maxicp.model.impls.ModelImpl;

import java.util.function.Function;

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
    public static IntVar[] intVarArray(int n, int domSize) {
        IntVar[] out = new IntVar[n];
        for(int i = 0; i < n; i++)
            out[i] = new IntVarRangeImpl(0, domSize-1);
        return out;
    }

    public static IntVar[] makeIntVarArray(int n, Function<Integer, IntVar> body) {
        IntVar[] t = new IntVar[n];
        for (int i = 0; i < n; i++)
            t[i] = body.apply(i);
        return t;
    }
}
