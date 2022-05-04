package org.maxicp.state.datastructures;


import org.maxicp.state.StateInt;
import org.maxicp.state.StateManager;

import java.util.Set;

/**
 * Sequence set implemented using a sparse-set data structure
 * that can be saved and restored through
 * the {@link org.maxicp.state.StateManager#saveState()} / {@link org.maxicp.state.StateManager#restoreState()}
 * methods.
 * The sequence is split into three sets: required values, possible values and excluded values
 */
public class StateSequenceSet {

    protected int[] elems;
    protected int[] elemPos;
    protected StateInt r;  // delimiter for the required values. They are included within 0...s-1
    protected StateInt p;  // delimiter for the possible values. They are included within s...p-1
    protected int n; // maximum number of elements

    protected int ofs; // offset
    protected int nOmitted; // number of values that are put in the exclusion set as soon as the instance was created

    /**
     * create a sequence set with the elements {@code {R : {}, P: {0,...,n-1}, E: {}}}
     * @param sm the state manager that will save and restore the set when
     *        {@link org.maxicp.state.StateManager#saveState()} / {@link org.maxicp.state.StateManager#restoreState()}
     *           methods are called
     * @param n number of elements within the set
     */
    public StateSequenceSet(StateManager sm, int n) {
        this(sm, 0, n-1);
    }

    /**
     * create a sequence set with the elements {@code {R : {}, P: {min,...,max}, E: {}}}
     * @param sm the state manager that will save and restore the set when
     *        {@link StateManager#saveState()} / {@link StateManager#restoreState()}
     *           methods are called
     * @param minInclusive minimum value of the domain
     * @param maxInclusive maximum value of the domain with {@code maxInclusive >= minInclusive}
     */
    public StateSequenceSet(StateManager sm, int minInclusive, int maxInclusive) {
        n = maxInclusive - minInclusive + 1;
        ofs = minInclusive;
        nOmitted = 0;
        r = sm.makeStateInt(0);
        p = sm.makeStateInt(n);
        elems = new int[n];
        elemPos = new int[n];
        for (int i = 0; i < n; i++) {
            elems[i] = i;
            elemPos[i] = i;
        }
    };

    /**
     * create a sequence set with the elements {@code {R : {}, P: values, E: {}}}
     * @param sm the state manager that will save and restore the set when
     *        {@link StateManager#saveState()} / {@link StateManager#restoreState()}
     *           methods are called
     * @param values values to set in the domain
     */
    public StateSequenceSet(StateManager sm, Set<Integer> values) {
        this(sm, values.stream().min(Integer::compareTo).get(), values.stream().max(Integer::compareTo).get());
        for (int i = ofs; i < n + ofs ; ++i) {
            if (!values.contains(i)) {
                exclude(i);
                ++nOmitted;
            }
        }
    }

    /**
     * move a value from the set of possible values to the set of excluded values
     * @param val value to mark as excluded
     * @return true if the value has been moved from the set of possible to the set of excluded
     */
    public boolean exclude(int val) {
        if (!isPossible(val))
            return false; // the value is already in the excluded set or in the set of required
        val -= ofs;
        this.p.decrement();
        exchangePositions(val, elems[p.value()]);
        return true;
    }

    /**
     * move a value from the set of possible values to the set of required values
     * @param val value to mark as required
     * @return true if the value has been moved from the set of possible to the set of required
     */
    public boolean require(int val) {
        if (!isPossible(val))
            return false; // the value is already in the excluded set or in the set of required
        val -= ofs;
        exchangePositions(val, elems[r.value()]);
        this.r.increment();
        return true;
    }

    /**
     * set one value as the required value and move all other values into the exclusion set
     * @param v unique value that will be set as required
     * @return true if the set of required was empty and the value has been marked as required
     */
    public boolean requireOne(int v) {
        if (!isPossible(v) || r.value() != 0)
            return false;
        // the value is set in the first position and the value for s and p are updated
        int val = elems[0];
        int index = elemPos[v];
        elemPos[v] = 0;
        elems[0] = v;
        elemPos[val] = index;
        elems[index] = val;
        r.setValue(1);
        p.setValue(1);
        return true;
    }


    /**
     * exchange the position of two values
     * @param val1 first value to exchange
     * @param val2 second value to exchange
     */
    private void exchangePositions(int val1, int val2) {
        assert (checkVal(val1));
        assert (checkVal(val2));
        int v1 = val1;
        int v2 = val2;
        int i1 = elemPos[v1];
        int i2 = elemPos[v2];
        elems[i1] = v2;
        elems[i2] = v1;
        elemPos[v1] = i2;
        elemPos[v2] = i1;
    }

    /**
     * @param val value to examine
     * @return true if the value belongs to the set of values
     */
    private boolean checkVal(int val) {
        assert (val < elems.length);
        return true;
    }

    /**
     * move all possible values into the set of excluded values
     * @return true if the set of possible values has been reduced
     */
    public boolean excludeAllPossible() {
        if (p.value() == r.value())
            return false;
        this.p.setValue(r.value());
        return true;
    }

    public boolean isRequired(int val) {
        val -= ofs;
        if (val < 0 || val >= n)
            return false;
        else
            return elemPos[val] < r.value();
    }

    public boolean isExcluded(int val) {
        val -= ofs;
        if (val < 0 || val >= n)
            return false;
        else
            return elemPos[val] >= p.value() && elemPos[val] < n - nOmitted;
    }

    public boolean isPossible(int val) {
        val -= ofs;
        if (val < 0 || val >= n)
            return false;
        else
            return elemPos[val] < p.value() && elemPos[val] >= r.value();
    }

    /**
     * tell if a value belongs to the domain
     * @param val value to train1
     * @return true if the value is either required, possible or excluded
     */
    public boolean contains(int val) {
        val -= ofs;
        if (val < 0 || val >= n)
            return false;
        return elemPos[val] < n - nOmitted;
    }

    public int nPossible() {
        return p.value() - r.value();
    }

    public int nExcluded() {
        return n - p.value() - nOmitted;
    }

    public int nRequired() {
        return r.value();
    }

    public int size() { return n - nOmitted;}

    public int getRequired(int[] dest) {
        int size = r.value();
        for (int i = 0; i < size ; ++i)
            dest[i] = elems[i] + ofs;
        return size;
    }

    public int getPossible(int[] dest) {
        int begin = r.value();
        int end = p.value() - begin;
        for (int i = 0; i < end ; ++i)
            dest[i] = elems[i + begin] + ofs;
        return end;
    }

    public int getExcluded(int[] dest) {
        int begin = p.value();
        int end = n - begin - nOmitted;
        for (int i = 0; i < end; ++i)
            dest[i] = elems[i + begin] + ofs;
        return end;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("R: {");

        int i=0;
        int rVal = r.value();
        int pVal = p.value();
        while (i < rVal - 1) {
            b.append(elems[i++] + ofs);
            b.append(',');
        }
        if (rVal > 0)
            b.append(elems[i++] + ofs);
        b.append("}\nP: {");

        while (i < pVal - 1) {
            b.append(elems[i++] + ofs);
            b.append(',');
        }
        if (pVal - rVal > 0)
            b.append(elems[i++] + ofs);
        b.append("}\nE: {");

        while (i < n - 1 - nOmitted) {
            b.append(elems[i++] + ofs);
            b.append(',');
        }
        if (nExcluded() > 0)
            b.append(elems[i++] + ofs);
        b.append('}');
        return b.toString();
    }

}
