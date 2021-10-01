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
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Assume;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.Factory;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
public class DisjunctiveTest extends CPSolverTest {


    private static void decomposeDisjunctive(CPIntVar[] start, int[] duration) {
        CPSolver cp = start[0].getSolver();
        for (int i = 0; i < start.length; i++) {
            CPIntVar end_i = Factory.plus(start[i], duration[i]);
            for (int j = i + 1; j < start.length; j++) {
                // i before j or j before i

                CPIntVar end_j = Factory.plus(start[j], duration[j]);
                CPBoolVar iBeforej = Factory.makeBoolVar(cp);
                CPBoolVar jBeforei = Factory.makeBoolVar(cp);

                cp.post(new IsLessOrEqualVar(iBeforej, end_i, start[j]));
                cp.post(new IsLessOrEqualVar(jBeforei, end_j, start[i]));
                cp.post(new NotEqual(iBeforej, jBeforei), false);

            }
        }

    }

    @Test
    public void testAllDiffDisjunctive() {

        try {

            CPSolver cp = solverFactory.get();

            CPIntVar[] s = Factory.makeIntVarArray(cp, 5, 5);
            int[] d = new int[5];
            Arrays.fill(d, 1);

            cp.post(new Disjunctive(s, d));

            SearchStatistics stats = Factory.makeDfs(cp, BranchingScheme.firstFail(s)).solve();
            assertEquals("disjunctive alldiff expect makeIntVarArray permutations", 120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Test
    public void testNotRemovingSolutions() {

        try {

            CPSolver cp = solverFactory.get();

            CPIntVar[] s = Factory.makeIntVarArray(cp, 4, 20);
            int[] d = new int[]{5, 4, 6, 7};
            DFSearch dfs = Factory.makeDfs(cp, BranchingScheme.firstFail(s));


            cp.getStateManager().saveState();

            cp.post(new Disjunctive(s, d));

            SearchStatistics stat1 = dfs.solve();

            cp.getStateManager().restoreState();

            decomposeDisjunctive(s, d);

            SearchStatistics stat2 = dfs.solve();


            assertEquals(stat1.numberOfSolutions(), stat2.numberOfSolutions());


        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void testBinaryDecomposition() {
        CPSolver cp = solverFactory.get();
        CPIntVar s1 = Factory.makeIntVar(cp, 0, 10);
        int d1 = 10;
        CPIntVar s2 = Factory.makeIntVar(cp, 6, 15);
        int d2 = 6;

        try {
            cp.post(new Disjunctive(new CPIntVar[]{s1, s2}, new int[]{d1, d2}));
            assertEquals(10, s2.min());
        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testOverloadChecker() {
        CPSolver cp = solverFactory.get();
        CPIntVar sA = Factory.makeIntVar(cp, 0, 9);
        int d1 = 5;
        CPIntVar sB = Factory.makeIntVar(cp, 1, 10);
        int d2 = 5;
        CPIntVar sC = Factory.makeIntVar(cp, 3, 7);
        int d3 = 6;

        try {
            cp.post(new Disjunctive(new CPIntVar[]{sA, sB, sC}, new int[]{d1, d2, d3}));
            assert (false);
            Assume.assumeTrue(false);
        } catch (InconsistencyException e) {
            assert (true);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testDetectablePrecedence() {
        CPSolver cp = solverFactory.get();
        CPIntVar sA = Factory.makeIntVar(cp, 0, 9);
        int d1 = 5;
        CPIntVar sB = Factory.makeIntVar(cp, 1, 10);
        int d2 = 5;
        CPIntVar sC = Factory.makeIntVar(cp, 8, 15);
        int d3 = 3;

        try {
            cp.post(new Disjunctive(new CPIntVar[]{sA, sB, sC}, new int[]{d1, d2, d3}));
            Assume.assumeTrue("not last should set est(C)=10", sC.min() == 10);
        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void testNotLast() {
        CPSolver cp = solverFactory.get();
        CPIntVar sA = Factory.makeIntVar(cp, 0, 9);
        int d1 = 5;
        CPIntVar sB = Factory.makeIntVar(cp, 1, 10);
        int d2 = 5;
        CPIntVar sC = Factory.makeIntVar(cp, 3, 9);
        int d3 = 4;

        try {
            cp.post(new Disjunctive(new CPIntVar[]{sA, sB, sC}, new int[]{d1, d2, d3}));
            Assume.assumeTrue("not last should set lst(C)=6", sC.max() == 6);
        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
