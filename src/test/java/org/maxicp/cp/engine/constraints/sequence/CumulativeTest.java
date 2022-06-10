package org.maxicp.cp.engine.constraints.sequence;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.cp.engine.core.CPSequenceVarImpl;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.state.StateManager;
import org.maxicp.util.exception.InconsistencyException;

import static org.junit.Assert.*;

public class CumulativeTest extends CPSolverTest {

    CPSolver cp;
    StateManager sm;
    CPSequenceVar sequence;
    static int nNodes;
    static int begin;
    static int end;

    @BeforeClass
    public static void SetUpClass() {
        nNodes = 10;
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
    public void testInitCumulative2() {
        int[] capacity = new int[] {1, 1, -1, -1, 0, 0, 0, 0, 0, 0};
        try {
            cp.post(new Cumulative(sequence, new int[] {0, 1}, new int[] {2, 3}, 2, capacity));
        } catch (InconsistencyException e) {
            fail("should not fail");
        }
    }

    @Test
    public void testFeasibleSequence() {
        int[] capacity = new int[] {2, 2, 1, 1, -2, -2, -1, -1, 0, 0};
        cp.post(new Cumulative(sequence, new int[] {0, 1, 2, 3}, new int[] {4, 5, 6, 7}, 3, capacity));
        cp.post(new Insert(sequence, sequence.begin(), 2));
        cp.post(new Insert(sequence, 2, 4));
        cp.post(new Insert(sequence, 4, 3));
        cp.post(new Insert(sequence, 3, 1));
        cp.post(new Insert(sequence, 1, 7));
        cp.post(new Insert(sequence, 7, 5));
    }

    @Test
    public void testNoInsertForStart() {
        int[] capacity = new int[] {2, 2, 1, 1, -2, -2, -1, -1, 0, 0};
        int[] p = new int[] {0, 1, 2, 3};
        int[] d = new int[] {4, 5, 6, 7};
        cp.post(new Cumulative(sequence, p, d, 3, capacity));
        cp.post(new Insert(sequence, sequence.begin(), p[0]));
        cp.post(new Insert(sequence, p[0], p[2]));
        cp.post(new Insert(sequence, p[2], d[2]));
        cp.post(new Insert(sequence, d[2], d[1]));
        // sequence at this point: begin -> p0   -> p2   -> d2   -> d1   -> end
        // capacity:                0, 0    0, 2    0, 1    1, 0    2, 0    0, 0
        assertTrue(sequence.isInsertion(p[0], p[1]));
        assertTrue(sequence.isInsertion(p[0], p[3]));
        assertTrue(sequence.isInsertion(p[0], d[3]));
    }

    /**
     * train1 if an activity with no inserted part has some insertions points removed
     */
    @Test
    public void testNotInsertedActivity() {
        int[] capacity = new int[] {2, 2, 1, 1, -2, -2, -1, -1, 0, 0};
        cp.post(new Cumulative(sequence, new int[] {0, 1, 2, 3}, new int[] {4, 5, 6, 7}, 3, capacity));
        cp.post(new Insert(sequence, sequence.begin(), 2));
        cp.post(new Insert(sequence, 2, 0));
        cp.post(new Insert(sequence, 0, 4));
        cp.post(new Insert(sequence, 4, 6)); // sequence: begin -> 2 -> 0 -> 4 -> 6 -> end
        int start = 1;
        int end = 5;
        assertTrue(sequence.isInsertion(sequence.begin(), start));
        assertTrue(sequence.isInsertion(2, start));
        assertFalse(sequence.isInsertion(0, start));
        assertTrue(sequence.isInsertion(4, start));
        assertTrue(sequence.isInsertion(6, start));

        assertTrue(sequence.isInsertion(sequence.begin(), end));
        assertTrue(sequence.isInsertion(2, end));
        assertFalse(sequence.isInsertion(0, end));
        assertTrue(sequence.isInsertion(4, end));
        assertTrue(sequence.isInsertion(6, end));
    }

    @Test
    public void testUpdateEnd() {
        int[] capacity = new int[] {2, 2, 1, 1, -2, -2, -1, -1, 0, 0};
        cp.post(new Cumulative(sequence, new int[] {0, 1, 2, 3}, new int[] {4, 5, 6, 7}, 3, capacity));
        cp.post(new Insert(sequence, sequence.begin(), 1));
        cp.post(new Insert(sequence, 1, 2));
        cp.post(new Insert(sequence, 2, 0));
        cp.post(new Insert(sequence, 0, 4));
        cp.post(new Insert(sequence, 4, 6)); // sequence: begin -> 1 -> 2 -> 0 -> 4 -> 6 -> end

        int end = 5; // end for start == 1
        assertFalse(sequence.isInsertion(sequence.begin(), end)); // cannot schedule end before start
        assertTrue(sequence.isInsertion(1, end));
        assertTrue(sequence.isInsertion(2, end));
        assertFalse(sequence.isInsertion(0, end)); // from this point, the end exceeds the max capacity
        assertFalse(sequence.isInsertion(4, end));
        assertFalse(sequence.isInsertion(6, end));
    }

    @Test
    public void testRemoveIntermediate() {
        int[] capacity = new int[] {2, 2, 1, 1, -2, -2, -1, -1, 0, 0};
        cp.post(new Cumulative(sequence, new int[] {0, 1, 2, 3}, new int[] {4, 5, 6, 7}, 3, capacity));
        cp.post(new Insert(sequence, sequence.begin(), 2));
        cp.post(new Insert(sequence, 2, 0));
        cp.post(new Insert(sequence, 0, 4));
        cp.post(new Insert(sequence, 4, 3));
        cp.post(new Insert(sequence, 3, 7));
        cp.post(new Insert(sequence, 7, 6)); // sequence: begin -> 2 -> 0 -> 4 -> 3 -> 7 -> 6 -> end

        int start = 1;
        int end = 5;
        assertTrue(sequence.isInsertion(sequence.begin(), start));
        assertTrue(sequence.isInsertion(2, start));  // capacity: 1
        assertFalse(sequence.isInsertion(0, start)); // capacity: 3
        assertTrue(sequence.isInsertion(4, start));  // capacity: 1
        assertFalse(sequence.isInsertion(3, start)); // capacity: 2
        assertTrue(sequence.isInsertion(7, start));  // capacity: 1
        assertTrue(sequence.isInsertion(6, start));  // capacity: 0

        assertTrue(sequence.isInsertion(sequence.begin(), end));
        assertTrue(sequence.isInsertion(2, end));    // capacity: 1
        assertFalse(sequence.isInsertion(0, end));   // capacity: 3
        assertTrue(sequence.isInsertion(4, end));    // capacity: 1
        assertFalse(sequence.isInsertion(3, end));   // capacity: 2
        assertTrue(sequence.isInsertion(7, end));    // capacity: 1
        assertTrue(sequence.isInsertion(6, end));    // capacity: 0

        try {
            cp.post(new Insert(sequence, 3, 1));
            fail();
        } catch (InconsistencyException e) {
            ;
        }
    }

    @Test
    public void testPartiallyInsert() {
        int[] capacity = new int[] {2, 2, 1, 1, -2, -2, -1, -1, 0, 0};
        cp.post(new Cumulative(sequence, new int[] {0, 1, 2, 3}, new int[] {4, 5, 6, 7}, 3, capacity));
        cp.post(new Insert(sequence, sequence.begin(), 2));
        cp.post(new Insert(sequence, 2, 3));
        cp.post(new Insert(sequence, 2, 4));
        cp.post(new Insert(sequence, 2, 5));
        cp.post(new Insert(sequence, 2, 6));
        cp.post(new Insert(sequence, 3, 7)); // sequence: begin -> 2 -> 6 -> 5 -> 4 -> 3 -> 7 -> end
    }

    @Test
    public void removeDropInsertion() {
        int[] capacity = new int[] {1, 1, 1, 1, -1, -1, -1, -1, 0, 0};
        int[] p = new int[] {0, 1, 2, 3};
        int[] d = new int[] {4, 5, 6, 7};
        Cumulative Cumulative = new Cumulative(sequence, p, d, 2, capacity);
        cp.post(Cumulative);
        cp.post(new Insert(sequence, sequence.begin(), p[0]));
        cp.post(new Insert(sequence, p[0], p[1]));
        cp.post(new Insert(sequence, p[1], p[2]));
        cp.post(new Insert(sequence, p[2], d[0]));
        // sequence:   begin -> 0 (p0) -> 1 (p1) -> 2 (p2) -> 4 (d0) -> end
        // capacity:   0, 0     0, 1      1, 2      1, 2      1, 0
        // partially inserted
        assertFalse(sequence.isInsertion(p[2], d[1]));
        assertTrue(sequence.isInsertion(p[1], d[1]));

        // not inserted: pickup
        assertTrue(sequence.isInsertion(sequence.begin(), p[3]));
        assertTrue(sequence.isInsertion(p[0], p[3]));
        assertTrue(sequence.isInsertion(p[1], p[3]));
        assertTrue(sequence.isInsertion(p[2], p[3]));
        assertTrue(sequence.isInsertion(d[0], p[3]));

        // not inserted: drop
        assertTrue(sequence.isInsertion(sequence.begin(), d[3]));
        assertTrue(sequence.isInsertion(p[0], d[3]));
        assertTrue(sequence.isInsertion(p[1], d[3]));
        assertTrue(sequence.isInsertion(p[2], d[3]));
        assertTrue(sequence.isInsertion(d[0], d[3]));
    }


    @Test
    public void removeNotInserted() {
        int[] capacity = new int[] {2, 2, 1, 1, -2, -2, -1, -1, 0, 0};
        int[] p = new int[] {0, 1, 2, 3};
        int[] d = new int[] {4, 5, 6, 7};
        Cumulative Cumulative = new Cumulative(sequence, p, d, 3, capacity);
        cp.post(Cumulative);
        cp.post(new Insert(sequence, sequence.begin(), p[0]));
        cp.post(new Insert(sequence, p[0], p[2]));
        cp.post(new Insert(sequence, p[2], d[2]));
        cp.post(new Insert(sequence, d[2], d[0]));
        // sequence:   begin -> (p0) -> (p2) -> 2 (d2) -> 4 (d0) -> end
        // sequence: capacity:   2       3         2         0
        // not inserted: drop
        assertTrue(sequence.isInsertion(sequence.begin(), d[1]));
        assertFalse(sequence.isInsertion(p[0], d[1]));
        assertFalse(sequence.isInsertion(p[2], d[1]));
        assertFalse(sequence.isInsertion(d[2], d[1]));
        assertTrue(sequence.isInsertion(d[0], d[1]));
        // not inserted: pickup
        assertTrue(sequence.isInsertion(sequence.begin(), p[1]));
        assertFalse(sequence.isInsertion(p[0], p[1]));
        assertFalse(sequence.isInsertion(p[2], p[1]));
        assertFalse(sequence.isInsertion(d[2], p[1]));
        assertTrue(sequence.isInsertion(d[0], p[1]));
    }

    @Test
    public void SeveralPartiallyInserted() {
        int[] capacity = new int[] {2, 2, 1, 1, -2, -2, -1, -1, 0, 0};
        int[] p = new int[] {0, 1, 2, 3};
        int[] d = new int[] {4, 5, 6, 7};
        cp.post(new Cumulative(sequence, p, d, 3, capacity));
        cp.post(new Insert(sequence, sequence.begin(), p[2]));
        cp.post(new Insert(sequence, p[2], p[3]));
        cp.post(new Insert(sequence, p[2], d[0]));
        cp.post(new Insert(sequence, p[3], d[1]));
        cp.post(new Insert(sequence, d[1], d[2])); // begin -> p2 -> d0 -> p3 -> d1 -> d2 -> end
        cp.post(new Insert(sequence, p[2], p[0])); // begin -> p2 -> p0 -> d0 -> p3 -> d1 -> d2 -> end
        int a = 0;
    }

    @Test
    public void MultipleDropPartiallyInserted() {
        int[] capacity = new int[] {1, 1, 1, 1, -1, -1, -1, -1, 0, 0};
        int[] p = new int[] {0, 1, 2, 3};
        int[] d = new int[] {4, 5, 6, 7};
        cp.post(new Cumulative(sequence, p, d, 1, capacity));
        cp.post(new Insert(sequence, sequence.begin(), d[0]));
        cp.post(new Insert(sequence, d[0], d[1]));
        cp.post(new Insert(sequence, d[1], d[2]));
        cp.post(new Insert(sequence, d[2], d[3])); // begin -> d0 -> d1 -> d2 -> d3 -> end
        assertFalse(sequence.isInsertion(sequence.begin(), p[1]));
        assertFalse(sequence.isInsertion(sequence.begin(), p[2]));
        assertFalse(sequence.isInsertion(d[0], p[2]));
        assertFalse(sequence.isInsertion(sequence.begin(), p[3]));
        assertFalse(sequence.isInsertion(d[0], p[3]));
        assertFalse(sequence.isInsertion(d[1], p[3]));
    }

    @Test
    public void MultiplePickupPartiallyInserted() {
        int[] capacity = new int[] {1, 1, 1, 1, -1, -1, -1, -1, 0, 0};
        int[] p = new int[] {0, 1, 2, 3};
        int[] d = new int[] {4, 5, 6, 7};
        cp.post(new Cumulative(sequence, p, d, 1, capacity));
        cp.post(new Insert(sequence, sequence.begin(), p[0]));
        cp.post(new Insert(sequence, p[0], p[1]));
        cp.post(new Insert(sequence, p[1], p[2]));
        cp.post(new Insert(sequence, p[2], p[3])); // begin -> p0 -> p1 -> p2 -> p3 -> end
        assertFalse(sequence.isInsertion(p[1], d[0]));
        assertFalse(sequence.isInsertion(p[2], d[1]));
        assertFalse(sequence.isInsertion(p[3], d[2]));
        assertTrue(sequence.isInsertion(p[0], d[0]));
        assertTrue(sequence.isInsertion(p[1], d[1]));
        assertTrue(sequence.isInsertion(p[2], d[2]));
        assertTrue(sequence.isInsertion(p[3], d[3]));
    }

    @Test
    public void MultiplePartiallyInserted1() {
        int[] capacity = new int[] {1, 1, 1, 1, -1, -1, -1, -1, 0, 0};
        int[] p = new int[] {0, 1, 2, 3};
        int[] d = new int[] {4, 5, 6, 7};
        cp.post(new Cumulative(sequence, p, d, 1, capacity));
        cp.post(new Insert(sequence, sequence.begin(), d[0]));
        cp.post(new Insert(sequence, d[0], p[1]));
        cp.post(new Insert(sequence, p[1], d[2]));
        cp.post(new Insert(sequence, d[2], p[3])); // begin -> d0 -> p1 -> d2 -> p3 -> end

        // insertions for p0
        assertTrue(sequence.isInsertion(sequence.begin(), p[0]));
        assertFalse(sequence.isInsertion(d[0], p[0]));
        assertFalse(sequence.isInsertion(p[1], p[0]));
        assertFalse(sequence.isInsertion(d[2], p[0]));
        assertFalse(sequence.isInsertion(p[3], p[0]));

        // insertions for d1
        assertFalse(sequence.isInsertion(sequence.begin(), d[1]));
        assertFalse(sequence.isInsertion(d[0], d[1]));
        assertTrue(sequence.isInsertion(p[1], d[1]));
        assertFalse(sequence.isInsertion(d[2], d[1]));
        assertFalse(sequence.isInsertion(p[3], d[1]));

        // insertions for p2
        assertFalse(sequence.isInsertion(sequence.begin(), p[2]));
        assertFalse(sequence.isInsertion(d[0], p[2]));
        assertTrue(sequence.isInsertion(p[1], p[2]));
        assertFalse(sequence.isInsertion(d[2], p[2]));
        assertFalse(sequence.isInsertion(p[3], p[2]));

        // insertions for d3
        assertFalse(sequence.isInsertion(sequence.begin(), d[3]));
        assertFalse(sequence.isInsertion(d[0], d[3]));
        assertFalse(sequence.isInsertion(p[1], d[3]));
        assertFalse(sequence.isInsertion(d[2], d[3]));
        assertTrue(sequence.isInsertion(p[3], d[3]));
    }

}
