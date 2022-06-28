/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */


package org.maxicp.state.datastructures;


import org.maxicp.state.State;
import org.maxicp.state.StateInt;
import org.maxicp.state.StateManager;

import java.util.Arrays;


/**
 * Class to represent a bit-set that can be saved and restored through
 * the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 */
public class StateSparseBitSet {

    /* Variables used to store value of the bitset */
    private int nWords;
    private State<Long>[] words;

    /* Variables used to make set sparse */
    private int[] nonZeroIdx;
    private StateInt nNonZero;

    private Long mask;

    /* Temp variable */
    public CollectionBitSet collection;

    /**
     * Bitset of the same capacity as the outer {@link StateSparseBitSet}.
     * It is not synchronized with  {@link StateManager}.
     * It is rather intended to be used as parameter to the
     * {@link #intersect(BitSet)} method to modify the outer {@link StateSparseBitSet}.
     */
    public class SupportBitSet extends BitSet {

        public SupportBitSet() {
            super(nWords);
        }

        public SupportBitSet(BitSet anotherBitSet) {
            this(anotherBitSet, false);
        }

        public SupportBitSet(BitSet anotherBitSet, boolean shared) {
            super(anotherBitSet, shared);
            assert anotherBitSet.nbWords == nWords : "BitSet size is incomptible with State Sparse BitSet size";
        }

    }

    /**
     * Temporary bitset used to hold temporary value.
     * Optimized to compute elements only on active words.
     */
    public class CollectionBitSet extends BitSet {
        private long[] collection;

        public CollectionBitSet() {
            super(nWords);
        }

        @Override
        public void clear() {
            for (int i = nNonZero.value() - 1; i >= 0; i--) {
                collection[nonZeroIdx[i]] = 0L;
            }
        }

        @Override
        public void union(BitSet other) {
            for (int i = nNonZero.value() - 1; i >= 0; i--) {
                int idx = nonZeroIdx[i];
                collection[idx] |= other.words[idx];
            }
        }

        @Override
        public void intersect(BitSet other) {
            for (int i = nNonZero.value() - 1; i >= 0; i--) {
                int idx = nonZeroIdx[i];
                collection[idx] &= other.words[idx];
            }
        }
    }

    /**
     * Creates a StateSparseSet with n bits, initially all set (value 1 for all)
     *
     * @param sm the state manager
     * @param n  the number of bits
     */
    public StateSparseBitSet(StateManager sm, int n) {
        nWords = (n + 63) >>> 6; // divided by 64
        words = new State[nWords];
        mask = ~0L >>> (64 - (n % 64));
        Arrays.setAll(words, i ->
                i == nWords - 1 ?
                        sm.makeStateLong(mask) :
                        sm.makeStateLong(0xFFFFFFFFFFFFFFFFL)
        );
        nonZeroIdx = new int[nWords];
        Arrays.setAll(nonZeroIdx, i -> i);
        nNonZero = sm.makeStateInt(nWords);
        collection = new CollectionBitSet();
    }

    /**
     * Test is the reversibleSparseBitSet is empty
     *
     * @return true is empty, false otherwise
     */
    public boolean isEmpty() {
        return nNonZero.value() == 0;
    }

    /**
     * Remove the content of the BitSet from this sparseBitSet
     *
     * @param bs the BitSet to remove
     */
    public void remove(BitSet bs) {
        int size = nNonZero.value();
        for (int i = size - 1; i >= 0; i--) {
            int idx = nonZeroIdx[i];
            long remove = words[idx].value() & ~bs.words[idx];
            if (remove == 0L) {
                // deactivation of word
                size -= 1;
                nonZeroIdx[i] = nonZeroIdx[size];
                nonZeroIdx[size] = idx;
            } else {
                words[idx].setValue(remove);
            }
        }
        nNonZero.setValue(size);
    }

    /**
     * Remove the content of the collection from this sparseBitSet
     */
    public void removeCollected() {
        this.remove(collection);
    }

    /**
     * Intersect this sparseBitSet with bs
     *
     * @param bs the BitSet to intersect with
     */
    public void intersect(BitSet bs) {
        int size = nNonZero.value();
        for (int i = size - 1; i >= 0; i--) {
            int idx = nonZeroIdx[i];
            long intersect = words[idx].value() & bs.words[idx];
            if (intersect == 0L) {
                // deactivation of word
                size -= 1;
                nonZeroIdx[i] = nonZeroIdx[size];
                nonZeroIdx[size] = idx;
            } else {
                words[idx].setValue(intersect);
            }
        }
        nNonZero.setValue(size);
    }

    /**
     * Intersect this sparseBitSet with the collection
     */
    public void intersectCollected() {
        this.intersect(collection);
    }

    /**
     * Test the emptiness of the intersection with a given BitSet
     *
     * @param bs the BitSet to test the intersection with
     * @return true if empty, false otherwise
     */
    public boolean hasEmptyIntersection(BitSet bs) {
        for (int i = nNonZero.value() - 1; i >= 0; i--) {
            int idx = nonZeroIdx[i];
            if ((words[idx].value() & bs.words[idx]) != 0L) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test the emptiness of the intersection with the collection
     * true if empty, false otherwise
     *
     * @return
     */
    public boolean hasEmptyIntersectionCollected() {
        return this.hasEmptyIntersection(collection);
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < nNonZero.value(); i++) {
            res += " w" + nonZeroIdx[i] + "=" + Long.toBinaryString(words[nonZeroIdx[i]].value());
        }
        return res;
    }
}
