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
 * Copyright (c)  2017. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package org.maxicp.examples;

import org.maxicp.engine.core.IntVar;
import org.maxicp.engine.core.Solver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;

import java.util.Arrays;

import static org.maxicp.cp.BranchingScheme.*;
import static org.maxicp.cp.Factory.*;

public class NQueensPerformance {
    public static void main(String[] args) {
        int n = 88;
        Solver cp = makeSolver();
        IntVar[] q = makeIntVarArray(cp, n, n);

        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++) {
                cp.post(notEqual(q[i], q[j]));
                cp.post(notEqual(plus(q[i], j - i), q[j]));
                cp.post(notEqual(minus(q[i], j - i), q[j]));
            }


        long t0 = System.currentTimeMillis();


        DFSearch dfs = makeDfs(cp, () -> {
            IntVar qs = selectMin(q,
                    qi -> qi.size() > 1,
                    qi -> qi.size());
            if (qs == null)
                return EMPTY;
            else {
                int v = qs.min();
                //return branch(() -> equal(qs, v), () -> notEqual(qs, v));
                return branch(() -> cp.post(equal(qs, v)), () -> cp.post(notEqual(qs, v)));
            }
        });

        dfs.onSolution(() ->
                System.out.println("solution:" + Arrays.toString(q))
        );


        SearchStatistics stats = dfs.solve(statistics -> {
            if ((statistics.numberOfNodes() / 2) % 10000 == 0) {
                //System.out.println("failures:"+statistics.nFailures);
                System.out.println("nodes:" + (statistics.numberOfNodes() / 2));
            }
            return statistics.numberOfSolutions() > 0 || statistics.numberOfNodes() >= 1000000;
        });


        System.out.println("time:" + (System.currentTimeMillis() - t0));
        System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
        System.out.format("Statistics: %s\n", stats);

    }
}
