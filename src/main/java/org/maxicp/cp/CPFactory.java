package org.maxicp.cp;

import org.maxicp.cp.engine.constraints.AllDifferentDC;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.model.Model;

public class CPFactory {
    static HashMap<Constraint, Function<...>>
    static public CPSolver instantiate(Model m) {
        for(Constraint c: m.getConstraints()) {
            switch (c.class) {
                case AllDifferent(a,b,c,d): new AllDifferentDC(a.instac)
            }
        }
    }
}
