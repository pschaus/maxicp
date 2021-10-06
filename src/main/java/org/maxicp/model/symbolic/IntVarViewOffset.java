package org.maxicp.model.symbolic;

import org.maxicp.model.IntVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.util.exception.IntOverFlowException;

public class IntVarViewOffset implements SymbolicIntVar {

    public final IntVar baseVar;
    public final int offset;
    private final ModelDispatcher bm;

    public IntVarViewOffset(IntVar x, int offset) { // y = x + o
        if (0L + x.min() + offset <= (long) Integer.MIN_VALUE)
            throw new IntOverFlowException("consider applying a smaller offset as the min domain on this view is <= Integer.MIN _VALUE");
        if (0L + x.max() + offset >= (long) Integer.MAX_VALUE)
            throw new IntOverFlowException("consider applying a smaller offset as the max domain on this view is >= Integer.MAX _VALUE");
        this.baseVar = x;
        this.offset = offset;
        this.bm = x.getDispatcher();
    }

    @Override
    public int initMin() {
        return baseVar.min()+ offset;
    }

    @Override
    public int initMax() {
        return baseVar.max()+ offset;
    }

    @Override
    public int initSize() {
        return baseVar.size();
    }

    @Override
    public boolean initContains(int v) {
        return baseVar.contains(v- offset);
    }

    @Override
    public int initFillArray(int[] array) {
        int s = baseVar.fillArray(array);
        for(int i = 0; i < s; i++)
            array[i] += offset;
        return s;
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return bm;
    }
}
