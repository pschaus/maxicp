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

import org.maxicp.cp.engine.constraints.Cumulative;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.io.InputReader;

import static org.maxicp.BranchingScheme.firstFail;
import static org.maxicp.cp.CPFactory.*;


/**
 * Resource Constrained Project Scheduling Problem.
 * <a href="http://www.om-db.wi.tum.de/psplib/library.html">PSPLIB</a>.
 */
public class RCPSP {


    public static void main(String[] args) {

        // Reading the data

        InputReader reader = new InputReader("data/rcpsp/j90_1_1.rcp");

        int nActivities = reader.getInt();
        int nResources = reader.getInt();

        int[] capa = new int[nResources];
        for (int i = 0; i < nResources; i++) {
            capa[i] = reader.getInt();
        }

        int[] duration = new int[nActivities];
        int[][] consumption = new int[nResources][nActivities];
        int[][] successors = new int[nActivities][];


        int horizon = 0;
        for (int i = 0; i < nActivities; i++) {
            // durations, demand for each resource, successors
            duration[i] = reader.getInt();
            horizon += duration[i];
            for (int r = 0; r < nResources; r++) {
                consumption[r][i] = reader.getInt();
            }
            successors[i] = new int[reader.getInt()];
            for (int k = 0; k < successors[i].length; k++) {
                successors[i][k] = reader.getInt() - 1;
            }
        }


        // -------------------------------------------

        // The Model

        CPSolver cp = makeSolver();

        CPIntVar[] start = makeIntVarArray(cp, nActivities, horizon);
        CPIntVar[] end = new CPIntVar[nActivities];


        for (int i = 0; i < nActivities; i++) {
            end[i] = plus(start[i], duration[i]);
        }

        for (int r = 0; r < nResources; r++) {
            cp.post(new Cumulative(start, duration, consumption[r], capa[r]));
        }
        for (int i = 0; i < nActivities; i++) {
            for (int k : successors[i]) {
                // activity i must precede activity k
                cp.post(lessOrEqual(end[i], start[k]));
            }
        }

        CPIntVar makespan = maximum(end);

        Objective obj = cp.minimize(makespan);

        DFSearch dfs = makeDfs(cp, firstFail(start));

        dfs.onSolution(() ->

                System.out.println("makespan:" + makespan)
        );

        SearchStatistics stats = dfs.optimize(obj);

        System.out.format("Statistics: %s\n", stats);
    }
}
