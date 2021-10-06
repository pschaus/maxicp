package org.maxicp.cp.engine.core;

import java.util.Iterator;

public interface DeltaCPIntVar extends Delta {

    CPIntVar variable();
    int oldMin();
    public int oldMax();
    public int oldSize();
    public boolean changed();
    public int size();
    public Iterator<Integer> values();
    public void fillArray(int [] values);
    public boolean minChanged();
    public boolean maxChanged();
}
