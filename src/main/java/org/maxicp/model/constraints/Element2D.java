package org.maxicp.model.constraints;

import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Element2D implements Constraint {

    public final int[][] T;
    public final IntVar x,y,z;

    @Override
    public Collection<Var> scope() {
        return List.of(x,y,z);
    }

    /**
     * T[x,y] = z
     * @param T
     */
    public Element2D(int [][] T, IntVar x, IntVar y, IntVar z) {
        this.T = T;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
