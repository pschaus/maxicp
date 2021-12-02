package org.maxicp.model.symbolic;

import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.concrete.ConcreteIntVar;
import org.maxicp.model.concrete.ConcreteModel;

public class IntVarRangeImpl implements SymbolicIntVar {

    private final int min;
    private final int max;
    private final ModelDispatcher bm;

    public IntVarRangeImpl(ModelDispatcher bm, int min, int max) {
        this.min = min;
        this.max = max;
        this.bm = bm;
    }

    @Override
    public int initMin() {
        return this.min;
    }

    @Override
    public int initMax() {
        return this.max;
    }

    @Override
    public int initSize() {
        return max-min+1;
    }

    @Override
    public boolean initContains(int v) {
        return min <= v && v <= max;
    }

    @Override
    public int initFillArray(int[] array) {
        for (int i = 0; i < initSize(); i++)
            array[i] = min + i;
        return initSize();
    }

    @Override
    public String toString() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm) {
            return ((ConcreteIntVar) cm.getMapping().get(this)).toString();
        }
        return "{"+initMin()+".."+initMax()+"}";
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return bm;
    }
}
