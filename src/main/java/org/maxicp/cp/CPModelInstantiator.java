package org.maxicp.cp;

import org.maxicp.cp.engine.constraints.AllDifferentDC;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.model.Constraint;
import org.maxicp.model.Model;

public class CPModelInstantiator {
    static HashMap<Constraint, ConstraintCreator> constraints;
    static public CPSolver instantiate(Model m) {
        for(Constraint c: m.getConstraints()) {
            switch (c.getClass()) {
                case AllDifferent(a, b, c, d) -> new AllDifferentDC(a.instac)
            }
        }
    }
}
