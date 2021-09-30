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

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeClass;
import org.maxicp.state.StateInt;
import org.maxicp.state.StateManager;
import org.maxicp.state.StateManagerTest;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.junit.Test;
import org.maxicp.BranchingScheme;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


@GradeClass(totalValue=1, allCorrect=true)
public class DFSearchTest extends StateManagerTest {
    @Test
    public void testExample1() {
        StateManager sm = stateFactory.get();
        StateInt i = sm.makeStateInt(0);
        int[] values = new int[3];

        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length)
                return BranchingScheme.EMPTY;
            else return BranchingScheme.branch(
                    () -> { // left branch
                        values[i.value()] = 0;
                        i.increment();
                    },
                    () -> { // right branch
                        values[i.value()] = 1;
                        i.increment();
                    }
            );
        });


        SearchStatistics stats = dfs.solve();

        assertEquals(8,stats.numberOfSolutions());
        assertEquals (0,stats.numberOfFailures());
        assertEquals (8 + 4 + 2,stats.numberOfNodes());
    }


    @Test
    public void testExample3() {
        StateManager sm = stateFactory.get();
        StateInt i = sm.makeStateInt(0);
        int[] values = new int[3];

        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length)
                return BranchingScheme.EMPTY;
            else return BranchingScheme.branch(
                    () -> { // left branch
                        values[i.value()] = 1;
                        i.increment();
                    },
                    () -> { // right branch
                        values[i.value()] = 0;
                        i.increment();
                    }
            );
        });


        dfs.onSolution(() -> {
            assert (Arrays.stream(values).allMatch(v -> v == 1));
        });


        SearchStatistics stats = dfs.solve(stat -> stat.numberOfSolutions() >= 1);

        assertEquals (1,stats.numberOfSolutions());
    }


    @Test
    public void testDFS() {
        StateManager sm = stateFactory.get();
        StateInt i = sm.makeStateInt(0);
        boolean[] values = new boolean[4];

        AtomicInteger nSols = new AtomicInteger(0);


        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length)
                return BranchingScheme.EMPTY;
            else return BranchingScheme.branch(
                    () -> {
                        // left branch
                        values[i.value()] = false;
                        i.increment();
                    },
                    () -> {
                        // right branch
                        values[i.value()] = true;
                        i.increment();
                    }
            );
        });

        dfs.onSolution(() -> {
            nSols.incrementAndGet();
        });


        SearchStatistics stats = dfs.solve();


        assertEquals(16, nSols.get());
        assertEquals(16, stats.numberOfSolutions());
        assertEquals(0, stats.numberOfFailures());
        assertEquals((16 + 8 + 4 + 2), stats.numberOfNodes());

    }

    @Test
    public void testDFSSearchLimit() {
        StateManager sm = stateFactory.get();

        StateInt i = sm.makeStateInt(0);
        boolean[] values = new boolean[4];

        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length) {
                return BranchingScheme.branch(() -> {
                    throw new InconsistencyException();
                });
            } else return BranchingScheme.branch(
                    () -> {
                        // left branch
                        values[i.value()] = false;
                        i.increment();
                    },
                    () -> {
                        // right branch
                        values[i.value()] = true;
                        i.increment();
                    }
            );
        });




        // stop search after 2 solutions
        SearchStatistics stats = dfs.solve(stat -> stat.numberOfFailures() >= 3);

        assertEquals (0,stats.numberOfSolutions());
        assertEquals (3,stats.numberOfFailures());

    }


    @Test
    @Grade(value = 0.5, cpuTimeout = 2000)
    public void testDeepDFS() {
        testExample1();
        testDFS();
        testDFSSearchLimit();

        StateManager sm = stateFactory.get();
        StateInt i = sm.makeStateInt(0);
        boolean[] values = new boolean[10000];

        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length) {
                return BranchingScheme.EMPTY;
            } else return BranchingScheme.branch(
                    () -> {
                        // left branch
                        values[i.value()] = false;
                        i.increment();
                    },
                    () -> {
                        // right branch
                        values[i.value()] = true;
                        i.increment();
                    }
            );
        });
        try {
            // stop search after 1 solutions (only left most branch)
            SearchStatistics stats = dfs.solve(stat -> stat.numberOfSolutions() >= 1);
            assertEquals (1,stats.numberOfSolutions());
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    @Grade(value = 0.5, cpuTimeout = 2000)
    public void checkInconsistenciesManagedCorrectly() {
        StateManager sm = stateFactory.get();
        int[] values = new int[3]; //init to 0

        DFSearch dfs = new DFSearch(sm, () -> {
            if(values[0] >= 100)
                return BranchingScheme.EMPTY;

            return BranchingScheme.branch(
                    () -> {
                        values[0] += 1;
                        if(values[0] == 1)
                            throw new InconsistencyException();
                        //this should never happen in a left branch!
                        assertNotEquals(2, values[0]);
                    },
                    () -> {
                        values[0] += 1;
                    });
        });

        dfs.solve();
    }
}
