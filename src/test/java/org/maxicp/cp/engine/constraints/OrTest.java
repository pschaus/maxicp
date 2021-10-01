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

import com.github.guillaumederval.javagrading.GradeClass;
import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.Factory;

import static org.junit.Assert.*;

@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
public class OrTest extends CPSolverTest {

    @Test
    public void or1() {
        try {

            CPSolver cp = solverFactory.get();
            CPBoolVar[] x = new CPBoolVar[]{Factory.makeBoolVar(cp), Factory.makeBoolVar(cp), Factory.makeBoolVar(cp), Factory.makeBoolVar(cp)};
            cp.post(new Or(x));

            for (CPBoolVar xi : x) {
                assertTrue(!xi.isBound());
            }

            cp.post(Factory.equal(x[1], 0));
            cp.post(Factory.equal(x[2], 0));
            cp.post(Factory.equal(x[3], 0));
            assertTrue(x[0].isTrue());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Test
    public void or2() {
        try {

            CPSolver cp = solverFactory.get();
            CPBoolVar[] x = new CPBoolVar[]{Factory.makeBoolVar(cp), Factory.makeBoolVar(cp), Factory.makeBoolVar(cp), Factory.makeBoolVar(cp)};
            cp.post(new Or(x));


            DFSearch dfs = Factory.makeDfs(cp, BranchingScheme.firstFail(x));

            dfs.onSolution(() -> {
                        int nTrue = 0;
                        for (CPBoolVar xi : x) {
                            if (xi.isTrue()) nTrue++;
                        }
                        assertTrue(nTrue > 0);

                    }
            );

            SearchStatistics stats = dfs.solve();

            assertEquals(15, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Test
    public void or3() {
        try {
            CPSolver cp = solverFactory.get();
            CPBoolVar[] x = new CPBoolVar[]{Factory.makeBoolVar(cp), Factory.makeBoolVar(cp), Factory.makeBoolVar(cp), Factory.makeBoolVar(cp)};
            
            for (CPBoolVar xi : x) {
                xi.assign(false);
            }
            
            cp.post(new Or(x));
            fail("should fail");
            
        } catch (InconsistencyException e) {
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
