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
import org.maxicp.search.SearchStatistics;

import java.util.Arrays;

import static org.maxicp.cp.BranchingScheme.*;
import static org.maxicp.cp.Factory.*;

/**
 * The Magic Series problem.
 * <a href="http://csplib.org/Problems/prob019/">CSPLib</a>.
 */
public class MagicSerie {
    public static void main(String[] args) {

        int n = 300;
        Solver cp = makeSolver(false);

        IntVar[] s = makeIntVarArray(cp, n, n);

        for (int i = 0; i < n; i++) {
            final int fi = i;
            cp.post(sum(Factory.makeIntVarArray(n, j -> isEqual(s[j], fi)), s[i]));
        }
        cp.post(sum(s, n));
        cp.post(sum(Factory.makeIntVarArray(n, i -> mul(s[i], i)), n));
        cp.post(sum(makeIntVarArray(n - 1, i -> mul(s[i], i - 1)), 0));

        long t0 = System.currentTimeMillis();
        DFSearch dfs = makeDfs(cp, () -> {
            IntVar sv = selectMin(s,
                    si -> si.size() > 1,
                    si -> -si.size());
            if (sv == null) return EMPTY;
            else {
                int v = sv.min();
                return branch(() -> cp.post(equal(sv, v)),
                        () -> cp.post(notEqual(sv, v)));
            }
        });

        dfs.onSolution(() ->
                System.out.println("solution:" + Arrays.toString(s))
        );

        SearchStatistics stats = dfs.solve();

        long t1 = System.currentTimeMillis();

        System.out.println(t1 - t0);

        System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
        System.out.format("Statistics: %s\n", stats);

    }
}
