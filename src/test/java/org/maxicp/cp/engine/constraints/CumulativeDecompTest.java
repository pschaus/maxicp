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

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeClass;
import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.BranchingScheme;
import org.maxicp.cp.CPFactory;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
public class CumulativeDecompTest extends CPSolverTest {


    @Test
    public void testAllDiffWithCumulative() {

        try {

            CPSolver cp = solverFactory.get();

            CPIntVar[] s = CPFactory.makeIntVarArray(cp, 5, 5);
            int[] d = new int[5];
            Arrays.fill(d, 1);
            int[] r = new int[5];
            Arrays.fill(r, 100);

            cp.post(new CumulativeDecomposition(s, d, r, 100));

            SearchStatistics stats = CPFactory.makeDfs(cp, BranchingScheme.firstFail(s)).solve();
            assertEquals("cumulative alldiff expect makeIntVarArray permutations", 120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Test
    public void testBasic1() {

        try {

            CPSolver cp = solverFactory.get();

            CPIntVar[] s = CPFactory.makeIntVarArray(cp, 2, 10);
            int[] d = new int[]{5, 5};
            int[] r = new int[]{1, 1};

            cp.post(new CumulativeDecomposition(s, d, r, 1));
            cp.post(CPFactory.equal(s[0], 0));

            assertEquals(5, s[1].min());

        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testBasic2() {

        try {

            CPSolver cp = solverFactory.get();

            CPIntVar[] s = CPFactory.makeIntVarArray(cp, 2, 10);
            int[] d = new int[]{5, 5};
            int[] r = new int[]{1, 1};

            cp.post(new CumulativeDecomposition(s, d, r, 1));

            cp.post(CPFactory.equal(s[0], 5));

            assertEquals(0, s[1].max());

        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    @Grade(value = 1, cpuTimeout = 12000)
    public void testCapaOk() {

        try {

            CPSolver cp = solverFactory.get();

            CPIntVar[] s = CPFactory.makeIntVarArray(cp, 5, 10);
            int[] d = new int[]{5, 10, 3, 6, 1};
            int[] r = new int[]{3, 7, 1, 4, 8};

            cp.post(new CumulativeDecomposition(s, d, r, 12));

            DFSearch search = CPFactory.makeDfs(cp, BranchingScheme.firstFail(s));

            SearchStatistics stats = search.solve();

            search.onSolution(() -> {
                Profile.Rectangle[] rects = IntStream.range(0, s.length).mapToObj(i -> {
                    int start = s[i].min();
                    int end = start + d[i];
                    int height = r[i];
                    return new Profile.Rectangle(start, end, height);
                }).toArray(Profile.Rectangle[]::new);
                int[] discreteProfile = discreteProfile(rects);
                for (int h : discreteProfile) {
                    assertTrue("capa exceeded in cumulative constraint", h <= 12);
                }
            });

        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    private static int[] discreteProfile(Profile.Rectangle... rectangles) {
        int min = Arrays.stream(rectangles).filter(r -> r.height() > 0).map(r -> r.start()).min(Integer::compare).get();
        int max = Arrays.stream(rectangles).filter(r -> r.height() > 0).map(r -> r.end()).max(Integer::compare).get();
        int[] heights = new int[max - min];
        // discrete profileRectangles of rectangles
        for (Profile.Rectangle r : rectangles) {
            if (r.height() > 0) {
                for (int i = r.start(); i < r.end(); i++) {
                    heights[i - min] += r.height();
                }
            }
        }
        return heights;
    }


}
