package org.maxicp.model;

import java.util.Iterator;

public interface Model {
    public void add(Constraint c);
    public Iterable<Constraint> getConstraints();
}
