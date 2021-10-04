package org.maxicp.cp.modelingCompat;

import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.concrete.ConcreteBoolVar;

public class ConcreteCPBoolVar implements ConcreteBoolVar, ConcreteCPVar {
    final CPBoolVar v;
    final ModelDispatcher md;

    public ConcreteCPBoolVar(ModelDispatcher md, CPBoolVar v) {
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
}
