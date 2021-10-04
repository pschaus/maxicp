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
import org.maxicp.cp.engine.core.CPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Assume;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.cp.CPFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static org.junit.Assert.*;

public class TableTest extends CPSolverTest {

    private static List<BiFunction<CPIntVar[], int[][], CPConstraint>> getAlgos() {
        List<BiFunction<CPIntVar[], int[][], CPConstraint>> algos = new ArrayList<>();
        algos.add(TableDecomp::new);
        algos.add(TableCT::new);
        return algos;
    }

    private int[][] randomTuples(Random rand, int arity, int nTuples, int minvalue, int maxvalue) {
        int[][] r = new int[nTuples][arity];
        for (int i = 0; i < nTuples; i++)
            for (int j = 0; j < arity; j++)
                r[i][j] = rand.nextInt(maxvalue - minvalue) + minvalue;
        return r;
    }

    @Test
    public void simpleTest0() {

        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = CPFactory.makeIntVarArray(cp, 2, 1);
            int[][] table = new int[][]{{0, 0}};
            cp.post(new TableCT(x, table));

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void simpleTest1() {
        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = CPFactory.makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{{0, 0, 2},
                    {3, 5, 7},
                    {6, 9, 10},
                    {1, 2, 3}};
            cp.post(new TableCT(x, table));

            assertEquals(4, x[0].size());
            assertEquals(4, x[1].size());
            assertEquals(4, x[2].size());

            assertEquals(0, x[0].min());
            assertEquals(6, x[0].max());
            assertEquals(0, x[1].min());
            assertEquals(9, x[1].max());
            assertEquals(2, x[2].min());
            assertEquals(10, x[2].max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void randomTest() {
        Random rand = new Random(67292);

        for (int i = 0; i < 100; i++) {
            int[][] tuples1 = randomTuples(rand, 3, 50, 2, 8);
            int[][] tuples2 = randomTuples(rand, 3, 50, 1, 7);
            int[][] tuples3 = randomTuples(rand, 3, 50, 0, 6);

            for (BiFunction<CPIntVar[], int[][], CPConstraint> algo : getAlgos()) {
                try {
                    testTable(algo, tuples1, tuples2, tuples3);
                } catch (NotImplementedException e) {
                    Assume.assumeNoException(e);
                }
            }
        }
    }


    public void testTable(BiFunction<CPIntVar[], int[][], CPConstraint> tc, int[][] t1, int[][] t2, int[][] t3) {

        SearchStatistics statsDecomp;
        SearchStatistics statsAlgo;

        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = CPFactory.makeIntVarArray(cp, 5, 9);
            cp.post(CPFactory.allDifferent(x));
            cp.post(new TableDecomp(new CPIntVar[]{x[0], x[1], x[2]}, t1));
            cp.post(new TableDecomp(new CPIntVar[]{x[2], x[3], x[4]}, t2));
            cp.post(new TableDecomp(new CPIntVar[]{x[0], x[2], x[4]}, t3));
            statsDecomp = CPFactory.makeDfs(cp, BranchingScheme.firstFail(x)).solve();
        } catch (InconsistencyException e) {
            statsDecomp = null;
        }

        try {
            CPSolver cp = solverFactory.get();
            CPIntVar[] x = CPFactory.makeIntVarArray(cp, 5, 9);
            cp.post(CPFactory.allDifferent(x));
            cp.post(tc.apply(new CPIntVar[]{x[0], x[1], x[2]}, t1));
            cp.post(tc.apply(new CPIntVar[]{x[2], x[3], x[4]}, t2));
            cp.post(tc.apply(new CPIntVar[]{x[0], x[2], x[4]}, t3));
            statsAlgo = CPFactory.makeDfs(cp, BranchingScheme.firstFail(x)).solve();
        } catch (InconsistencyException e) {
            statsAlgo = null;
        }

        assertTrue((statsDecomp == null && statsAlgo == null) || (statsDecomp != null && statsAlgo != null));
        if (statsDecomp != null) {
            assertEquals(statsDecomp.numberOfSolutions(), statsAlgo.numberOfSolutions());
            assertEquals(statsDecomp.numberOfFailures(), statsAlgo.numberOfFailures());
            assertEquals(statsDecomp.numberOfNodes(), statsAlgo.numberOfNodes());
        }
    }
}
