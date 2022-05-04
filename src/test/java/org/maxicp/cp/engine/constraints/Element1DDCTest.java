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

package org.maxicp.cp.engine.constraints;

import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.maxicp.util.exception.NotImplementedException;
import org.junit.Test;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.Procedure;
import org.maxicp.BranchingScheme;
import org.maxicp.cp.CPFactory;

import java.util.HashSet;
import java.util.Random;
import java.util.function.Supplier;

import static org.maxicp.cp.CPFactory.notEqual;
import static org.junit.Assert.*;

public class Element1DDCTest extends CPSolverTest {

    @Test
    public void element1dTest1() {
        try {
            CPSolver cp = solverFactory.get();

            Random rand = new Random(678);
            CPIntVar y = CPFactory.makeIntVar(cp, 0, 100);
            CPIntVar z = CPFactory.makeIntVar(cp, 0, 100);


            int[] T = new int[70];
            for (int i = 0; i < 70; i++)
                T[i] = rand.nextInt(100);

            cp.post(new Element1DDomainConsistent(T, y, z));

            assertTrue(y.max() < 70);

            Supplier<Procedure[]> branching = () -> {
                if (y.isFixed() && z.isFixed()) {
                    assertEquals(T[y.min()], z.min());
                    return BranchingScheme.EMPTY;
                }
                int[] possibleY = new int[y.size()];
                y.fillArray(possibleY);

                int[] possibleZ = new int[z.size()];
                z.fillArray(possibleZ);

                HashSet<Integer> possibleValues = new HashSet<>();
                HashSet<Integer> possibleValues2 = new HashSet<>();
                for (int i = 0; i < possibleZ.length; i++)
                    possibleValues.add(possibleZ[i]);

                for (int i = 0; i < possibleY.length; i++) {
                    assertTrue(possibleValues.contains(T[possibleY[i]]));
                    possibleValues2.add(T[possibleY[i]]);
                }
                assertEquals(possibleValues.size(), possibleValues2.size());

                if (!y.isFixed() && (z.isFixed() || rand.nextBoolean())) {
                    //select a random y
                    int val = possibleY[rand.nextInt(possibleY.length)];
                    return BranchingScheme.branch(() -> cp.post(CPFactory.equal(y, val)),
                            () -> cp.post(CPFactory.notEqual(y, val)));
                } else {
                    int val = possibleZ[rand.nextInt(possibleZ.length)];
                    return BranchingScheme.branch(() -> cp.post(CPFactory.equal(z, val)),
                            () -> cp.post(CPFactory.notEqual(z, val)));
                }
            };

            DFSearch dfs = CPFactory.makeDfs(cp, branching);

            SearchStatistics stats = dfs.solve();
            assertEquals(stats.numberOfSolutions(), T.length);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
