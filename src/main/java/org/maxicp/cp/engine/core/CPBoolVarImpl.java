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


import org.maxicp.util.Procedure;

public class CPBoolVarImpl implements CPBoolVar {

    private CPIntVar binaryVar;

    /**
     * Create a boolean variable view from the binary variable
     * @param binaryVar
     */
    public CPBoolVarImpl(CPIntVar binaryVar) {
        if (binaryVar.max() > 1 || binaryVar.min() < 0) {
            throw new IllegalArgumentException("must be a binary {0,1} variable");
        }
        this.binaryVar = binaryVar;
    }

    public CPBoolVarImpl(CPSolver cp) {
        this.binaryVar = new CPIntVarImpl(cp, 0, 1);
    }

    @Override
    public boolean isTrue() {
        return min() == 1;
    }

    @Override
    public boolean isFalse() {
        return max() == 0;
    }

    @Override
    public void fix(boolean b) {
        fix(b ? 1 : 0);
    }

    @Override
    public CPSolver getSolver() {
        return binaryVar.getSolver();
    }

    @Override
    public void whenFixed(Procedure f) {
        binaryVar.whenFixed(f);
    }

    @Override
    public void whenBoundChange(Procedure f) {
        binaryVar.whenBoundChange(f);
    }

    @Override
    public void whenDomainChange(Procedure f) {
        binaryVar.whenDomainChange(f);
    }

    @Override
    public void propagateOnDomainChange(CPConstraint c) {
        binaryVar.propagateOnDomainChange(c);
    }

    @Override
    public void propagateOnFix(CPConstraint c) {
        binaryVar.propagateOnFix(c);
    }

    @Override
    public void propagateOnBoundChange(CPConstraint c) {
        binaryVar.propagateOnBoundChange(c);
    }

    @Override
    public int min() {
        return binaryVar.min();
    }

    @Override
    public int max() {
        return binaryVar.max();
    }

    @Override
    public int size() {
        return binaryVar.size();
    }

    @Override
    public int fillArray(int[] dest) {
        return binaryVar.fillArray(dest);
    }

    @Override
    public boolean isFixed() {
        return binaryVar.isFixed();
    }

    @Override
    public boolean contains(int v) {
        return binaryVar.contains(v);
    }

    @Override
    public void remove(int v) {
        binaryVar.remove(v);
    }

    @Override
    public void fix(int v) {
        binaryVar.fix(v);
    }

    @Override
    public void removeBelow(int v) {
        binaryVar.removeBelow(v);
    }

    @Override
    public void removeAbove(int v) {
        binaryVar.removeAbove(v);
    }

    @Override
    public int fillDeltaArray(int oldMin, int oldMax, int oldSize, int[] dest) {
        return binaryVar.fillDeltaArray(oldMin,oldMax,oldSize,dest);
    }

    @Override
    public DeltaCPIntVar delta(CPConstraint c) {
        return binaryVar.delta(c);
    }

    @Override
    public String toString() {
        if (isTrue()) return "true";
        else if (isFalse()) return "false";
        else return "{false,true}";
    }
}