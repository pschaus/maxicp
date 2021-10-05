package org.maxicp.model;

import org.maxicp.model.symbolic.SymbolicModel;

import java.util.HashSet;

public interface Model {
    void add(Constraint c);

    default Model symbolicCopy() {
        return new SymbolicModel(this);
    }

    /**
     * Jump to a specific constraint node.
     *
     * If the model is a concrete one, the new list should have a node in common with the previous one,
     * and this common node must be either the node at which the model was concretized, or a node created after that.
     *
     * @param node
     */
    void jumpTo(ConstraintListNode node);
    default void jumpTo(Model m) { jumpTo(m.getCstNode()); }

    ConstraintListNode getCstNode();
    Iterable<Constraint> getConstraints();
    ModelDispatcher getDispatcher();

    default Iterable<Var> getVariables() {
        HashSet<Var> allVars = new HashSet<>();
        for(Constraint c: getConstraints())
            for(Var v: c.scope())
                allVars.add(v);
        return allVars;
    }
}
