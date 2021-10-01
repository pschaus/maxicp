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
import org.maxicp.cp.engine.core.IntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.Factory;

import static org.maxicp.Factory.makeIntVar;
import static org.junit.Assert.*;

@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
public class Element1DTest extends CPSolverTest {

    @Test
    public void element1dTest1() {

        try {

            CPSolver cp = solverFactory.get();
            IntVar y = Factory.makeIntVar(cp, -3, 10);
            IntVar z = Factory.makeIntVar(cp, 2, 40);

            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z));

            assertEquals(0, y.min());
            assertEquals(4, y.max());


            assertEquals(5, z.min());
            assertEquals(9, z.max());

            z.removeAbove(7);
            cp.fixPoint();

            assertEquals(2, y.min());


            y.remove(3);
            cp.fixPoint();

            assertEquals(7, z.max());
            assertEquals(6, z.min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void element1dTest2() {

        try {

            CPSolver cp = solverFactory.get();
            IntVar y = Factory.makeIntVar(cp, -3, 10);
            IntVar z = Factory.makeIntVar(cp, -20, 40);

            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z));

            DFSearch dfs = Factory.makeDfs(cp, BranchingScheme.firstFail(y, z));
            dfs.onSolution(() ->
                    assertEquals(T[y.min()], z.min())
            );
            SearchStatistics stats = dfs.solve();

            assertEquals(5, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void element1dTest3() {
        try {

            CPSolver cp = solverFactory.get();
            IntVar y = Factory.makeIntVar(cp, 0, 4);
            IntVar z = Factory.makeIntVar(cp, 5, 9);


            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z));

            y.remove(3); //T[4]=5
            y.remove(0); //T[0]=9

            cp.fixPoint();

            assertEquals(6, z.min());
            assertEquals(8, z.max());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void element1dTest4() {

        try {

            CPSolver cp = solverFactory.get();
            IntVar y = Factory.makeIntVar(cp, 0, 4);
            IntVar z = Factory.makeIntVar(cp, 5, 9);


            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z));

            z.remove(9); //new max is 8
            z.remove(5); //new min is 6
            cp.fixPoint();

            assertFalse(y.contains(0));
            assertFalse(y.contains(3));
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
