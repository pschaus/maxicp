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

import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.IntOverFlowException;
import org.junit.Test;
import org.maxicp.cp.CPFactory;

import static org.maxicp.cp.CPFactory.makeIntVar;
import static org.junit.Assert.*;


public class CPIntVarViewOffsetTest extends CPSolverTest {

    public boolean propagateCalled = false;

    @Test
    public void testIntVar() {
        CPSolver cp = solverFactory.get();

        CPIntVar x = CPFactory.plus(CPFactory.makeIntVar(cp, -3, 4), 3); // domain is {0,1,2,3,4,5,6,7}

        assertEquals(0, x.min());
        assertEquals(7, x.max());
        assertEquals(8, x.size());

        cp.getStateManager().saveState();


        try {

            assertFalse(x.isFixed());

            x.remove(0);
            assertFalse(x.contains(0));
            x.remove(3);
            assertTrue(x.contains(1));
            assertTrue(x.contains(2));
            assertEquals(6, x.size());
            x.removeAbove(6);
            assertEquals(6, x.max());
            x.removeBelow(3);
            assertEquals(4, x.min());
            x.fix(5);
            assertTrue(x.isFixed());
            assertEquals(5, x.max());


        } catch (InconsistencyException e) {
            e.printStackTrace();
            fail("should not fail here");
        }

        try {
            x.fix(4);
            fail("should have failed");
        } catch (InconsistencyException expectedException) {
        }

        cp.getStateManager().restoreState();

        assertEquals(8, x.size());
        assertFalse(x.contains(-1));

    }


    @Test
    public void onDomainChangeOnBind() {
        propagateCalled = false;
        CPSolver cp = solverFactory.get();

        CPIntVar x = CPFactory.plus(CPFactory.makeIntVar(cp, 10), 1); // 1..11
        CPIntVar y = CPFactory.plus(CPFactory.makeIntVar(cp, 10), 1); // 1..11

        CPConstraint cons = new AbstractCPConstraint(cp) {

            @Override
            public void post() {
                x.whenFixed(() -> propagateCalled = true);
                y.whenDomainChange(() -> propagateCalled = true);
            }
        };

        try {
            cp.post(cons);
            x.remove(9);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.fix(5);
            cp.fixPoint();
            assertTrue(propagateCalled);
            propagateCalled = false;
            y.remove(11);
            cp.fixPoint();
            assertFalse(propagateCalled);
            y.remove(10);
            cp.fixPoint();
            assertTrue(propagateCalled);

        } catch (InconsistencyException inconsistency) {
            fail("should not fail");
        }
    }


    @Test
    public void onBoundChange() {

        CPSolver cp = solverFactory.get();

        CPIntVar x = CPFactory.plus(CPFactory.makeIntVar(cp, 10), 1);
        CPIntVar y = CPFactory.plus(CPFactory.makeIntVar(cp, 10), 1);

        CPConstraint cons = new AbstractCPConstraint(cp) {

            @Override
            public void post() {
                x.whenFixed(() -> propagateCalled = true);
                y.whenDomainChange(() -> propagateCalled = true);
            }
        };

        try {
            cp.post(cons);
            x.remove(9);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.remove(10);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.fix(5);
            cp.fixPoint();
            assertTrue(propagateCalled);
            propagateCalled = false;
            assertFalse(y.contains(11));
            y.remove(11);
            cp.fixPoint();
            assertFalse(propagateCalled);
            propagateCalled = false;
            y.remove(3);
            cp.fixPoint();
            assertTrue(propagateCalled);

        } catch (InconsistencyException inconsistency) {
            fail("should not fail");
        }
    }


    @Test(expected = IntOverFlowException.class)
    public void testOverFlow() {
        CPSolver cp = solverFactory.get();
        CPIntVar x = CPFactory.plus(CPFactory.makeIntVar(cp, Integer.MAX_VALUE - 5, Integer.MAX_VALUE - 2), 3);
    }


}
