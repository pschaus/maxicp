package org.maxicp.cp.modelingCompat;

import org.maxicp.cp.CPFactory;
import org.maxicp.cp.CPInstantiableConstraint;
import org.maxicp.cp.engine.constraints.AllDifferentDC;
import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.cp.engine.core.CPVar;
import org.maxicp.model.*;
import org.maxicp.model.concrete.ConcreteModel;
import org.maxicp.model.concrete.ConcreteVar;
import org.maxicp.model.constraints.AllDifferent;
import org.maxicp.model.constraints.Equal;
import org.maxicp.model.constraints.NotEqual;
import org.maxicp.model.symbolic.IntVarRangeImpl;
import org.maxicp.model.symbolic.IntVarSetImpl;
import org.maxicp.model.symbolic.IntVarViewOffset;
import org.maxicp.state.State;

import java.util.Arrays;
import java.util.HashMap;

public class InstanciatedCPModel implements ConcreteModel {
    final State<ConstraintListNode> model;
    public final CPSolver solver;
    final HashMap<Var, ConcreteVar> mapping;
    final ModelDispatcher bm;

    public InstanciatedCPModel(ModelDispatcher bm, CPSolver solver, ConstraintListNode baseNode) {
        this.bm = bm;
        this.model = solver.getStateManager().makeStateRef(baseNode);
        this.solver = solver;
        this.mapping = new HashMap<>();

        for(Constraint c: baseNode)
            instantiateConstraint(c);
    }

    public CPIntVar getVar(IntVar v) {
        return (CPIntVar) createCPVarIfNeeded(v);
    }

    public CPBoolVar getVar(BoolVar v) {
        return (CPBoolVar) createCPVarIfNeeded(v);
    }

    @Override
    public void add(Constraint c) {
        model.setValue(new ConstraintListNode(model.value(), c));
        instantiateConstraint(c);
    }

    @Override
    public ConstraintListNode getCstNode() {
        return model.value();
    }

    @Override
    public Iterable<Constraint> getConstraints() {
        return model.value();
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return bm;
    }

    @Override
    public HashMap<Var, ConcreteVar> getMapping() {
        return mapping;
    }

    private void instantiateConstraint(Constraint c) {
        switch (c) {
            case AllDifferent a -> {
                CPIntVar[] args = Arrays.stream(a.x).map(this::getVar).toArray(CPIntVar[]::new);
                solver.post(new AllDifferentDC(args));
            }
            case Equal e -> solver.post(CPFactory.equal(getVar(e.x), e.v));
            case NotEqual e -> solver.post(CPFactory.notEqual(getVar(e.x), e.v));
            case CPInstantiableConstraint cpic -> solver.post(cpic.instantiate(this));
            default -> throw new IllegalStateException("Unexpected value: " + c);
        }
    }

    private CPVar createCPVarIfNeeded(Var v) {
        if(mapping.containsKey(v))
            return ((ConcreteCPVar)mapping.get(v)).getVar();

        CPIntVar cpiv = switch (v) {
            case IntVarSetImpl iv -> CPFactory.makeIntVar(solver, iv.dom);
            case IntVarRangeImpl iv -> CPFactory.makeIntVar(solver, iv.min(), iv.max());
            case IntVarViewOffset iv -> CPFactory.plus(((CPIntVar)createCPVarIfNeeded(iv.baseVar)), iv.offset);
            default -> throw new IllegalStateException("Unexpected value: " + v);
        };

        mapping.put(v, new ConcreteCPIntVar(v.getDispatcher(), cpiv));
        return cpiv;
    }
}
