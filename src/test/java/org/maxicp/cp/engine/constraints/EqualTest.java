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
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;
import org.maxicp.cp.CPFactory;

import static org.junit.Assert.*;


public class EqualTest extends CPSolverTest {

    private static boolean equalDom(CPIntVar x, CPIntVar y) {
        for (int v = x.min(); v < x.max(); v++) {
            if (x.contains(v) && !y.contains(v)) {
                return false;
            }
        }
        for (int v = y.min(); v < y.max(); v++) {
            if (y.contains(v) && !x.contains(v)) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void equal1() {
        try {

            CPSolver cp = solverFactory.get();
            CPIntVar x = CPFactory.makeIntVar(cp,0,10);
            CPIntVar y = CPFactory.makeIntVar(cp,0,10);

            cp.post(CPFactory.equal(x,y));

            x.removeAbove(7);
            cp.fixPoint();

            assertTrue(equalDom(x,y));

            y.removeAbove(6);
            cp.fixPoint();

            x.remove(3);
            cp.fixPoint();

            assertTrue(equalDom(x,y));

            x.fix(1);
            cp.fixPoint();

            assertTrue(equalDom(x,y));


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


    @Test
    public void equal2() {
        try {

            CPSolver cp = solverFactory.get();
            CPIntVar x = CPFactory.makeIntVar(cp,Integer.MAX_VALUE-20,Integer.MAX_VALUE-1);
            CPIntVar y = CPFactory.makeIntVar(cp,Integer.MAX_VALUE-10,Integer.MAX_VALUE-1);

            cp.post(CPFactory.notEqual(x,Integer.MAX_VALUE-5));

            cp.post(CPFactory.equal(x,y));

            cp.post(CPFactory.equal(x,Integer.MAX_VALUE-1));

            assertEquals(y.min(), Integer.MAX_VALUE-1);


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


}
