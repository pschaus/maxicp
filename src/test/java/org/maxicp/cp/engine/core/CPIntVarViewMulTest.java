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


public class CPIntVarViewMulTest extends CPSolverTest {

    public boolean propagateCalled = false;

    @Test
    public void testIntVar() {
        CPSolver cp = solverFactory.get();

        CPIntVar x = CPFactory.mul(CPFactory.mul(CPFactory.makeIntVar(cp, -3, 4), -3), -1); // domain is {-9,-6,-3,0,3,6,9,12}

        assertEquals(-9, x.min());
        assertEquals(12, x.max());
        assertEquals(8, x.size());

        cp.getStateManager().saveState();


        try {

            assertFalse(x.isBound());

            x.remove(-6);
            assertFalse(x.contains(-6));
            x.remove(2);
            assertTrue(x.contains(0));
            assertTrue(x.contains(3));
            assertEquals(7, x.size());
            x.removeAbove(7);
            assertEquals(6, x.max());
            x.removeBelow(-8);
            assertEquals(-3, x.min());
            x.assign(3);
            assertTrue(x.isBound());
            assertEquals(3, x.max());


        } catch (InconsistencyException e) {
            e.printStackTrace();
            fail("should not fail here");
        }

        try {
            x.assign(8);
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

        CPIntVar x = CPFactory.mul(CPFactory.makeIntVar(cp, 10), 1);
        CPIntVar y = CPFactory.mul(CPFactory.makeIntVar(cp, 10), 1);

        CPConstraint cons = new AbstractCPConstraint(cp) {

            @Override
            public void post() {
                x.whenBind(() -> propagateCalled = true);
                y.whenDomainChange(() -> propagateCalled = true);
            }
        };

        try {
            cp.post(cons);
            x.remove(8);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.assign(4);
            cp.fixPoint();
            assertTrue(propagateCalled);
            propagateCalled = false;
            y.remove(10);
            cp.fixPoint();
            assertFalse(propagateCalled);
            y.remove(9);
            cp.fixPoint();
            assertTrue(propagateCalled);

        } catch (InconsistencyException inconsistency) {
            fail("should not fail");
        }
    }


    @Test
    public void onBoundChange() {

        CPSolver cp = solverFactory.get();

        CPIntVar x = CPFactory.mul(CPFactory.makeIntVar(cp, 10), 1);
        CPIntVar y = CPFactory.mul(CPFactory.makeIntVar(cp, 10), 1);

        CPConstraint cons = new AbstractCPConstraint(cp) {

            @Override
            public void post() {
                x.whenBind(() -> propagateCalled = true);
                y.whenDomainChange(() -> propagateCalled = true);
            }
        };

        try {
            cp.post(cons);
            x.remove(8);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.remove(9);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.assign(4);
            cp.fixPoint();
            assertTrue(propagateCalled);
            propagateCalled = false;
            assertFalse(y.contains(10));
            y.remove(10);
            cp.fixPoint();
            assertFalse(propagateCalled);
            propagateCalled = false;
            y.remove(2);
            cp.fixPoint();
            assertTrue(propagateCalled);

        } catch (InconsistencyException inconsistency) {
            fail("should not fail");
        }
    }


    @Test(expected = IntOverFlowException.class)
    public void testOverFlow() {
        CPSolver cp = solverFactory.get();
        CPIntVar x = CPFactory.mul(CPFactory.makeIntVar(cp, 1000000, 1000000), 10000000);
    }


}
