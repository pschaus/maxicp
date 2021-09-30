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

package org.maxicp.engine.constraints;

import com.github.guillaumederval.javagrading.GradeClass;
import org.maxicp.engine.SolverTest;
import org.maxicp.engine.core.BoolVar;
import org.maxicp.engine.core.IntVar;
import org.maxicp.engine.core.Solver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.cp.BranchingScheme;
import org.maxicp.cp.Factory;

import static org.junit.Assert.*;

@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
public class IsLessOrEqualVarTest extends SolverTest {

    @Test
    public void test1() {
        try {


            Solver cp = solverFactory.get();
            IntVar x = Factory.makeIntVar(cp, 0, 5);
            IntVar y = Factory.makeIntVar(cp, 0, 5);

            BoolVar b = Factory.makeBoolVar(cp);

            cp.post(new IsLessOrEqualVar(b, x, y));

            DFSearch search = Factory.makeDfs(cp, BranchingScheme.firstFail(x, y));

            SearchStatistics stats = search.solve();

            search.onSolution(() ->
                    assertTrue(x.min() <= y.min() && b.isTrue() || x.min() > y.min() && b.isFalse())
            );

            assertEquals(36, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test2() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = Factory.makeIntVar(cp, -8, 7);
            IntVar y = Factory.makeIntVar(cp, -4, 3);

            BoolVar b = Factory.makeBoolVar(cp);

            cp.post(new IsLessOrEqualVar(b, x, y));

            cp.getStateManager().saveState();
            cp.post(Factory.equal(b, 1));
            assertEquals(3, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(Factory.equal(b, 0));
            assertEquals(-3, x.min());
            cp.getStateManager().restoreState();

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test3() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = Factory.makeIntVar(cp, -4, 7);
            IntVar y = Factory.makeIntVar(cp, 0, 7);
            cp.post(Factory.equal(x, -2));

            BoolVar b = Factory.makeBoolVar(cp);
            cp.post(new IsLessOrEqualVar(b, x, y));
            assertTrue(b.isTrue());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test4() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = Factory.makeIntVar(cp, -4, 7);
            BoolVar b = Factory.makeBoolVar(cp);

            cp.getStateManager().saveState();
            cp.post(Factory.equal(b, 1));
            cp.post(new IsLessOrEqual(b, x, -2));
            assertEquals(-2, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(Factory.equal(b, 0));
            cp.post(new IsLessOrEqual(b, x, -2));
            assertEquals(-1, x.min());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
