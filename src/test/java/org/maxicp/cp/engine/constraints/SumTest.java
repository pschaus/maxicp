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

import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.IntOverFlowException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.BranchingScheme;

import java.util.Arrays;
import java.util.HashSet;

import static org.maxicp.cp.CPFactory.lessOrEqual;
import static org.maxicp.cp.CPFactory.makeIntVar;
import static org.junit.Assert.*;


public class SumTest extends CPSolverTest {


    private static CPIntVar makeIVar(CPSolver cp, Integer... values) {
        return CPFactory.makeIntVar(cp, new HashSet<>(Arrays.asList(values)));
    }

    @Test
    public void sum1() {
        try {
            CPSolver cp = solverFactory.get();
            CPIntVar y = CPFactory.makeIntVar(cp, -100, 100);
            CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, 0, 5), CPFactory.makeIntVar(cp, 1, 5), CPFactory.makeIntVar(cp, 0, 5)};
            cp.post(new Sum(x, y));

            assertEquals(1, y.min());
            assertEquals(15, y.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Test
    public void sum2() {
        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, -5, 5), CPFactory.makeIntVar(cp, 1, 2), CPFactory.makeIntVar(cp, 0, 1)};
            CPIntVar y = CPFactory.makeIntVar(cp, 0, 100);
            cp.post(new Sum(x, y));

            assertEquals(-3, x[0].min());
            assertEquals(0, y.min());
            assertEquals(8, y.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void sum3() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, -5, 5), CPFactory.makeIntVar(cp, 1, 2), CPFactory.makeIntVar(cp, 0, 1)};
            CPIntVar y = CPFactory.makeIntVar(cp, 5, 5);
            cp.post(new Sum(x, y));

            x[0].removeBelow(1);
            // 1-5 + 1-2 + 0-1 = 5
            x[1].assign(1);
            // 1-5 + 1 + 0-1 = 5
            cp.fixPoint();

            assertEquals(4, x[0].max());
            assertEquals(3, x[0].min());
            assertEquals(1, x[2].max());
            assertEquals(0, x[2].min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void sum4() {

        try {

            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, 0, 5), CPFactory.makeIntVar(cp, 0, 2), CPFactory.makeIntVar(cp, 0, 1)};
            cp.post(new Sum(x, 0));

            assertEquals(0, x[0].max());
            assertEquals(0, x[1].max());
            assertEquals(0, x[2].max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void sum5() {
        try {

            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, -5, 0), CPFactory.makeIntVar(cp, -5, 0), CPFactory.makeIntVar(cp, -3, 0)};
            cp.post(new Sum(x, 0));

            assertEquals(0, x[0].min());
            assertEquals(0, x[1].min());
            assertEquals(0, x[2].min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void sum6() {
        try {

            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, -5, 0), CPFactory.makeIntVar(cp, -5, 0), CPFactory.makeIntVar(cp, -3, 3)};
            cp.post(new Sum(x, 0));
            assertEquals(-3, x[0].min());
            assertEquals(-3, x[1].min());

            x[2].removeAbove(0);
            cp.fixPoint();

            assertEquals(0, x[0].min());
            assertEquals(0, x[1].min());
            assertEquals(0, x[2].min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void sum7() {
        try {

            CPSolver cp = solverFactory.get();
            CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, -5, 0), CPFactory.makeIntVar(cp, -5, 0), CPFactory.makeIntVar(cp, -3, 3)};
            cp.post(new Sum(x, 0));
            assertEquals(-3, x[0].min());
            assertEquals(-3, x[1].min());

            x[2].remove(1);
            x[2].remove(2);
            x[2].remove(3);
            x[2].remove(4);
            x[2].remove(5);
            cp.fixPoint();

            assertEquals(0, x[0].min());
            assertEquals(0, x[1].min());
            assertEquals(0, x[2].min());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void sum8() {
        try {

            CPSolver cp = solverFactory.get();

            // {0,0,0},  1
            // {-2,1,1}  3
            // {2,-1,-1} 3
            // {-1,1,0}  6
            // {0,-3,3}  6
            // {2,-2,0}  6
            // {-1,1,0}  6
            // {1,2,-3}  6


            CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, -3, 3), CPFactory.makeIntVar(cp, -3, 3), CPFactory.makeIntVar(cp, -3, 3)};
            cp.post(new Sum(x, 0));

            DFSearch search = CPFactory.makeDfs(cp, BranchingScheme.firstFail(x));

            SearchStatistics stats = search.solve();

            assertEquals(37, stats.numberOfSolutions());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void sum9() {
        CPSolver cp = solverFactory.get();

        CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, -9, -9)};
        boolean failed = false;
        try {
            cp.post(new Sum(x));
        } catch (InconsistencyException e) {
            failed = true;
        }
        assertTrue(failed);
    }


    @Test
    public void sum10() {
        CPSolver cp = solverFactory.get();

        CPIntVar[] x = new CPIntVar[]{CPFactory.makeIntVar(cp, -9, -4)};
        boolean failed = false;
        try {
            cp.post(new Sum(x));
        } catch (InconsistencyException e) {
            failed = true;
        }
        assertTrue(failed);
    }


    @Test
    public void sum11() {

        try {
            CPSolver cp = solverFactory.get();

            CPIntVar x = makeIVar(cp, -2147483645, -2147483639, -2147483637);
            CPIntVar y = makeIVar(cp, -2147483645, -2147483638);

            boolean failed = false;
            try {
                cp.post(CPFactory.sum(new CPIntVar[]{x}, y));
            } catch (InconsistencyException e) {
                failed = true;
            }
            assertFalse(failed);

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void sum12() {

        try {

            CPSolver cp = solverFactory.get();

            CPIntVar x = makeIVar(cp, -45, -39, -37);
            CPIntVar y = makeIVar(cp, -45, -3);

            boolean failed = false;
            try {
                cp.post(CPFactory.sum(new CPIntVar[]{x}, y));
            } catch (InconsistencyException e) {
                failed = true;
            }
            assertFalse(failed);

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test(expected = IntOverFlowException.class)
    public void sum13OverFlow() {

        try {

            CPSolver cp = solverFactory.get();

            CPIntVar x0 = makeIVar(cp, -463872433, -463872431, -463872430, -463872429);
            CPIntVar x1 = makeIVar(cp, -463872438, -463872437, -463872430);
            CPIntVar x2 = makeIVar(cp, -463872432, -463872429);
            CPIntVar x3 = makeIVar(cp, -463872435, -463872434, -463872432, -463872431, -463872430, -463872429);
            CPIntVar x4 = makeIVar(cp, -463872437, -463872436, -463872435, -463872432, -463872431, -463872430, -463872429);


            cp.post(CPFactory.lessOrEqual(CPFactory.sum(x0, x1, x2, x3, x4), 0));

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


}
