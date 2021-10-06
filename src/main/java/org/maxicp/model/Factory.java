package org.maxicp.model;

public final class Factory {
    private Factory() {}

    static public ModelDispatcher makeModelDispatcher() {
        return new ModelDispatcher();
    }
}
