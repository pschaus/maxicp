package org.maxicp.state.datastructures;


/**
 * Bitset abstract class
 */
public abstract class BitSet {
    protected long[] words;

    /**
     * Initializes a bit-set with the same capacity as the outer {@link StateSparseBitSet}.
     * All the bits are initially unset. The set it represents is thus empty.
     */
    public BitSet(int nWords) {
        words = new long[nWords];
    }

    public BitSet(BitSet anotherBitSet){
        words = anotherBitSet.words;
    }

    /**
     * Set the ith bit
     *
     * @param i the bit to set
     */
    public void set(int i) {
        words[i >>> 6] |= 1L << i; // << is a cyclic shift, (1L << 64) == 1L
    }


}
