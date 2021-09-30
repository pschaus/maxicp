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

import org.maxicp.engine.constraints.Circuit;
import org.maxicp.engine.constraints.Element1D;
import org.maxicp.engine.core.IntVar;
import org.maxicp.engine.core.Solver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.io.InputReader;

import static org.maxicp.cp.BranchingScheme.*;
import static org.maxicp.cp.Factory.*;

/**
 * Traveling salesman problem.
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">Wikipedia</a>.
 */
public class TSPBoundImpact {


    /**
     * Fages, J. G., & Prud’Homme, C. Making the first solution good! In 2017 IEEE 29th International Conference on Tools with Artificial Intelligence (ICTAI). IEEE.
     * @param x
     * @param obj
     * @return the value that if assigned to v induced the least augmentation of the objective obj
     */
    public static int boundImpactValueSelector(IntVar x, IntVar obj) {
        // STUDENT throw new NotImplementedException("boundImpactValueSelector");
        // BEGIN STRIP
        int val = x.min();
        int best = Integer.MAX_VALUE;
        for (int v = x.min(); v < x.max(); v++) {
            if (x.contains(v)) {
                x.getSolver().getStateManager().saveState();
                try {
                    x.getSolver().post(equal(x,v));
                    if ((obj.min()) < best) {
                        val = v;
                        best = obj.min();
                    }

                } catch (InconsistencyException e) {
                }
                x.getSolver().getStateManager().restoreState();
            }
        }
        return val;
        // END STRIP
    }


    public static void main(String[] args) {


        // instance gr17 https://people.sc.fsu.edu/~jburkardt/datasets/tsp/gr17_d.txt
        InputReader reader = new InputReader("data/tsp.txt");

        int n = reader.getInt();

        int[][] distanceMatrix = reader.getMatrix(n, n);

        Solver cp = makeSolver(false);
        IntVar[] succ = makeIntVarArray(cp, n, n);
        IntVar[] distSucc = makeIntVarArray(cp, n, 1000);

        cp.post(new Circuit(succ));

        for (int i = 0; i < n; i++) {
            cp.post(new Element1D(distanceMatrix[i], succ[i], distSucc[i]));
        }

        IntVar totalDist = sum(distSucc);

        Objective obj = cp.minimize(totalDist);

        DFSearch dfs = makeDfs(cp, () -> {
            IntVar xs = selectMin(succ,
                    xi -> xi.size() > 1,
                    xi -> xi.size());
            if (xs == null)
                return EMPTY;
            else {
                //int v = boundImpactValueSelector(xs,totalDist);// now the first solution should have objective 2561
                int v = xs.min(); // the first solution should have objective 4722
                return branch(() -> cp.post(equal(xs, v)),
                        () -> cp.post(notEqual(xs, v)));
            }
        });

        dfs.onSolution(() ->
                System.out.println(totalDist)
        );

        SearchStatistics stats = dfs.optimize(obj);

        System.out.println(stats);


    }
}
