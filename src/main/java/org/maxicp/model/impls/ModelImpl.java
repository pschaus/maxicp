package org.maxicp.model.impls;

import org.maxicp.model.Constraint;
import org.maxicp.model.IntVar;
import org.maxicp.model.Model;

import java.util.Iterator;

public class ModelImpl implements Model {
    ConstraintListNode cur = null;

    @Override
    public void add(Constraint c) {
        cur = new ConstraintListNode(cur, c);
    }

    @Override
    public Iterable<Constraint> getConstraints() {
        return cur;
    }

    private record ConstraintListNode(ConstraintListNode parent, Constraint value) implements Iterable<Constraint> {
        @Override
        public Iterator<Constraint> iterator() {
            return new ConstraintListIterator(this);
        }
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
