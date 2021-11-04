package org.maxicp.search;

import org.maxicp.model.IntVar;

public class CartesianSpaceEvaluator {
    /**
     * Computes the logarithm of the size of the cartesian product of all the variables' domains.
     *
     * @param vars IntVars on which to compute the cartesian product
     * @return the log of the space of all possible solutions (without constraints)
     */
    public static double evaluate(IntVar[] vars) {
        double out = 0.0;
        for(IntVar i: vars)
            out += Math.log(i.size());
        return out;
    }
}
