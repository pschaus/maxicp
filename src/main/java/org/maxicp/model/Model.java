package org.maxicp.model;

public interface Model {

    public void addAllDifferent();

    public IntVar [] makeIntVarArray(int size, int domSize);

    public void add(Constraint c);

}
