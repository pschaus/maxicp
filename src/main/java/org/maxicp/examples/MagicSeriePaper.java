/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package org.maxicp.examples;

import org.maxicp.cp.Factory;
import org.maxicp.engine.core.IntVar;
import org.maxicp.engine.core.Solver;
import org.maxicp.search.DFSearch;

import java.util.Arrays;
import org.maxicp.util.Procedure;

/**
 * The Magic Series problem.
 * <a href="http://csplib.org/Problems/prob019/">CSPLib</a>.
 */
public class MagicSeriePaper {
    public static void main(String[] args) {
        int n = 8;
        Solver cp = Factory.makeSolver(false);
        IntVar[] s = Factory.makeIntVarArray(cp, n, n);

        for (int i = 0; i < n; i++) {
            final int fi = i;
            cp.post(Factory.sum(Factory.makeIntVarArray(n, j -> Factory.isEqual(s[j], fi)), s[i]));
        }
        cp.post(Factory.sum(s, n));
        cp.post(Factory.sum(Factory.makeIntVarArray(n, i -> Factory.mul(s[i], i)), n));

        DFSearch dfs = Factory.makeDfs(cp, () -> {
            int idx = -1; // index of the first variable that is not bound
            for (int k = 0; k < s.length; k++)
                if (s[k].size() > 1) {
                    idx = k;
                    break;
                }
            if (idx == -1)
                return new Procedure[0];
            else {
                IntVar si = s[idx];
                int v = si.min();
                Procedure left = () -> cp.post(Factory.equal(si, v));
                Procedure right = () -> cp.post(Factory.notEqual(si, v));
                return new Procedure[]{left, right};
            }                
        });

        dfs.onSolution(() ->
                System.out.println("solution:" + Arrays.toString(s))
        );
        dfs.solve();
    }
}
