package org.maxicp.cp.engine.core;

public interface InsertionListener {

    /**
     * Called whenever the InsertionVar has been inserted to one point
     */
    void insert();

    /**
     * called whenever a possible insertion has been removed from its corresponding sequence
     */
    void exclude();

    /**
     * called whenever the number of insertion related to this InsertionVar has changed
     */
    void change();

}
