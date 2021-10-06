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


import org.maxicp.state.State;

import java.util.ArrayList;


/**
 * Abstract class the most of the constraints
 * should extend.
 */
public abstract class AbstractCPConstraint implements CPConstraint {

    /**
     * The solver in which the constraint is created
     */
    private final CPSolver cp;
    private boolean scheduled = false;
    private final State<Boolean> active;

    private ArrayList<Delta> deltas;

    public AbstractCPConstraint(CPSolver cp) {
        this.cp = cp;
        active = cp.getStateManager().makeStateRef(true);
    }

    public void post() {
    }

    public CPSolver getSolver() {
        return cp;
    }

    public void propagate() {
    }

    public void registerDelta(Delta delta) {
        deltas.add(delta);
        delta.update();
    }

    private void updateDeltas() {
        for (Delta d: deltas) {
            d.update();
        }
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setActive(boolean active) {
        this.active.setValue(active);
    }

    public boolean isActive() {
        return active.value();
    }
}
