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

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.IntVar;
import org.maxicp.state.StateSparseBitSet;
import org.maxicp.util.exception.InconsistencyException;


import static org.maxicp.Factory.minus;

/**
 * Implementation of Compact Table algorithm described in
 * <p><i>Compact-Table: Efficiently Filtering Table Constraints with Reversible Sparse Bit-Sets</i>
 * Jordan Demeulenaere, Renaud Hartert, Christophe Lecoutre, Guillaume Perez, Laurent Perron, Jean-Charles Régin, Pierre Schaus
 * <p>See <a href="https://www.info.ucl.ac.be/~pschaus/assets/publi/cp2016-compacttable.pdf">The article.</a>
 */

public class TableCTNew extends AbstractCPConstraint {
    private IntVar[] x; //variables
    private int[][] table; //the table

    private StateSparseBitSet validTuples;
    private StateSparseBitSet.BitSet collected;
    //supports[i][v] is the set of tuples supported by x[i]=v
    private StateSparseBitSet.BitSet[][] supports;

    /**
     * Table constraint.
     * <p>The table constraint ensures that
     * {@code x} is a row from the given table.
     * More exactly, there exist some row <i>i</i>
     * such that
     * {@code x[0]==table[i][0], x[1]==table[i][1], etc}.
     *
     * <p>This constraint is sometimes called <i>in extension</i> constraint
     * as the user enumerates the set of solutions that can be taken
     * by the variables.
     *
     * @param x  the non empty set of variables to constraint
     * @param table the possible set of solutions for x.
     *              The second dimension must be of the same size as the array x.
     */
    public TableCTNew(IntVar[] x, int[][] table) {
        super(x[0].getSolver());
        this.x = new IntVar[x.length];
        this.table = table;

        validTuples = new StateSparseBitSet(x[0].getSolver().getStateManager(),table.length);
        collected = validTuples.new BitSet();

        // Allocate supportedByVarVal
        supports = new StateSparseBitSet.BitSet[x.length][];
        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].min()); // map the variables domain to start at 0
            supports[i] = new StateSparseBitSet.BitSet[x[i].max() - x[i].min() + 1];
            for (int j = 0; j < supports[i].length; j++)
                supports[i][j] = validTuples.new BitSet();
        }

        // Set values in supportedByVarVal, which contains all the tuples supported by each var-val pair
        for (int i = 0; i < table.length; i++) { //i is the index of the tuple (in table)
            for (int j = 0; j < x.length; j++) { //j is the index of the current variable (in x)
                if (x[j].contains(table[i][j])) {
                    supports[j][table[i][j] - x[j].min()].set(i);
                }
            }
        }
    }

    @Override
    public void post() {
        for (IntVar var : x)
            var.propagateOnDomainChange(this);
        propagate();
    }

    @Override
    public void propagate() {


        // Bit-set of tuple indices all set to 0
        collected.clear();

        // TODO 1: compute supportedTuples as
        // supportedTuples = (supports[0][x[0].min()] | ... | supports[0][x[0].max()] ) & ... &
        //                   (supports[x.length][x[0].min()] | ... | supports[x.length][x[0].max()] )
        //

        // STUDENT // This should be displayed instead of the actual code
        // BEGIN STRIP
        for (int i = 0; i < x.length; i++) {
            collected.clear();
            for (int v = x[i].min(); v <= x[i].max(); v++) {
                if (x[i].contains(v)) {
                    collected.union(supports[i][v]);
                }
            }
            validTuples.intersect(collected);
            if (validTuples.isEmpty()) {
                throw InconsistencyException.INCONSISTENCY;
            }
            // if empty, fail
        }
        // END STRIP

        // TODO 2
        for (int i = 0; i < x.length; i++) {
            for (int v = x[i].min(); v <= x[i].max(); v++) {
                if (x[i].contains(v)) {
                    // TODO 2 the condition for removing the setValue v from x[i] is to check if
                    // there is no intersection between supportedTuples and the support[i][v]
                    // STUDENT throw new NotImplementedException();
                    // BEGIN STRIP
                    if (validTuples.hasEmptyIntersection(supports[i][v])) {
                        x[i].remove(v);
                    }
                    // END STRIP
                }
            }
        }


        //throw new NotImplementedException("TableCT");
    }
}