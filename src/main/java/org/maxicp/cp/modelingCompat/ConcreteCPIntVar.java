package org.maxicp.cp.modelingCompat;

import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.concrete.ConcreteIntVar;
import org.maxicp.model.concrete.ConcreteVar;

public class ConcreteCPIntVar implements ConcreteIntVar, ConcreteCPVar {
    final CPIntVar v;
    final ModelDispatcher md;

    public ConcreteCPIntVar(ModelDispatcher md, CPIntVar v) {
        this.md = md;
        this.v = v;
    }

    @Override
    public int min() {
        return v.min();
    }

    @Override
    public int max() {
        return v.max();
    }

    @Override
    public int size() {
        return v.size();
    }

    @Override
    public boolean contains(int val) {
        return v.contains(val);
    }

    @Override
    public int fillArray(int[] array) {
        return v.fillArray(array);
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return md;
    }

    @Override
    public CPVar getVar() {
        return v;
    }

    @Override
    public String toString() {
        return v.toString();
    }
}
