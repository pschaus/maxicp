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

import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.util.io.InputReader;

import java.util.Random;
import java.util.stream.IntStream;

import static org.maxicp.BranchingScheme.firstFail;
import static org.maxicp.cp.CPFactory.*;

/**
 * The Quadratic Assignment problem.
 * There are a set of n facilities and a set of n locations.
 * For each pair of locations, a distance is specified and for
 * each pair of facilities a weight or flow is specified
 * (e.g., the amount of supplies transported between the two facilities).
 * The problem is to assign all facilities to different locations
 * with the goal of minimizing the sum of the distances multiplied
 * by the corresponding flows.
 * <a href="https://en.wikipedia.org/wiki/Quadratic_assignment_problem">Wikipedia</a>.
 */
public class QAPLNS {

    public static void main(String[] args) {

        // ---- read the instance -----

        InputReader reader = new InputReader("data/qap.txt");

        int n = reader.getInt();
        // Weights
        int[][] w = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                w[i][j] = reader.getInt();
            }
        }
        // Distance
        int[][] d = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = reader.getInt();
            }
        }

        // ----- build the model ---

        CPSolver cp = makeSolver();
        CPIntVar[] x = makeIntVarArray(cp, n, n);

        cp.post(allDifferent(x));


        // build the objective function
        CPIntVar[] weightedDist = new CPIntVar[n * n];
        int ind = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                CPIntVar distXiXj = element(d, x[i], x[j]);
                weightedDist[ind] = mul(distXiXj, w[i][j]);
                ind++;
            }
        }
        CPIntVar totCost = sum(weightedDist);
        Objective obj = cp.minimize(totCost);

        DFSearch dfs = makeDfs(cp, firstFail(x));


        // --- Large Neighborhood Search ---

        // Current best solution
        int[] xBest = IntStream.range(0, n).toArray();
        /*        int[] xBest = new int[n];
        for (int i = 0; i < n; i++) {
            xBest[i] = i;
            }*/

        dfs.onSolution(() -> {
            // Update the current best solution
            for (int i = 0; i < n; i++) {
                xBest[i] = x[i].min();
            }
            System.out.println("objective:" + totCost.min());
        });


        int nRestarts = 1000;
        int failureLimit = 100;
        Random rand = new java.util.Random(0);

        for (int i = 0; i < nRestarts; i++) {
            if (i % 10 == 0)
                System.out.println("restart number #" + i);

            dfs.optimizeSubjectTo(obj, statistics -> statistics.numberOfFailures() >= failureLimit, () -> {
                        // Assign the fragment 5% of the variables randomly chosen
                        for (int j = 0; j < n; j++) {
                            if (rand.nextInt(100) < 5) {
                                // after the solveSubjectTo those constraints are removed
                                cp.post(equal(x[j], xBest[j]));
                            }
                        }
                    }
            );
        }
    }
}
