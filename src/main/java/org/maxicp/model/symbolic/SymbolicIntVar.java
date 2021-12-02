package org.maxicp.model.symbolic;

import org.maxicp.model.IntVar;
import org.maxicp.model.concrete.ConcreteIntVar;
import org.maxicp.model.concrete.ConcreteModel;

public interface SymbolicIntVar extends IntVar, SymbolicVar {

    default int to() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteIntVar) cm.getMapping().get(this)).min();
        return initMin();
    }

    default int min() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteIntVar) cm.getMapping().get(this)).min();
        return initMin();
    }

    default int max() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteIntVar) cm.getMapping().get(this)).max();
        return initMax();
    }

    default int size() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteIntVar) cm.getMapping().get(this)).size();
        return initSize();
    }

    default boolean contains(int v) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteIntVar) cm.getMapping().get(this)).contains(v);
        return initContains(v);
    }

    default int fillArray(int[] array) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteIntVar) cm.getMapping().get(this)).fillArray(array);
        return initFillArray(array);
    }

    int initMin();
    int initMax();
    int initSize();
    boolean initContains(int v);
    int initFillArray(int[] array);

}
