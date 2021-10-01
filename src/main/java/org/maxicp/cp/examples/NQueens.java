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

package org.maxicp.cp.examples;

import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.Procedure;

import java.util.Arrays;

import static org.maxicp.cp.CPFactory.minus;
import static org.maxicp.cp.CPFactory.notEqual;

/**
 * The N-Queens problem.
 * <a href="http://csplib.org/Problems/prob054/">CSPLib</a>.
 */
public class NQueens {
    public static void main(String[] args) {
        int n = 8;
        CPSolver cp = CPFactory.makeSolver(false);
        CPIntVar[] q = CPFactory.makeIntVarArray(cp, n, n);


        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++) {
                cp.post(CPFactory.notEqual(q[i], q[j]));

                cp.post(CPFactory.notEqual(q[i], q[j], j - i));
                cp.post(CPFactory.notEqual(q[i], q[j], i - j));
                // alternative modeling using views
                // cp.post(notEqual(plus(q[i], j - i), q[j]));
                // cp.post(notEqual(minus(q[i], j - i), q[j]));

            }



        DFSearch search = CPFactory.makeDfs(cp, () -> {
            int idx = -1; // index of the first variable that is not bound
            for (int k = 0; k < q.length; k++)
                if (q[k].size() > 1) {
                    idx = k;
                    break;
                }
            if (idx == -1)
                return new Procedure[0];
            else {
                CPIntVar qi = q[idx];
                int v = qi.min();
                Procedure left = () -> cp.post(CPFactory.equal(qi, v));
                Procedure right = () -> cp.post(CPFactory.notEqual(qi, v));
                return new Procedure[]{left, right};
            }
        });

        // a more compact first fail search using selectors is given next
/*
        DFSearch search = Factory.makeDfs(cp, () -> {
            IntVar qs = selectMin(q,
                    qi -> qi.size() > 1,
                    qi -> qi.size());
            if (qs == null) return EMPTY;
            else {
                int v = qs.min();
                return branch(() -> cp.post(Factory.equal(qs, v)),
                        () -> cp.post(Factory.notEqual(qs, v)));
            }
        });*/


        search.onSolution(() ->
                System.out.println("solution:" + Arrays.toString(q))
        );
        SearchStatistics stats = search.solve(statistics -> statistics.numberOfSolutions() == 1000);

        System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
        System.out.format("Statistics: %s\n", stats);

    }
}
