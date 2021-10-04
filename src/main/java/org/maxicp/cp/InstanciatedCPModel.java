package org.maxicp.cp;

import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.cp.engine.core.CPVar;
import org.maxicp.model.*;
import org.maxicp.state.State;

import java.util.HashMap;

import static org.maxicp.cp.CPModelInstantiator.getCPVar;
import static org.maxicp.cp.CPModelInstantiator.instantiateConstraint;

public class InstanciatedCPModel implements Model {
    final State<ConstraintListNode> model;
    public final CPSolver solver;
    final HashMap<Var, CPVar> mapping;

    public InstanciatedCPModel(CPSolver solver, HashMap<Var, CPVar> mapping, ConstraintListNode baseNode) {
        this.model = solver.getStateManager().makeStateRef(baseNode);
        this.solver = solver;
        this.mapping = mapping;
    }

    public CPIntVar getVar(IntVar v) {
        return (CPIntVar) getCPVar(solver, mapping, v);
    }

    public CPBoolVar getVar(BoolVar v) {
        return (CPBoolVar) getCPVar(solver, mapping, v);
    }



    @Override
    public void add(Constraint c) {
        model.setValue(new ConstraintListNode(model.value(), c));
        instantiateConstraint(solver, mapping, c);
    }

    @Override
    public ConstraintListNode getCstNode() {
        return model.value();
    }

    @Override
    public Iterable<Constraint> getConstraints() {
        return model.value();
    }
}
