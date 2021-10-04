package org.maxicp.model;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class IntVarSetImpl implements IntVar {

    TreeSet<Integer> dom;
    public IntVarSetImpl(Set<Integer> domain) {
        dom = new TreeSet<>();
        dom.addAll(domain);
    }

    @Override
    public int min() {
        return dom.first();
    }

    @Override
    public int max() {
        return dom.last();
    }

    @Override
    public Iterator<Integer> iterator() {
        return dom.iterator();
    }
}
