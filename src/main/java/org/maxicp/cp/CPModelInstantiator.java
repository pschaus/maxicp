package org.maxicp.cp;

import org.maxicp.cp.engine.constraints.AllDifferentDC;
import org.maxicp.cp.engine.core.*;
import org.maxicp.cp.modelingCompat.ConcreteCPIntVar;
import org.maxicp.cp.modelingCompat.ConcreteCPVar;
import org.maxicp.cp.modelingCompat.InstanciatedCPModel;
import org.maxicp.model.*;
import org.maxicp.model.concrete.ConcreteBoolVar;
import org.maxicp.model.concrete.ConcreteIntVar;
import org.maxicp.model.concrete.ConcreteVar;
import org.maxicp.model.constraints.AllDifferent;
import org.maxicp.model.symbolic.IntVarRangeImpl;
import org.maxicp.model.symbolic.IntVarSetImpl;
import org.maxicp.state.Copier;
import org.maxicp.state.Trailer;

import java.util.HashMap;

public class CPModelInstantiator {
    static public InstanciatedCPModel instantiate(Model m) {
        return instantiate(m, true);
    }

    static public InstanciatedCPModel instantiate(Model m, boolean useTrailing) {
        CPSolver solver = new MiniCP(useTrailing ? new Trailer() : new Copier());

        HashMap<Var, ConcreteVar> mapping = new HashMap<Var, ConcreteVar>();
        for(Constraint c: m.getConstraints())
            instantiateConstraint(solver, mapping, c);

        return new InstanciatedCPModel(m.getDispatcher(), solver, mapping, m.getCstNode());
    }

    static public CPVar getCPVar(CPSolver cp, HashMap<Var, ConcreteVar> mapping, Var v) {
        if(mapping.containsKey(v))
            return ((ConcreteCPVar)mapping.get(v)).getVar();
        switch (v) {
            case IntVarSetImpl iv -> {
                CPIntVar cpiv = CPFactory.makeIntVar(cp, iv.dom);
                mapping.put(v, new ConcreteCPIntVar(v.getDispatcher(), cpiv));
                return cpiv;
            }
            case IntVarRangeImpl iv -> {
                CPIntVar cpiv = CPFactory.makeIntVar(cp, iv.min(), iv.max());
                mapping.put(v, new ConcreteCPIntVar(v.getDispatcher(), cpiv));
                return cpiv;
            }
            default -> throw new IllegalStateException("Unexpected value: " + v);
        }
    }

    static public void instantiateConstraint(CPSolver cp, HashMap<Var, ConcreteVar> mapping, Constraint c) {
        switch (c) {
            case AllDifferent a -> {
                CPIntVar[] args = a.scope().stream().map(x -> (CPIntVar) getCPVar(cp, mapping, x)).toArray(CPIntVar[]::new);
                cp.post(new AllDifferentDC(args));
            }
            default -> throw new IllegalStateException("Unexpected value: " + c);
        }
    }

}
