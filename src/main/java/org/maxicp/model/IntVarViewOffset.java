package org.maxicp.model;

import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.util.exception.IntOverFlowException;

import java.util.Iterator;

public class IntVarViewOffset implements IntVar {

    private final IntVar x;
    private final int o;

    public IntVarViewOffset(IntVar x, int offset) { // y = x + o
        if (0L + x.min() + offset <= (long) Integer.MIN_VALUE)
            throw new IntOverFlowException("consider applying a smaller offset as the min domain on this view is <= Integer.MIN _VALUE");
        if (0L + x.max() + offset >= (long) Integer.MAX_VALUE)
            throw new IntOverFlowException("consider applying a smaller offset as the max domain on this view is >= Integer.MAX _VALUE");
        this.x = x;
        this.o = offset;

    }

    @Override
    public int min() {
        return x.min()+o;
    }

    @Override
    public int max() {
        return x.max()+o;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            Iterator<Integer> ite = x.iterator();
            @Override
            public boolean hasNext() {
                return ite.hasNext();
            }

            @Override
            public Integer next() {
                return ite.next()+o;
            }
        };
    }
}
