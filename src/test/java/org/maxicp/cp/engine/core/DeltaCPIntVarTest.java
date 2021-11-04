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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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
                super.propagate();
            }
        });

        cp.post(CPFactory.lessOrEqual(x,4));
        assertTrue(propag[0]);

    }

}
