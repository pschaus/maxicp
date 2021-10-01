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

package org.maxicp.cp.examples;

import org.maxicp.Factory;
import org.maxicp.cp.engine.core.IntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.BranchingScheme;

import java.util.Arrays;

import static org.maxicp.cp.examples.SMoney.Letters.*;

/**
 * The Send-More-Money problem.
 *    S E N D
 * +  M O R E
 * ----------
 *  M O N E Y
 * All digits values are different.
 * Leading digits can't be zero
 */
public class SMoney {

    enum Letters {
        S(0), E(1), N(2), D(3), M(4), O(5), R(6), Y(7);
        public final int val;

        Letters(int v) {
            val = v;
        }
    };

    public static void main(String[] args) {
        CPSolver cp = Factory.makeSolver(false);
        IntVar[] values = Factory.makeIntVarArray(cp, Y.val + 1, 0, 9);
        IntVar[] carry = Factory.makeIntVarArray(cp, 4, 0, 1);

        cp.post(Factory.allDifferent(values));
        cp.post(Factory.notEqual(values[S.val], 0));
        cp.post(Factory.notEqual(values[M.val], 0));
        cp.post(Factory.equal(values[M.val], carry[3]));
        cp.post(Factory.equal(Factory.sum(carry[2], values[S.val], values[M.val], Factory.minus(values[O.val]), Factory.mul(carry[3], -10)), 0));
        cp.post(Factory.equal(Factory.sum(carry[1], values[E.val], values[O.val], Factory.minus(values[N.val]), Factory.mul(carry[2], -10)), 0));
        cp.post(Factory.equal(Factory.sum(carry[0], values[N.val], values[R.val], Factory.minus(values[E.val]), Factory.mul(carry[1], -10)), 0));
        cp.post(Factory.equal(Factory.sum(values[D.val], values[E.val], Factory.minus(values[Y.val]), Factory.mul(carry[0], -10)), 0));


        DFSearch search = Factory.makeDfs(cp, BranchingScheme.firstFail(values));

        search.onSolution(() ->
                System.out.println("solution:" + Arrays.toString(values))
        );
        SearchStatistics stats = search.solve();
        System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
        System.out.format("Statistics: %s\n", stats);
    }
}
