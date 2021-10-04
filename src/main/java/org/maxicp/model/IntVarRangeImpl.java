package org.maxicp.model;

import java.util.Iterator;

public class IntVarRangeImpl implements IntVar {

    private int min;
    private int max;

    public IntVarRangeImpl(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int min() {
        return this.min;
    }

    @Override
    public int max() {
        return this.max;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int i = min();
            @Override
            public boolean hasNext() {
                return i <= max;
            }

            @Override
            public Integer next() {
                int v = i;
                i++;
                return v;
            }
        };
    }
}
