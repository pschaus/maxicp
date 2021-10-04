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
import org.maxicp.cp.engine.constraints.TableCT;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.io.InputReader;

import java.util.Arrays;

import static org.maxicp.BranchingScheme.and;
import static org.maxicp.BranchingScheme.firstFail;
import static org.maxicp.cp.CPFactory.*;

/**
 *
 *  The Eternity II puzzle is an edge-matching puzzle which
 *  involves placing 256 square puzzle pieces into a 16 by 16 grid,
 *  constrained by the requirement to match adjacent edges.
 *  <a href="https://en.wikipedia.org/wiki/Eternity_II_puzzle">Wikipedia.</a>
 */
public class Eternity {

    public static CPIntVar[] flatten(CPIntVar[][] x) {
        return Arrays.stream(x).flatMap(Arrays::stream).toArray(CPIntVar[]::new);
    }

    public static void main(String[] args) {

        // Reading the data

        InputReader reader = new InputReader("data/eternity/eternity7x7.txt");

        int n = reader.getInt();
        int m = reader.getInt();

        int[][] pieces = new int[n * m][4];
        int maxTmp = 0;

        for (int i = 0; i < n * m; i++) {
            for (int j = 0; j < 4; j++) {
                pieces[i][j] = reader.getInt();
                if (pieces[i][j] > maxTmp)
                    maxTmp = pieces[i][j];
            }
            System.out.println(Arrays.toString(pieces[i]));
        }
        final int max = maxTmp;

        // ------------------------

        // TODO: create the table where each line correspond to one possible rotation of a piece
        // For instance if the line piece[6] = [2,3,5,1]
        // the four lines created in the table are
        // [6,2,3,5,1] // rotation of 0°
        // [6,3,5,1,2] // rotation of 90°
        // [6,5,1,2,3] // rotation of 180°
        // [6,1,2,3,5] // rotation of 270°

        // Table with makeIntVarArray pieces and for each their 4 possible rotations

        int[][] table = new int[4 * n * m][5];

        for (int i = 0; i < pieces.length; i++) {
            for (int r = 0; r < 4; r++) {
                table[i * 4 + r][0] = i;
                table[i * 4 + r][1] = pieces[i][(r + 0) % 4];
                table[i * 4 + r][2] = pieces[i][(r + 1) % 4];
                table[i * 4 + r][3] = pieces[i][(r + 2) % 4];
                table[i * 4 + r][4] = pieces[i][(r + 3) % 4];
            }
        }

        CPSolver cp = makeSolver();

        //   |         |
        // - +---------+- -
        //   |    u    |
        //   | l  i  r |
        //   |    d    |
        // - +---------+- -
        //   |         |


        CPIntVar[][] id = new CPIntVar[n][m]; // id
        CPIntVar[][] u = new CPIntVar[n][m];  // up
        CPIntVar[][] r = new CPIntVar[n][m];  // right
        CPIntVar[][] d = new CPIntVar[n][m];  // down
        CPIntVar[][] l = new CPIntVar[n][m];  // left

        for (int i = 0; i < n; i++) {
            u[i] = CPFactory.makeIntVarArray(m, j -> makeIntVar(cp, 0, max));
            id[i] = makeIntVarArray(cp, m, n * m);
        }
        for (int k = 0; k < n; k++) {
            final int i = k;
            if (i < n - 1) d[i] = u[i + 1];
            else d[i] = CPFactory.makeIntVarArray(m, j -> makeIntVar(cp, 0, max));
        }
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                l[i][j] = makeIntVar(cp, 0, max);
            }
        }
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                if (j < m - 1) r[i][j] = l[i][j + 1];
                else r[i][j] = makeIntVar(cp, 0, max);
            }
        }

        // The constraints of the problem

        // Constraint1: all the pieces placed are different

        // Constraint2: all the pieces placed are valid ones i.e. one of the given mxn pieces possibly rotated

        // Constraint3: place "0" one all external side of the border (gray color)

        // all the the pieces placed are different
        cp.post(allDifferent(flatten(id)));

        // make the pieces placed are valid ones (one of the given mxn piece possibly rotated)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                //cp.post(new TableCT(new IntVar[]{id[i][j],u[i][j],r[i][j],d[i][j],l[i][j]},table));
                cp.post(new TableCT(new CPIntVar[]{id[i][j],u[i][j],r[i][j],d[i][j],l[i][j]},table));
                //cp.post(new TableDecomp(new IntVar[]{id[i][j], u[i][j], r[i][j], d[i][j], l[i][j]}, table));
            }
        }

        // 0 on the border
        for (int i = 0; i < n; i++) {
            cp.post(equal(l[i][0], 0));
            cp.post(equal(r[i][m - 1], 0));
        }
        for (int j = 0; j < m; j++) {
            cp.post(equal(u[0][j], 0));
            cp.post(equal(d[n - 1][j], 0));
        }


        // The search using the and combinator

        DFSearch dfs = makeDfs(cp,
                and(firstFail(flatten(id)),
                        firstFail(flatten(u)),
                        firstFail(flatten(r)),
                        firstFail(flatten(d)),
                        firstFail(flatten(l)))
        );


        dfs.onSolution(() -> {
            System.out.println("----------------");
            // Pretty Print
            for (int i = 0; i < n; i++) {
                String line = "   ";
                for (int j = 0; j < m; j++) {
                    line += u[i][j].min() + "   ";
                }
                System.out.println(line);
                line = " ";
                for (int j = 0; j < m; j++) {
                    line += l[i][j].min() + "   ";
                }
                line += r[i][m - 1].min();
                System.out.println(line);
            }
            String line = "   ";
            for (int j = 0; j < m; j++) {
                line += d[n - 1][j].min() + "   ";
            }
            System.out.println(line);

        });

        long t0 = System.currentTimeMillis();
        SearchStatistics stats = dfs.solve(statistics -> statistics.numberOfSolutions() == 5);

        System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
        System.out.format("Statistics: %s\n", stats);
        System.out.format("time: %s\n", System.currentTimeMillis()-t0);


    }
}
