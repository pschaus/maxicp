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

import org.junit.Test;
import org.maxicp.state.StateManager;
import org.maxicp.state.StateManagerTest;
import org.maxicp.state.datastructures.BitSet;
import org.maxicp.state.datastructures.StateSparseBitSet;

import static org.junit.Assert.*;

public class StateSparseBitSetTest extends StateManagerTest {


    @Test
    public void testExample() {

        StateManager sm = stateFactory.get();
        StateSparseBitSet set = new StateSparseBitSet(sm, 256);

        StateSparseBitSet.SupportBitSet b1 = set.new SupportBitSet(); // [0..59] U [130..255]
        StateSparseBitSet.SupportBitSet b2 = set.new SupportBitSet(); // [60..129]
        StateSparseBitSet.SupportBitSet b3 = set.new SupportBitSet(); // empty

        for (int i = 0; i < 256; i++) {
            if (i < 60 || i >= 130) {
                b1.set(i);
            } else {
                b2.set(i);
            }
        }

        set.intersect(b1); // set is now [0..59] U [130..255]

        assertTrue(!set.hasEmptyIntersection(b1));
        assertTrue(set.hasEmptyIntersection(b2));

        sm.saveState();

        set.intersect(b3); // set is now empty

        assertTrue(set.hasEmptyIntersection(b1));
        assertTrue(set.hasEmptyIntersection(b2));
        assertTrue(set.hasEmptyIntersection(b3));

        sm.restoreState();  // set is now [0..59] U [130..255]

        assertTrue(!set.hasEmptyIntersection(b1));

        assertTrue(set.hasEmptyIntersection(b2));


    }


}