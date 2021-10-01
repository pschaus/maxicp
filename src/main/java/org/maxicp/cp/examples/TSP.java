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

import org.maxicp.cp.engine.constraints.Circuit;
import org.maxicp.cp.engine.constraints.Element1D;
import org.maxicp.cp.engine.core.IntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.util.io.InputReader;
import org.maxicp.search.SearchStatistics;

import static org.maxicp.BranchingScheme.*;
import static org.maxicp.Factory.*;

/**
 * Traveling salesman problem.
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">Wikipedia</a>.
 */
public class TSP {




    public static void main(String[] args) {


        // instance gr17 https://people.sc.fsu.edu/~jburkardt/datasets/tsp/gr17_d.txt
        InputReader reader = new InputReader("data/tsp.txt");

        int n = reader.getInt();

        int[][] distanceMatrix = reader.getMatrix(n, n);

        CPSolver cp = makeSolver(false);
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
                int v = xs.min();
                return branch(() -> xs.getSolver().post(equal(xs, v)),
                        () -> xs.getSolver().post(notEqual(xs, v)));
            }
        });

        dfs.onSolution(() ->
                System.out.println(totalDist)
        );

        SearchStatistics stats = dfs.optimize(obj,s -> s.numberOfSolutions() == 1);
        System.out.println(stats);




    }
}
