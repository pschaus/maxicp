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


import org.maxicp.Factory;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.BranchingScheme;

import java.util.Arrays;

/**
 * The Magic Square problem.
 * <a href="http://csplib.org/Problems/prob019/">CSPLib</a>.
 */
public class MagicSquare {

    //
    public static void main(String[] args) {

        int n = 6;
        int sumResult = n * (n * n + 1) / 2;

        CPSolver cp = Factory.makeSolver();
        CPIntVar[][] x = new CPIntVar[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                x[i][j] = Factory.makeIntVar(cp, 1, n * n);
            }
        }


        CPIntVar[] xFlat = new CPIntVar[x.length * x.length];
        for (int i = 0; i < x.length; i++) {
            System.arraycopy(x[i], 0, xFlat, i * x.length, x.length);
        }


        // AllDifferent
        cp.post(Factory.allDifferent(xFlat));

        // Sum on lines
        for (int i = 0; i < n; i++) {
            cp.post(Factory.sum(x[i], sumResult));
        }

        // Sum on columns
        for (int j = 0; j < x.length; j++) {
            CPIntVar[] column = new CPIntVar[n];
            for (int i = 0; i < x.length; i++)
                column[i] = x[i][j];
            cp.post(Factory.sum(column, sumResult));
        }

        // Sum on diagonals
        CPIntVar[] diagonalLeft = new CPIntVar[n];
        CPIntVar[] diagonalRight = new CPIntVar[n];
        for (int i = 0; i < x.length; i++) {
            diagonalLeft[i] = x[i][i];
            diagonalRight[i] = x[n - i - 1][i];
        }
        cp.post(Factory.sum(diagonalLeft, sumResult));
        cp.post(Factory.sum(diagonalRight, sumResult));

        DFSearch dfs = Factory.makeDfs(cp, BranchingScheme.firstFail(xFlat));

        dfs.onSolution(() -> {
                    for (int i = 0; i < n; i++) {
                        System.out.println(Arrays.toString(x[i]));
                    }
                }
        );

        SearchStatistics stats = dfs.solve(stat -> stat.numberOfSolutions() >= 1); // stop on first solution

        System.out.println(stats);
    }

}
