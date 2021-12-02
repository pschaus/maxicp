package org.maxicp.model.constraints;

import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.Var;

import java.util.Collection;
import java.util.List;

public class Element1D implements Constraint {

    public final int[] T;
    public final IntVar y,z;

    @Override
    public Collection<Var> scope() {
        return List.of(y,z);
    }

    /**
     * T[y] = z
     * @param T
     */
    public Element1D(int [] T, IntVar y, IntVar z) {
        this.T = T;
        this.y = y;
        this.z = z;
    }
}
