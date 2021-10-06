package org.maxicp.model.symbolic;

import org.maxicp.model.Constraint;
import org.maxicp.model.ConstraintListNode;
import org.maxicp.model.Model;
import org.maxicp.model.ModelDispatcher;

public class SymbolicModel implements Model {
    ConstraintListNode cur = null;
    final ModelDispatcher bm;

    public SymbolicModel(ModelDispatcher bm) {
        this.bm = bm;
    }
    public SymbolicModel(Model copyFrom) {
        this.bm = copyFrom.getDispatcher();
        this.cur = copyFrom.getCstNode();
    }

    @Override
    public void add(Constraint c) {
        cur = new ConstraintListNode(cur, c);
    }

    @Override
    public void jumpTo(ConstraintListNode node) {
        cur = node;
    }

    @Override
    public ConstraintListNode getCstNode() {
        return cur;
    }

    @Override
    public Iterable<Constraint> getConstraints() {
        return cur;
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return bm;
    }
}
