package org.maxicp.model.concrete;

import org.maxicp.model.Constraint;
import org.maxicp.model.Model;
import org.maxicp.model.Var;
import org.maxicp.model.symbolic.SymbolicModel;

import java.util.HashMap;

public interface ConcreteModel extends Model {
    HashMap<Var, ConcreteVar> getMapping();

    void add(Constraint c);

    /**
     * Jump to a specific constraint node.
     *
     * The new list should have a node in common with current model,
     * and this common node must be either the node at which the model was concretized, or a node created after that.
     *
     * @param m the model to jump to
     */
    void jumpTo(SymbolicModel m);
}
