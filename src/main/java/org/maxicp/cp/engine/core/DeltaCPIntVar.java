package org.maxicp.cp.engine.core;

import java.util.Iterator;

public interface DeltaCPIntVar extends Delta, Iterable<Integer> {

    CPIntVar variable();
    int oldMin();
    public int oldMax();
    public int oldSize();
    public boolean changed();
    public int size();
    public Iterator<Integer> iterator();
    public int fillArray(int [] values);
    public boolean minChanged();
    public boolean maxChanged();
}
