package org.maxicp.model.impls;

import org.maxicp.model.Constraint;
import org.maxicp.model.ConstraintListNode;
import org.maxicp.model.Model;

import java.util.Iterator;

public class ModelImpl implements Model {
    ConstraintListNode cur = null;

    @Override
    public void add(Constraint c) {
        cur = new ConstraintListNode(cur, c);
    }

    @Override
    public ConstraintListNode getCstNode() {
        return cur;
    }

    @Override
    public Iterable<Constraint> getConstraints() {
        return cur;
    }
}
