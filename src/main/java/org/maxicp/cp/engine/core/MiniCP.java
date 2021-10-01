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

import org.maxicp.cp.CPFactory;
import org.maxicp.search.Objective;
import org.maxicp.state.StateManager;
import org.maxicp.state.StateStack;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.Procedure;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MiniCP implements CPSolver {

    private Queue<CPConstraint> propagationQueue = new ArrayDeque<>();
    private List<Procedure> fixPointListeners = new LinkedList<>();

    private final StateManager sm;

    private final StateStack<CPIntVar> vars;

    public MiniCP(StateManager sm) {
        this.sm = sm;
        vars = new StateStack<>(sm);
    }

    @Override
    public StateManager getStateManager() {
        return sm;
    }

    public void schedule(CPConstraint c) {
        if (c.isActive() && !c.isScheduled()) {
            c.setScheduled(true);
            propagationQueue.add(c);
        }
    }

    @Override
    public void onFixPoint(Procedure listener) {
        fixPointListeners.add(listener);
    }

    private void notifyFixPoint() {
        fixPointListeners.forEach(s -> s.call());
    }

    @Override
    public void fixPoint() {
        try {
            notifyFixPoint();
            while (!propagationQueue.isEmpty()) {
                propagate(propagationQueue.remove());
            }
        } catch (InconsistencyException e) {
            // empty the queue and unset the scheduled status
            while (!propagationQueue.isEmpty())
                propagationQueue.remove().setScheduled(false);
            throw e;
        }
    }

    private void propagate(CPConstraint c) {
        c.setScheduled(false);
        if (c.isActive())
            c.propagate();
    }

    @Override
    public Objective minimize(CPIntVar x) {
        return new Minimize(x);
    }

    @Override
    public Objective maximize(CPIntVar x) {
        return minimize(CPFactory.minus(x));
    }

    @Override
    public void post(CPConstraint c) {
        post(c, true);
    }

    @Override
    public void post(CPConstraint c, boolean enforceFixPoint) {
        c.post();
        if (enforceFixPoint) fixPoint();
    }

    @Override
    public void post(CPBoolVar b) {
        b.assign(true);
        fixPoint();
    }
}
