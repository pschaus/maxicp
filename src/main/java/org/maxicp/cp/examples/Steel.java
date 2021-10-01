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
import org.maxicp.cp.engine.constraints.IsOr;
import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.io.InputReader;
import org.maxicp.util.Procedure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.maxicp.BranchingScheme.*;
import static org.maxicp.cp.CPFactory.*;

/**
 * Steel is produced by casting molten iron into slabs.
 * A steel mill can produce a finite number of slab sizes.
 * An order has two properties, a colour corresponding to the route required through the steel mill and a weight.
 * Given d input orders, the problem is to assign the orders to slabs, the number and size of which are also to be determined,
 * such that the total weight of steel produced is minimised.
 * This assignment is subject to two further constraints:
 * - Capacity constraints: The total weight of orders assigned to a slab cannot exceed the slab capacity.
 * - Colour constraints: Each slab can contain at most p of k total colours (p is usually 2).
 * <a href="http://www.csplib.org/Problems/prob038/">CSPLib</a>
 */
public class Steel {


    public static void main(String[] args) {

        // Reading the data

        InputReader reader = new InputReader("data/steel/bench_19_10");
        int nCapa = reader.getInt();
        int[] capa = new int[nCapa];
        for (int i = 0; i < nCapa; i++) {
            capa[i] = reader.getInt();
        }
        int maxCapa = capa[capa.length - 1];
        int[] loss = new int[maxCapa + 1];
        int capaIdx = 0;
        for (int i = 0; i < maxCapa; i++) {
            loss[i] = capa[capaIdx] - i;
            if (loss[i] == 0) capaIdx++;
        }
        loss[0] = 0;

        int nCol = reader.getInt();
        int nSlab = reader.getInt();
        int nOrder = nSlab;
        int[] w = new int[nSlab];
        int[] c = new int[nSlab];
        for (int i = 0; i < nSlab; i++) {
            w[i] = reader.getInt();
            c[i] = reader.getInt() - 1;
        }

        // ---------------------------

        try {


            CPSolver cp = makeSolver();
            CPIntVar[] x = makeIntVarArray(cp, nOrder, nSlab);
            CPIntVar[] l = makeIntVarArray(cp, nSlab, maxCapa + 1);

            CPBoolVar[][] inSlab = new CPBoolVar[nSlab][nOrder]; // inSlab[j][i] = 1 if order i is placed in slab j

            for (int j = 0; j < nSlab; j++) {
                for (int i = 0; i < nOrder; i++) {
                    inSlab[j][i] = isEqual(x[i], j);
                }
            }


            for (int j = 0; j < nSlab; j++) {
                // for each color, is it present in the slab
                CPIntVar[] presence = new CPIntVar[nCol];

                for (int col = 0; col < nCol; col++) {
                    presence[col] = makeBoolVar(cp);

                    ArrayList<CPBoolVar> inSlabWithColor = new ArrayList<>();
                    for (int i = 0; i < nOrder; i++) {
                        if (c[i] == col) inSlabWithColor.add(inSlab[j][i]);
                    }

                    // TODO 2: model that presence[col] is true iff at least one order with color col is placed in slab j
                    // STUDENT
                    // BEGIN STRIP
                    cp.post(new IsOr((CPBoolVar) presence[col], inSlabWithColor.toArray(new CPBoolVar[0])));
                    // END STRIP
                }
                // TODO 3: restrict the number of colors present in slab j to be <= 2
                // STUDENT
                // BEGIN STRIP
                cp.post(lessOrEqual(sum(presence), 2));
                // END STRIP
            }


            // bin packing constraint
            for (int j = 0; j < nSlab; j++) {
                CPIntVar[] wj = new CPIntVar[nSlab];
                for (int i = 0; i < nOrder; i++) {
                    wj[i] = mul(inSlab[j][i], w[i]);
                }
                cp.post(sum(wj, l[j]));
            }

            // TODO 4: add the redundant constraint that the sum of the loads is equal to the sum of elements
            // STUDENT
            // BEGIN STRIP
            cp.post(sum(l, IntStream.of(w).sum()));
            // END STRIP

            // TODO 5: model the objective function using element constraint + a sum constraint
            CPIntVar totLoss = null;
            // STUDENT
            // BEGIN STRIP
            CPIntVar[] losses = CPFactory.makeIntVarArray(nSlab, j -> element(loss, l[j]));
            totLoss = sum(losses);
            // END STRIP

            Objective obj = cp.minimize(totLoss);


            // TODO 6 add static symmetry breaking constraint

            // TODO 7 implement a dynamic symmetry breaking during search
            // STUDENT DFSearch dfs = makeDfs(cp,firstFail(x));
            // BEGIN STRIP
            DFSearch dfs = makeDfs(cp,
                    () -> {
                        CPIntVar xs = selectMin(x,
                                xi -> xi.size() > 1,
                                xi -> xi.size());
                        if (xs == null) return EMPTY;
                        else {
                            int maxUsed = -1;
                            for (CPIntVar xi : x)
                                if (xi.isBound() && xi.min() > maxUsed)
                                    maxUsed = xi.min();
                            Procedure[] branches = new Procedure[maxUsed + 2];
                            for (int i = 0; i <= maxUsed + 1; i++) {
                                final int slab = i;
                                branches[i] = () -> cp.post(equal(xs, slab));
                            }
                            return branch(branches);
                        }
                    });
            // END STRIP
            dfs.onSolution(() -> {
                System.out.println("---");
                //System.out.println(totLoss);

                Set<Integer>[] colorsInSlab = new Set[nSlab];
                for (int j = 0; j < nSlab; j++) {
                    colorsInSlab[j] = new HashSet<>();
                }
                for (int i = 0; i < nOrder; i++) {
                    colorsInSlab[x[i].min()].add(c[i]);
                }
                for (int j = 0; j < nSlab; j++) {
                    if (colorsInSlab[j].size() > 2) {
                        System.out.println("problem, " + colorsInSlab[j].size() + " colors in slab " + j + " should be <= 2");
                    }
                }
            });

            SearchStatistics statistics = dfs.optimize(obj);
            System.out.println(statistics);

        } catch (InconsistencyException e) {
            e.printStackTrace();

        }
    }
}
