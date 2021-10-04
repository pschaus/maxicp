package org.maxicp.model;

import org.maxicp.model.impls.ModelImpl;

import java.util.Iterator;

public record ConstraintListNode(ConstraintListNode parent, Constraint value) implements Iterable<Constraint> {
    @Override
    public Iterator<Constraint> iterator() {
        return new ConstraintListIterator(this);
    }

    private static class ConstraintListIterator implements Iterator<Constraint> {
        private ConstraintListNode cur;

        public ConstraintListIterator(ConstraintListNode start) {
            cur = start;
        }

        @Override
        public boolean hasNext() {
            return cur != null;
        }

        @Override
        public Constraint next() {
            Constraint c = cur.value;
            cur = cur.parent;
            return c;
        }
    }
}
