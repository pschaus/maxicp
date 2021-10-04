package org.maxicp.cp;

import org.maxicp.cp.engine.constraints.AllDifferentDC;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.cp.engine.core.CPVar;
import org.maxicp.cp.engine.core.MiniCP;
import org.maxicp.model.*;
import org.maxicp.model.constraints.AllDifferent;
import org.maxicp.state.Copier;
import org.maxicp.state.Trailer;

import java.util.HashMap;

public class CPModelInstantiator {
    static public InstanciatedCPModel instantiate(Model m, boolean useTrailing) {
        CPSolver solver = new MiniCP(useTrailing ? new Trailer() : new Copier());

        HashMap<Var, CPVar> mapping = new HashMap<Var, CPVar>();
        for(Constraint c: m.getConstraints())
            instantiateConstraint(solver, mapping, c);

        return new InstanciatedCPModel(solver, mapping, m);
    }

    static public CPVar getCPVar(CPSolver cp, HashMap<Var, CPVar> mapping, Var v) {
        if(mapping.containsKey(v))
            return mapping.get(v);

        switch (v) {
            case IntVarImplSet iv -> {
                CPIntVar cpiv = CPFactory.makeIntVar(cp, iv.dom);
                mapping.put(v, cpiv);
                return cpiv;
            }
            case IntVarImplRange iv -> {
                CPIntVar cpiv = CPFactory.makeIntVar(cp, iv.min(), iv.max());
                mapping.put(v, cpiv);
                return cpiv;
            }
            default -> throw new IllegalStateException("Unexpected value: " + v);
        }
    }

    static public void instantiateConstraint(CPSolver cp, HashMap<Var, CPVar> mapping, Constraint c) {
        switch (c) {
            case AllDifferent a -> {
                CPIntVar[] args = a.scope().stream().map(x -> (CPIntVar) getCPVar(cp, mapping, x)).toArray(CPIntVar[]::new);
                cp.post(new AllDifferentDC(args));
            }
            default -> throw new IllegalStateException("Unexpected value: " + c);
        }
    }
}
