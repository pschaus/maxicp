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

package org.maxicp.cp.engine.core;

import org.junit.Assert;
import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.CPSolverTest;
import org.junit.Test;
import org.maxicp.cp.engine.constraints.LessOrEqual;
import org.maxicp.model.IntVar;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.state.State;
import org.maxicp.state.copy.Copy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.maxicp.BranchingScheme.*;
import static org.maxicp.cp.CPFactory.makeDfs;


public class DeltaCPIntVarTest extends CPSolverTest {

    public Set<Integer> setOf(int [] values) {
        Set<Integer> s = new HashSet<>();
        for (int i: values) {
            s.add(i);
        }
        return s;
    }

    @Test
    public void test1() {
        CPSolver cp = solverFactory.get();
        CPIntVar x = CPFactory.makeIntVar(cp,Set.of(1,3,5,7));
        boolean [] propag = new boolean[]{false};
        int [] removed = new int[x.size()];
        cp.post(new AbstractCPConstraint(cp) {

            DeltaCPIntVar delta = x.delta(this);

            @Override
            public void post() {
                super.post();
                x.propagateOnBoundChange(this);
            }

            @Override
            public void propagate() {
                propag[0] = true;
                Assert.assertTrue(delta.changed());
                Assert.assertEquals(2,delta.size());
                Assert.assertEquals(true,delta.maxChanged());
                Assert.assertEquals(false,delta.minChanged());
                Assert.assertEquals(1,delta.oldMin());
                Assert.assertEquals(7,delta.oldMax());
                int s = delta.fillArray(removed);
                Assert.assertEquals(2,s);
                Assert.assertEquals(Set.of(5,7),setOf(Arrays.copyOfRange(removed,0,s)));
            }
        });

        cp.post(CPFactory.lessOrEqual(x,4));
        assertTrue(propag[0]);

    }


    private static Set<Integer> domain(CPIntVar x) {
        Set<Integer> values = new HashSet<>();
        for (int i = x.min(); i <= x.max(); i++) {
            if (x.contains(i)) {
                values.add(i);
            }
        }
        return values;
    }

    @Test
    public void test2() {
        CPSolver cp = solverFactory.get();
        CPIntVar x = CPFactory.makeIntVar(cp,Set.of(1,3,5,7,10,11,12,13,14,15,16,20,24,26,27));
        State<Set<Integer>> previousDom = cp.getStateManager().makeStateRef(domain(x));
        Random rand = new Random();
        int max = x.max();

        int [] removed = new int[x.size()];

        cp.post(new AbstractCPConstraint(cp) {

            DeltaCPIntVar delta = x.delta(this);
            @Override
            public void post() {
                super.post();
                x.propagateOnDomainChange(this);
            }

            @Override
            public void propagate() {
                Set<Integer> deltaSetExpected = new HashSet<>(previousDom.value());
                deltaSetExpected.removeAll(domain(x));
                int s = delta.fillArray(removed);
                Set<Integer> deltaComputed = setOf(Arrays.copyOfRange(removed,0,s));
                Assert.assertEquals(deltaSetExpected,deltaComputed);
                previousDom.setValue(domain(x));
            }
        });

        cp.post(new AbstractCPConstraint(cp) {

            DeltaCPIntVar delta = x.delta(this);

            @Override
            public void post() {
                super.post();
                x.propagateOnDomainChange(this);
            }

            @Override
            public void propagate() {
                int v = rand.nextInt(max-1);
                x.remove(rand.nextInt(max-1));
            }
        });

        DFSearch dfs = makeDfs(cp, () -> {
            if (x.isBound()) return EMPTY;
            else {
                final int v = x.min()+(x.max()-x.min())/2;
                return branch(() -> cp.post(CPFactory.lessOrEqual(x, v)),
                        () -> cp.post(CPFactory.largerOrEqual(x, v+1)));
            }
        });
        SearchStatistics stats = dfs.solve();


    }

}
