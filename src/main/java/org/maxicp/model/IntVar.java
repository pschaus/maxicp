package org.maxicp.model;

import java.util.Iterator;

public interface IntVar extends Var {
    int min();
    int max();
    int size();
    boolean contains(int v);
    int fillArray(int[] array);
}
