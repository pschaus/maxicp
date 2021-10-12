package org.maxicp.model.symbolic;

import org.maxicp.model.Constraint;
import org.maxicp.model.Model;
import org.maxicp.model.ModelDispatcher;

import java.util.Iterator;


public record SymbolicModel(Constraint c, SymbolicModel parent, ModelDispatcher md) implements Model, Iterable<Constraint> {
    @Override
    public SymbolicModel symbolicCopy() {
        return this;
    }

    @Override
    public Iterable<Constraint> getConstraints() {
        return this;
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return md;
    }

    @Override
    public Iterator<Constraint> iterator() {
        return new ConstraintListIterator(this);
    }

    private static class ConstraintListIterator implements Iterator<Constraint> {
        private SymbolicModel cur;

        public ConstraintListIterator(SymbolicModel start) {
            cur = start;
        }

        @Override
        public boolean hasNext() {
            return cur.parent != null;
        }

        @Override
        public Constraint next() {
            Constraint c = cur.c;
            cur = cur.parent;
            return c;
        }
    }

    public static SymbolicModel emptyModel(ModelDispatcher md) {
        return new SymbolicModel(null, null, md);
    }

    public SymbolicModel add(Constraint c) {
        return new SymbolicModel(c, this, md);
    }
}