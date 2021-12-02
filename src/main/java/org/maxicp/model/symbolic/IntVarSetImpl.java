package org.maxicp.model.symbolic;

import org.maxicp.model.IntVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.concrete.ConcreteIntVar;
import org.maxicp.model.concrete.ConcreteModel;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class IntVarSetImpl implements SymbolicIntVar {
    public final TreeSet<Integer> dom;
    final ModelDispatcher bm;

    public IntVarSetImpl(ModelDispatcher bm, Set<Integer> domain) {
        dom = new TreeSet<>();
        dom.addAll(domain);
        this.bm = bm;
    }

    @Override
    public int initMin() {
        return dom.first();
    }

    @Override
    public int initMax() {
        return dom.last();
    }

    @Override
    public int initSize() {
        return dom.size();
    }

    @Override
    public boolean initContains(int v) {
        return dom.contains(v);
    }

    @Override
    public int initFillArray(int[] array) {
        int idx = 0;
        for(Integer i: dom) {
            array[idx] = i;
            idx++;
        }
        return idx;
    }

    @Override
    public String toString() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm) {
            return ((ConcreteIntVar) cm.getMapping().get(this)).toString();
        }
        return dom.toString();
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return bm;
    }
}
