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

package org.maxicp.search;

import org.maxicp.BranchingScheme;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.maxicp.util.exception.NotImplementedException;
import org.junit.Test;
import org.maxicp.cp.CPFactory;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class LastConflictSearchTest {


    @Test
    public void testExample1() {
        try {
            CPSolver cp = CPFactory.makeSolver();
            CPIntVar[] x = CPFactory.makeIntVarArray(cp, 8, 8);
            for(int i = 4; i < 8; i++)
                x[i].removeAbove(2);

            // apply alldifferent on the four last variables.
            // of course, this cannot work!
            CPIntVar[] fourLast = Arrays.stream(x).skip(4).toArray(CPIntVar[]::new);
            cp.post(CPFactory.allDifferent(fourLast));

            DFSearch dfs = new DFSearch(cp.getStateManager(), BranchingScheme.lastConflict(
                    () -> { //select first unbound variable in x
                        for(CPIntVar z: x)
                            if(!z.isBound())
                                return z;
                        return null;
                    },
                    CPIntVar::min //select smallest value
            ));

            SearchStatistics stats = dfs.solve();
            assertEquals(stats.numberOfSolutions(), 0);
            assertEquals(stats.numberOfFailures(), 70);
            assertEquals(stats.numberOfNodes(), 138);
        }
        catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
