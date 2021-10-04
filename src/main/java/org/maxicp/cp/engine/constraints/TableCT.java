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
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.BitSet;

import static org.maxicp.cp.CPFactory.minus;

/**
 * Implementation of Compact Table algorithm described in
 * <p><i>Compact-Table: Efficiently Filtering Table Constraints with Reversible Sparse Bit-Sets</i>
 * Jordan Demeulenaere, Renaud Hartert, Christophe Lecoutre, Guillaume Perez, Laurent Perron, Jean-Charles Régin, Pierre Schaus
 * <p>See <a href="https://www.info.ucl.ac.be/~pschaus/assets/publi/cp2016-compacttable.pdf">The article.</a>
 */
public class TableCT extends AbstractCPConstraint {
    private CPIntVar[] x; //variables
    private int[][] table; //the table
    //supports[i][v] is the set of tuples supported by x[i]=v
    private BitSet[][] supports;

    private BitSet supportedTuples;
    private BitSet tmpSupport;

    private int[] dom; // domain iterator

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
    public TableCT(CPIntVar[] x, int[][] table) {
        super(x[0].getSolver());
        this.x = new CPIntVar[x.length];
        this.table = table;
        dom = new int[Arrays.stream(x).map(var -> var.size()).max(Integer::compare).get()];

        // Allocate supportedByVarVal
        supports = new BitSet[x.length][];
        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].min()); // map the variables domain to start at 0
            supports[i] = new BitSet[x[i].max() - x[i].min() + 1];
            for (int j = 0; j < supports[i].length; j++)
                supports[i][j] = new BitSet();
        }

        // Set values in supportedByVarVal, which contains all the tuples supported by each var-val pair
        for (int t = 0; t < table.length; t++) { //i is the index of the tuple (in table)
            for (int i = 0; i < x.length; i++) { //j is the index of the current variable (in x)
                if (x[i].contains(table[t][i])) {
                    supports[i][table[t][i] - x[i].min()].set(t);
                }
            }
        }

        supportedTuples = new BitSet(table.length);
        tmpSupport = new BitSet(table.length);
    }

    @Override
    public void post() {
        for (CPIntVar var : x)
            var.propagateOnDomainChange(this);
        propagate();
    }



    @Override
    public void propagate() {


        // Bit-set of tuple indices all set to 1
        supportedTuples.set(0, table.length);

        for (int i = 0; i < x.length; i++) {
            tmpSupport.clear();
            int nVal = x[i].fillArray(dom);
            for (int v = 0; v < nVal; v++) {
                    tmpSupport.or(supports[i][dom[v]]);
            }
            supportedTuples.and(tmpSupport);
            if (supportedTuples.isEmpty()) throw InconsistencyException.INCONSISTENCY;
        }

        for (int i = 0; i < x.length; i++) {
            int nVal = x[i].fillArray(dom);
            for (int v = 0; v < nVal; v++) {
                    if (!supports[i][dom[v]].intersects(supportedTuples)) {
                        x[i].remove(dom[v]);
                    }

            }
        }

    }
}
