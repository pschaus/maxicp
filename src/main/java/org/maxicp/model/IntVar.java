package org.maxicp.model;

import org.maxicp.model.symbolic.IntVarViewOffset;

import java.util.Iterator;

public interface IntVar extends Var {
    int min();
    int max();
    int size();
    boolean contains(int v);
    int fillArray(int[] array);
    default IntVar plus(int offset) {
        return new IntVarViewOffset(this, offset);
    }
    default IntVar minus(int offset) {
        return new IntVarViewOffset(this, -offset);
    }
}
