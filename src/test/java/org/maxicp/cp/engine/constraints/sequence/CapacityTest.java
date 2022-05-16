package org.maxicp.cp.engine.constraints.sequence;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.cp.engine.core.CPSequenceVarImpl;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.state.StateManager;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class CapacityTest extends CPSolverTest {

    CPSolver cp;
    StateManager sm;
    CPSequenceVar sequence;
    static int nNodes;
    static int begin;
    static int end;

    @BeforeClass
    public static void SetUpClass() {
        nNodes = 8;
        begin = 8;
        end = 9;
    }

    @Before
    public void SetUp() {
        cp = solverFactory.get();
        sm = cp.getStateManager();
        sequence = new CPSequenceVarImpl(cp, nNodes, begin, end);
    }

    @Test
    public void testInitCapacityInvalid() {
        int[] load = new int[] {9, 2, 4, 5, 3, 7, 6, 8};
        sequence.getSolver().getStateManager().saveState();
        try {
            cp.post(new Capacity(sequence, 1, 2, load));
            fail("allowed to init with initial capacity > max capacity");
        } catch (IllegalArgumentException ignored) {

        }
        sequence.getSolver().getStateManager().restoreState();

        sequence.getSolver().getStateManager().saveState();
        cp.post(new Capacity(sequence, 10, 10, load));
        assertEquals(0, sequence.nPossible());
        sequence.getSolver().getStateManager().restoreState();

        sequence.getSolver().getStateManager().saveState();
        cp.post(new Capacity(sequence, 1, 0, load));
        assertEquals(0, sequence.nPossible());
        sequence.getSolver().getStateManager().restoreState();

    }

    @Test
    public void testInitCapacityValid() {
        int[] load = new int[] {9, 2, 4, 5, 3, 7, 6, 8};

        sequence.getSolver().getStateManager().saveState();
        cp.post(new Capacity(sequence, 4, load));
        assertEquals(3, sequence.nPossible());
        assertTrue(sequence.isPossible(1));
        assertTrue(sequence.isPossible(2));
        assertTrue(sequence.isPossible(4));
        sequence.getSolver().getStateManager().restoreState();
    }

    @Test
    public void testRemoveNodes() {
        int[] load = new int[] {9, 2, 4, 5, 3, 7, 6, 8};

        cp.post(new Capacity(sequence, 10, load));
        assertEquals(8, sequence.nPossible());

        sequence.getSolver().getStateManager().saveState();
        sequence.insert(sequence.begin(), 3); // schedule node with capacity = 5
        cp.fixPoint();
        assertEquals(4, sequence.nExcluded()); // capa = 6, 7, 8, 9
        assertEquals(3, sequence.nPossible()); // 2, 3, 4
        assertEquals(1, sequence.nMember(false)); // 5
        assertTrue(sequence.isPossible(1)); //  capa = 2
        assertTrue(sequence.isPossible(4)); // capa = 3
        assertTrue(sequence.isPossible(2)); // ...
        assertTrue(sequence.isMember(3));
        assertTrue(sequence.isExcluded(6));
        assertTrue(sequence.isExcluded(5));
        assertTrue(sequence.isExcluded(7));
        assertTrue(sequence.isExcluded(0)); // capa = 9

        sequence.insert(sequence.begin(), 4); // schedule node with capacity = 3
        cp.fixPoint();
        assertEquals(5, sequence.nExcluded()); // capa = 6, 7, 8, 9, 4
        assertEquals(1, sequence.nPossible()); // 2
        assertEquals(2, sequence.nMember(false)); // 3, 5
        assertTrue(sequence.isPossible(1));
        assertTrue(sequence.isMember(4));
        assertTrue(sequence.isExcluded(2));
        assertTrue(sequence.isMember(3));
        assertTrue(sequence.isExcluded(6));
        assertTrue(sequence.isExcluded(5));
        assertTrue(sequence.isExcluded(7));
        assertTrue(sequence.isExcluded(0));
        sequence.getSolver().getStateManager().restoreState();

        sequence.insert(sequence.begin(), 0); // schedule node with capacity = 0
        cp.fixPoint();
        assertEquals(7, sequence.nExcluded());
        assertEquals(0, sequence.nPossible());
        assertEquals(1, sequence.nMember(false));

    }



}
