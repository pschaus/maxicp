package org.maxicp.model.symbolic;

import org.maxicp.model.IntVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.util.exception.IntOverFlowException;

public class IntVarViewOffset implements SymbolicIntVar {

    private final IntVar x;
    private final int o;
    private final ModelDispatcher bm;

    public IntVarViewOffset(IntVar x, int offset) { // y = x + o
        if (0L + x.min() + offset <= (long) Integer.MIN_VALUE)
            throw new IntOverFlowException("consider applying a smaller offset as the min domain on this view is <= Integer.MIN _VALUE");
        if (0L + x.max() + offset >= (long) Integer.MAX_VALUE)
            throw new IntOverFlowException("consider applying a smaller offset as the max domain on this view is >= Integer.MAX _VALUE");
        this.x = x;
        this.o = offset;
        this.bm = x.getDispatcher();
    }

    @Override
    public int initMin() {
        return x.min()+o;
    }

    @Override
    public int initMax() {
        return x.max()+o;
    }

    @Override
    public int initSize() {
        return x.size();
    }

    @Override
    public boolean initContains(int v) {
        return x.contains(v-o);
    }

    @Override
    public int initFillArray(int[] array) {
        int s = x.fillArray(array);
        for(int i = 0; i < s; i++)
            array[i] += o;
        return s;
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return bm;
    }
}
