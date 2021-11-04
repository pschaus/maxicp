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

import org.maxicp.state.StateManager;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.Procedure;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Depth First Search Branch and Bound implementation
 */
public class DFSearch extends AbstractSearchMethod {
    public DFSearch(StateManager sm, Supplier<Procedure[]> branching) {
        super(sm, branching);
    }

    // solution to DFS with explicit stack
    private void expandNode(Stack<Procedure> alternatives, SearchStatistics statistics) {
        Procedure[] alts = branching.get();
        if (alts.length == 0) {
            statistics.incrSolutions();
            notifySolution();
        } else {
            for (int i = alts.length - 1; i >= 0; i--) {
                Procedure a = alts[i];
                alternatives.push(() -> sm.restoreState());
                alternatives.push(() -> {
                    statistics.incrNodes();
                    a.call();
                    expandNode(alternatives, statistics);
                });
                alternatives.push(() -> sm.saveState());
            }
        }
    }

    protected void startSolve(SearchStatistics statistics, Predicate<SearchStatistics> limit) {
        Stack<Procedure> alternatives = new Stack<Procedure>();
        expandNode(alternatives, statistics);
        while (!alternatives.isEmpty()) {
            if (limit.test(statistics)) throw new StopSearchException();
            try {
                alternatives.pop().call();
            } catch (InconsistencyException e) {
                statistics.incrFailures();
                notifyFailure();
            }
        }
    }
}
