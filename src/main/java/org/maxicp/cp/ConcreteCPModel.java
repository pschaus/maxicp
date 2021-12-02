package org.maxicp.cp;

import org.maxicp.cp.engine.constraints.AllDifferentDC;
import org.maxicp.cp.engine.core.*;
import org.maxicp.cp.modelingCompat.ConcreteCPIntVar;
import org.maxicp.cp.modelingCompat.ConcreteCPVar;
import org.maxicp.model.*;
import org.maxicp.model.concrete.ConcreteModel;
import org.maxicp.model.concrete.ConcreteVar;
import org.maxicp.model.constraints.*;
import org.maxicp.model.symbolic.IntVarRangeImpl;
import org.maxicp.model.symbolic.IntVarSetImpl;
import org.maxicp.model.symbolic.IntVarViewOffset;
import org.maxicp.model.symbolic.SymbolicModel;
import org.maxicp.search.BestFirstSearch;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.state.State;
import org.maxicp.util.Procedure;

import java.util.*;
import java.util.function.Supplier;

public class ConcreteCPModel implements ConcreteModel {

    final State<SymbolicModel> model;
    final SymbolicModel concretizedNode;
    public final CPSolver solver;
    final HashMap<Var, ConcreteVar> mapping;
    final ModelDispatcher bm;

    public ConcreteCPModel(ModelDispatcher bm, CPSolver solver, SymbolicModel baseNode) {
        this.bm = bm;
        this.concretizedNode = baseNode;
        this.model = solver.getStateManager().makeStateRef(baseNode);
        this.solver = solver;
        this.mapping = new HashMap<>();

        for (Constraint c : baseNode) {
            instantiateConstraint(c);
        }
    }

    public DFSearch dfSearch(Supplier<Procedure[]> branching) {
        return new DFSearch(solver.getStateManager(), branching);
    }

    public <U extends Comparable<U>> BestFirstSearch<U> bestFirstSearch(Supplier<Procedure[]> branching, Supplier<U> nodeEvaluator) {
        return new BestFirstSearch<U>(bm, solver.getStateManager(), branching, nodeEvaluator);
    }

    public CPIntVar getVar(IntVar v) {
        return (CPIntVar) createCPVarIfNeeded(v);
    }

    public Objective minimize(IntVar v) {
        return new Minimize(getVar(v));
    }

    public Objective maximize(IntVar v) {
        return new Maximize(getVar(v));
    }


    public CPBoolVar getVar(BoolVar v) {
        return (CPBoolVar) createCPVarIfNeeded(v);
    }

    @Override
    public void add(Constraint c) {
        instantiateConstraint(c);
        model.setValue(model.value().add(c));
    }

    @Override
    public void jumpTo(SymbolicModel node) {
        //Find the first node in common
        HashSet<SymbolicModel> nodeCurrentlyPresent = new HashSet<>();

        SymbolicModel cur = model.value();
        while (cur != concretizedNode) {
            nodeCurrentlyPresent.add(cur);
            cur = cur.parent();
        }
        nodeCurrentlyPresent.add(cur);

        cur = node;
        while (cur != null && !nodeCurrentlyPresent.contains(cur)) {
            cur = cur.parent();
        }
        SymbolicModel firstCommonNode = cur;

        //Now list the nodes before this first common node
        nodeCurrentlyPresent.clear();
        cur = firstCommonNode;
        while (cur != null) {
            nodeCurrentlyPresent.add(cur);
            cur = cur.parent();
        }

        // Now revert the solver until we are at a node below the first common node
        while (!nodeCurrentlyPresent.contains(model.value())) {
            solver.getStateManager().restoreState();
        }
        solver.getStateManager().saveState();

        // We now juste have to add constraints until we are at the right node
        ArrayDeque<SymbolicModel> needConcretization = new ArrayDeque<>();
        cur = node;
        while (cur != model.value()) {
            needConcretization.addFirst(cur);
            cur = cur.parent();
        }
        for(SymbolicModel cln: needConcretization) {
            instantiateConstraint(cln.c());
        }

        model.setValue(node);
    }

    @Override
    public SymbolicModel symbolicCopy() {
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
            case Sum s -> {
                CPIntVar[] x = Arrays.stream(s.x).map(this::getVar).toArray(CPIntVar[]::new);
                CPIntVar y = getVar(s.y);
                solver.post(CPFactory.sum(x,y));
            }
            case Element2D e -> {
                CPIntVar x = getVar(e.x);
                CPIntVar y = getVar(e.y);
                CPIntVar z = getVar(e.z);
                solver.post(new org.maxicp.cp.engine.constraints.Element2D(e.T,x,y,z));
            }
            case Element1D e -> {
                CPIntVar y = getVar(e.y);
                CPIntVar z = getVar(e.z);
                solver.post(new org.maxicp.cp.engine.constraints.Element1D(e.T,y,z));
            }
            case LessOrEqual e -> {
                CPIntVar x = getVar(e.x);
                CPIntVar y = getVar(e.y);
                solver.post(new org.maxicp.cp.engine.constraints.LessOrEqual(x,y));
            }
            case CPInstantiableConstraint cpic -> solver.post(cpic.instantiate(this));
            default -> throw new IllegalStateException("Unexpected value: " + c);
        }
    }

    private CPVar createCPVarIfNeeded(Var v) {
        if (mapping.containsKey(v)) {
            return ((ConcreteCPVar) mapping.get(v)).getVar();
        }

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
