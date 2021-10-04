package org.maxicp.model;

import java.util.Iterator;

public interface IntVar extends Var, Iterable<Integer> {
    int min();
    int max();
    Iterator<Integer> iterator();
}
