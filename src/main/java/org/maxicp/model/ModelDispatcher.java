package org.maxicp.model;

import org.maxicp.model.symbolic.IntVarRangeImpl;
import org.maxicp.model.symbolic.SymbolicModel;

import java.util.function.Function;

public class ModelDispatcher {
    private Model initialModel;
    private final ThreadLocal<Model> currentModel;

    public ModelDispatcher() {
        initialModel = new SymbolicModel(this);
        currentModel = ThreadLocal.withInitial(() -> this.initialModel);
    }

    /**
     * @return the current model
     */
    public Model getModel() {
        return currentModel.get();
    }

    /**
     * Set the current model to m. m should have this base model as origin.
     * @param m
     */
    public void setModel(Model m) {
        if(m.getDispatcher() != this)
            throw new RuntimeException("Model being assigned to this BaseModel does not originate from here");
        currentModel.set(m);
    }

    /**
     * Shortcut for baseModel.getModel().add(c);
     * @param c constraint to add
     */
    public void add(Constraint c) {
        getModel().add(c);
    }

    /**
     * Shortcut for baseModel.getModel().getCstNode();
     * @return the root constraint node, which is a actually a linked list of all constraints in the current model
     */
    public ConstraintListNode getCstNode() {
        return getModel().getCstNode();
    }

    /**
     * Shortcut for baseModel.getModel().getConstraints();
     * @return an iterable with all the constraints in the current model
     */
    public Iterable<Constraint> getConstraints() {
        return getModel().getConstraints();
    }

    public Iterable<Var> getVariables() {
        return getModel().getVariables();
    }

    /**
     * Create an array of n IntVars with domain between 0 and domSize-1, inclusive.
     * @param n size of the array, number of IntVars
     * @param domSize size of the domains. Domains are [0, domsize-1]
     */
    public IntVar[] intVarArray(int n, int domSize) {
        IntVar[] out = new IntVar[n];
        for(int i = 0; i < n; i++)
            out[i] = new IntVarRangeImpl(this, 0, domSize-1);
        return out;
    }

    public IntVar[] makeIntVarArray(int n, Function<Integer, IntVar> body) {
        IntVar[] t = new IntVar[n];
        for (int i = 0; i < n; i++)
            t[i] = body.apply(i);
        return t;
    }

    public IntVar intVar(int min, int max) {
        return new IntVarRangeImpl(this, min, max);
    }
}
