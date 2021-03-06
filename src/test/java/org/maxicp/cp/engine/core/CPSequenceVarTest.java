package org.maxicp.cp.engine.core;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.state.StateManager;
import org.maxicp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.*;



public class CPSequenceVarTest extends CPSolverTest {

    CPSolver cp;
    StateManager sm;
    CPSequenceVar sequence;
    static int[] insertions;
    static int nNodes;
    static int begin;
    static int end;

    public boolean[] propagateInsertArrCalled = new boolean[nNodes];
    public boolean[] propagateChangeArrCalled = new boolean[nNodes];
    public boolean[] propagateExcludeArrCalled = new boolean[nNodes];
    public boolean propagateBindCalled = false;
    public boolean propagateInsertCalled = false;
    public boolean propagateExcludeCalled = false;

    private void resetPropagatorsArrays() {
        for (int i = 0 ; i < nNodes; ++i) {
            propagateExcludeArrCalled[i] = false;
            propagateInsertArrCalled[i] = false;
            propagateChangeArrCalled[i] = false;
        }
    }

    private void resetPropagators() {
        propagateBindCalled = false;
        propagateExcludeCalled = false;
        propagateInsertCalled = false;
    }

    @BeforeClass
    public static void SetUpClass() {
        nNodes = 12;
        begin = 10;
        end = 11;
        insertions = IntStream.range(0, nNodes-1).toArray();
    }

    @Before
    public void SetUp() {
        cp = solverFactory.get();
        sm = cp.getStateManager();
        sequence = CPFactory.makeSequenceVar(cp, nNodes, begin, end);
        int a = 0;
    }

    private void assertIsBoolArrayTrueAt(boolean[] values, int... indexes) {
        Arrays.sort(indexes);
        int j = 0;
        int i = 0;
        for (; i < values.length && j < indexes.length; ++i) {
            if (i == indexes[j]) {
                assertTrue(values[i]);
                ++j;
            } else {
                assertFalse(values[i]);
            }
        }
        for (; i < values.length ; ++i) {
            assertFalse(values[i]);
        }
    }

    /**
     * train1 if a sequence corresponds to the expected arrays
     * @param scheduled scheduled nodes of the sequence. Ordered by appearance in the sequence, including begin and end node
     * @param possible possible nodes of the sequence
     * @param excluded excluded nodes of the sequence
     * @param scheduledInsert scheduled insertions of each InsertionVar. first indexing = id of the InsertionVar.
     *                        Must contain the beginning node if present
     * @param possibleInsert possible insertions of each InsertionVar. first indexing = id of the InsertionVar.
     *                       Must contain the beginning node if present
     */
    public static void isSequenceValid(CPSequenceVar sequence, int[] scheduled, int[] possible, int[] excluded,
                                       int[][] scheduledInsert, int[][] possibleInsert) {
        assertEquals(scheduled.length, sequence.nMember());
        assertEquals(possible.length, sequence.nPossible());
        assertEquals(excluded.length, sequence.nExcluded());
        assertEquals(sequence.begin(), sequence.nextMember(sequence.end()));
        assertEquals(sequence.end(), sequence.predMember(sequence.begin()));
        assertEquals(sequence.nNode(), scheduledInsert.length);
        // test the ordering
        int[] ordering = new int[scheduled.length];
        assertEquals(scheduled.length, sequence.fillOrder(ordering, true));
        for (int i = 0 ; i < ordering.length ; ++i) {
            assertEquals(scheduled[i], ordering[i]);
        }

        int[] insertions = IntStream.range(0, sequence.nNode()).toArray();
        int[] actual;
        int[] expected;
        int pred = Integer.MIN_VALUE;
        boolean foundPred = false;
        for (int i: scheduled) {
            assertTrue(sequence.isMember(i));
            assertFalse(sequence.isPossible(i));
            assertFalse(sequence.isExcluded(i));
            assertEquals(0, sequence.fillMemberInsertion(i, insertions));
            assertEquals(0, sequence.fillPossibleInsertion(i, insertions));
            if (foundPred) {
                assertEquals(i, sequence.nextMember(pred));
                assertEquals(pred, sequence.predMember(i));
            }
            foundPred = true;
            pred = i;
        }
        for (int i: possible) {
            assertFalse(sequence.isMember(i));
            assertTrue(sequence.isPossible(i));
            assertFalse(sequence.isExcluded(i));

            assertEquals(scheduledInsert[i].length, sequence.fillMemberInsertion(i, insertions));
            actual = Arrays.copyOfRange(insertions, 0, scheduledInsert[i].length);
            Arrays.sort(actual);
            expected = scheduledInsert[i];
            Arrays.sort(expected);
            assertArrayEquals(expected, actual);

            assertEquals(possibleInsert[i].length, sequence.fillPossibleInsertion(i, insertions));
            actual = Arrays.copyOfRange(insertions, 0, possibleInsert[i].length);
            Arrays.sort(actual);
            expected = possibleInsert[i];
            Arrays.sort(expected);
            assertArrayEquals(expected, actual);
        }
        for (int i: excluded) {
            assertFalse(sequence.isMember(i));
            assertFalse(sequence.isPossible(i));
            assertTrue(sequence.isExcluded(i));
        }

    }

    private void isSequenceValid(int[] scheduled, int[] possible, int[] excluded, int[][] scheduledInsertions, int[][] possibleInsertions) {
        isSequenceValid(sequence, scheduled, possible, excluded, scheduledInsertions, possibleInsertions);
    }

    /**
     * test if a sequence corresponds to the expected arrays
     * assume that no exclusion of a node for a particular InsertionVar has occurred
     * @param scheduled scheduled nodes of the sequence. Ordered by appearance in the sequence, including begin and end node
     * @param possible possible nodes of the sequence
     * @param excluded excluded nodes of the sequence
     */
    private void isSequenceValid(int[] scheduled, int[] possible, int[] excluded) {
        assertEquals(scheduled.length, sequence.nMember());
        assertEquals(possible.length, sequence.nPossible());
        assertEquals(excluded.length, sequence.nExcluded());
        assertEquals(sequence.begin(), sequence.nextMember(sequence.end()));
        assertEquals(sequence.end(), sequence.predMember(sequence.begin()));

        int[] sorted_scheduled = new int[scheduled.length - 1]; // used for scheduled insertions. includes begin node but not ending node
        for (int i = 0; i < sorted_scheduled.length; ++i)
            sorted_scheduled[i] = scheduled[i];
        Arrays.sort(sorted_scheduled);
        int[] sorted_possible = Arrays.copyOf(possible, possible.length);
        Arrays.sort(sorted_possible);
        int[] val;

        int nbScheduledInsertions = scheduled.length - 1; // number of scheduled nodes - end node
        int nbPossibleInsertions = possible.length - 1;       // number of possible nodes - node being tested

        int pred = Integer.MIN_VALUE;
        boolean hasPred = false;
        for (int i: scheduled) {
            assertTrue(sequence.isMember(i));
            assertFalse(sequence.isPossible(i));
            assertFalse(sequence.isExcluded(i));
            assertEquals(0, sequence.fillMemberInsertion(i, insertions));
            assertEquals(0, sequence.fillPossibleInsertion(i, insertions));
            if (hasPred) {
                assertEquals(i, sequence.nextMember(pred));
                assertEquals(pred, sequence.predMember(i));
            }
            pred = i;
            hasPred = true;
        }
        for (int i: possible) {
            assertFalse(sequence.isMember(i));
            assertTrue(sequence.isPossible(i));
            assertFalse(sequence.isExcluded(i));

            assertEquals(nbScheduledInsertions, sequence.fillMemberInsertion(i, insertions));
            val = Arrays.copyOfRange(insertions, 0, nbScheduledInsertions);
            Arrays.sort(val);
            assertArrayEquals(sorted_scheduled, val);

            assertEquals(nbPossibleInsertions, sequence.fillPossibleInsertion(i, insertions));
            val = Arrays.copyOfRange(insertions, 0, nbPossibleInsertions);
            Arrays.sort(val);
            assertArrayEquals(Arrays.stream(sorted_possible).filter(j -> j != i).toArray(), val);
        }
        for (int i: excluded) {
            assertFalse(sequence.isMember(i));
            assertFalse(sequence.isPossible(i));
            assertTrue(sequence.isExcluded(i));
        }
    }

    /**
     * train1 if the sequence is constructed with the right number of nodes and insertions
     */
    @Test
    public void testSequenceVar() {
        assertEquals(12, sequence.nNode());
        assertEquals(10, sequence.begin());
        assertEquals(11, sequence.end());
        assertEquals(sequence.begin(), sequence.nextMember(sequence.end()));
        assertEquals(sequence.end(), sequence.nextMember(sequence.begin()));
        assertEquals(sequence.begin(), sequence.predMember(sequence.end()));
        assertEquals(sequence.end(), sequence.predMember(sequence.begin()));
        assertEquals(2, sequence.nMember());
        assertEquals(10, sequence.nPossible());
        assertEquals(0, sequence.nExcluded());
        for (int i = 0; i < 10 ; ++i) {
            assertEquals(10, sequence.fillInsertion(i, insertions));
            boolean beginFound = false; // true if the begin node is considered as a predecessor
            for (int val: insertions) {
                assertNotEquals(val, i); // a node cannot have itself as predecessor
                beginFound = beginFound || val == 10;
            }
            assertTrue(beginFound);
        }
    }

    /**
     * train1 if the sequence is constructed correctly when begin and end are not 1 number apart
     */
    @Test
    public void testSequenceVarOffset() {
        int begin = 10;
        int end = 11;
        sequence = CPFactory.makeSequenceVar(cp, 12, begin, end);
        int[] scheduledInit = new int[] {begin, end};
        int[] possibleInit = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] excludedInit = new int[] {};
        isSequenceValid(scheduledInit, possibleInit, excludedInit);

    }

    /**
     * train1 for the scheduling of insertions within the sequence
     */
    @Test
    public void testSchedule() {
        sm.saveState();

        sequence.insert(sequence.begin(), 0);
        sequence.insert(0, 2);
        // sequence at this point: begin -> 0 -> 2 -> end
        int[] scheduled1 = new int[] {begin, 0, 2, end};
        int[] possible1 = new int[] {1, 3, 4, 5, 6, 7, 8, 9};
        int[] excluded1 = new int[] {};
        isSequenceValid(scheduled1, possible1, excluded1);

        sm.saveState();

        sequence.insert(sequence.begin(), 8);  // begin -> 8 -> 0 -> 2 -> end
        sequence.insert(2, 5);           // begin -> 8 -> 0 -> 2 -> 5 -> end
        int[] scheduled2 = new int[] {begin, 8, 0, 2, 5, end};
        int[] possible2 = new int[] {1, 3, 4, 6, 7, 9};
        int[] excluded2 = new int[] {};
        isSequenceValid(scheduled2, possible2, excluded2);

        sm.saveState();

        sequence.insert(8, 3);  // begin -> 8 -> 3 -> 0 -> 2 -> end
        sequence.insert(2, 7);  // begin -> 8 -> 3 -> 0 -> 2 -> 7 -> 5 -> end
        int[] scheduled3 = new int[] {begin, 8, 3, 0, 2, 7, 5, end};
        int[] possible3 = new int[] {1, 4, 6, 9};
        int[] excluded3 = new int[] {};
        isSequenceValid(scheduled3, possible3, excluded3);

        sm.saveState();

        sequence.insert(0, 4);  // begin -> 8 -> 3 -> 0 -> 4 -> 2 -> 7 -> 5 -> end
        sequence.insert(0, 9);  // begin -> 8 -> 3 -> 0 -> 9 -> 4 -> 2 -> 7 -> 5 -> end
        int[] scheduled4 = new int[] {begin, 8, 3, 0, 9, 4, 2, 7, 5, end};
        int[] possible4 = new int[] {1, 6};
        int[] excluded4 = new int[] {};
        isSequenceValid(scheduled4, possible4, excluded4);

        sm.saveState();

        sequence.insert(3, 1);  // begin -> 8 -> 3 -> 1 -> 0 -> 4 -> 2 -> 7 -> 5 -> end
        sequence.insert(5, 6);  // begin -> 8 -> 3 -> 0 -> 9 -> 4 -> 2 -> 7 -> 5 -> 6 -> end
        int[] scheduled5 = new int[] {begin, 8, 3, 1, 0, 9, 4, 2, 7, 5, 6, end};
        int[] possible5 = new int[] {};
        int[] excluded5 = new int[] {};
        isSequenceValid(scheduled5, possible5, excluded5);

        sm.restoreState();
        isSequenceValid(scheduled4, possible4, excluded4);
        sm.restoreState();
        isSequenceValid(scheduled3, possible3, excluded3);
        sm.restoreState();
        isSequenceValid(scheduled2, possible2, excluded2);
        sm.restoreState();
        isSequenceValid(scheduled1, possible1, excluded1);
        sm.restoreState();

        int[] scheduledInit = new int[] {begin, end};
        int[] possibleInit = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] excludedInit = new int[] {};
        isSequenceValid(scheduledInit, possibleInit, excludedInit);
    }

    /**
     * train1 for exclusion of nodes within the sequence
     */
    @Test
    public void testExclude() {
        sm.saveState();

        sequence.exclude(0);
        sequence.exclude(2);
        int[] scheduled1 = new int[] {begin, end};
        int[] possible1 = new int[] {1, 3, 4, 5, 6, 7, 8, 9};
        int[] excluded1 = new int[] {0, 2};
        isSequenceValid(scheduled1, possible1, excluded1);

        sm.saveState();

        sequence.exclude(5);
        sequence.exclude(7);
        sequence.exclude(9);
        sequence.exclude(3);
        int[] scheduled2 = new int[] {begin, end};
        int[] possible2 = new int[] {1, 4, 6, 8};
        int[] excluded2 = new int[] {0, 2, 5, 7, 9, 3};
        isSequenceValid(scheduled2, possible2, excluded2);

        sm.saveState();

        sequence.exclude(4);
        sequence.exclude(6);
        sequence.exclude(1);
        sequence.exclude(8);
        int[] scheduled3 = new int[] {begin, end};
        int[] possible3 = new int[] {};
        int[] excluded3 = new int[] {0, 2, 5, 7, 9, 3, 5, 6, 1, 8};
        isSequenceValid(scheduled3, possible3, excluded3);

        sm.restoreState();
        isSequenceValid(scheduled2, possible2, excluded2);
        sm.restoreState();
        isSequenceValid(scheduled1, possible1, excluded1);
        sm.restoreState();

        int[] scheduledInit = new int[] {begin, end};
        int[] possibleInit = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] excludedInit = new int[] {};
        isSequenceValid(scheduledInit, possibleInit, excludedInit);
    }

    /**
     * train1 for both exclusion and scheduling of insertions within the sequence
     */
    @Test
    public void testExcludeAndSchedule() {
        sm.saveState();
        sequence.insert(sequence.begin(), 4);
        sequence.exclude(5);
        sequence.exclude(6);

        int[] scheduled1 = new int[] {begin, 4, end};
        int[] possible1 = new int[] {0, 1, 2, 3, 7, 8, 9};
        int[] excluded1 = new int[] {5, 6};
        isSequenceValid(scheduled1, possible1, excluded1);
        sm.saveState();

        sequence.insert(4, 9);
        sequence.exclude(2);
        int[] scheduled2 = new int[] {begin, 4, 9, end};
        int[] possible2 = new int[] {0, 1, 3, 7, 8};
        int[] excluded2 = new int[] {2, 5, 6};
        isSequenceValid(scheduled2, possible2, excluded2);

        sm.saveState();

        sequence.exclude(1);
        sequence.insert(sequence.begin(), 7);
        int[] scheduled3 = new int[] {begin, 7, 4, 9, end};
        int[] possible3 = new int[] {0, 3, 8};
        int[] excluded3 = new int[] {1, 2, 5, 6};
        isSequenceValid(scheduled3, possible3, excluded3);

        sm.restoreState();
        isSequenceValid(scheduled2, possible2, excluded2);
        sm.restoreState();
        isSequenceValid(scheduled1, possible1, excluded1);
        sm.restoreState();
        int[] scheduledInit = new int[] {begin, end};
        int[] possibleInit = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] excludedInit = new int[] {};
        isSequenceValid(scheduledInit, possibleInit, excludedInit);
    }

    /**
     * train1 for removal of individual insertion candidates in an InsertionVar
     */
    @Test
    public void testIndividualInsertionRemoval() {
        sm.saveState();

        CPInsertionVar[] insertionVars = new CPInsertionVar[nNodes];
        for (int i=0; i< nNodes; ++i) {
            insertionVars[i] = sequence.getInsertionVar(i);
        }

        insertionVars[0].removeInsert(4);
        insertionVars[0].removeInsert(9);
        insertionVars[1].removeInsert(0);
        insertionVars[1].removeInsert(8);
        insertionVars[1].removeInsert(2);
        insertionVars[6].removeInsert(0);
        insertionVars[6].removeInsert(3);
        insertionVars[7].removeInsert(sequence.begin());
        insertionVars[7].removeInsert(5);
        insertionVars[7].removeInsert(6);
        int[] scheduled1 = new int[] {begin, end};
        int[] possible1 = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] excluded1 = new int[] {};
        int[][] scheduledInsertions1 = new int[][] {
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {},
                {sequence.begin()},
                {sequence.begin()},
                {},
                {}
        };
        int[][] possibleInsertions1 = new int[][] {
                {   1, 2, 3,    5, 6, 7, 8,  },
                {         3, 4, 5, 6, 7,    9},
                {0, 1,    3, 4, 5, 6, 7, 8, 9},
                {0, 1, 2,    4, 5, 6, 7, 8, 9},
                {0, 1, 2, 3,    5, 6, 7, 8, 9},
                {0, 1, 2, 3, 4,    6, 7, 8, 9},
                {   1, 2,    4, 5,    7, 8, 9},
                {0, 1, 2, 3, 4,          8, 9},
                {0, 1, 2, 3, 4, 5, 6, 7,    9},
                {0, 1, 2, 3, 4, 5, 6, 7, 8,  },
                {},
                {}
        };
        isSequenceValid(scheduled1, possible1, excluded1, scheduledInsertions1, possibleInsertions1);
        sm.saveState();

        sequence.insert(sequence.begin(), 4);
        insertionVars[2].removeInsert(3); // possible insert
        insertionVars[2].removeInsert(4); // scheduled insert
        insertionVars[2].removeInsert(6); // possible insert
        insertionVars[2].removeInsert(sequence.begin()); // scheduled insert
        insertionVars[8].removeInsert(4); // scheduled insert
        sequence.exclude(5);
        int[][] scheduledInsertions2 = new int[][] {
                {sequence.begin()},
                {sequence.begin(), 4},
                {},
                {sequence.begin(), 4},
                {sequence.begin(), 4},
                {sequence.begin(), 4},
                {sequence.begin(), 4},
                {4},
                {sequence.begin()},
                {sequence.begin(), 4},
                {},
                {}
        };
        int[][] possibleInsertions2 = new int[][] {
                {   1, 2, 3,       6, 7, 8,  },
                {         3,       6, 7,    9},
                {0, 1,                7, 8, 9},
                {0, 1, 2,          6, 7, 8, 9},
                {}, // sequenced node
                {0, 1, 2, 3,       6, 7, 8, 9},
                {   1, 2,             7, 8, 9},
                {0, 1, 2, 3,             8, 9},
                {0, 1, 2, 3,       6, 7,    9},
                {0, 1, 2, 3,       6, 7, 8,  },
                {},
                {}
        };
        int[] scheduled2 = new int[] {begin, 4, end};
        int[] possible2= new int[] {0, 1, 2, 3, 6, 7, 8, 9};
        int[] excluded2 = new int[] {5};
        isSequenceValid(scheduled2, possible2, excluded2, scheduledInsertions2, possibleInsertions2);

        sm.restoreState();
        isSequenceValid(scheduled1, possible1, excluded1, scheduledInsertions1, possibleInsertions1);
    }

    /**
     * train1 for calls to propagation within the InsertionVars contained in the sequence
     */
    @Test
    public void testPropagationInsertion() {
        resetPropagatorsArrays();

        CPInsertionVar[] insertionVars = new CPInsertionVar[nNodes];
        for (int i = 0; i < nNodes; ++i) {
            insertionVars[i] = sequence.getInsertionVar(i);
        }

        CPConstraint cons = new AbstractCPConstraint(cp) {
            @Override
            public void post() {
                for (int i = 0; i < nNodes; ++i) {
                    int finalI = i;
                    insertionVars[i].whenInsert(() -> propagateInsertArrCalled[finalI] = true);
                    insertionVars[i].whenDomainChange(() -> propagateChangeArrCalled[finalI] = true);
                    insertionVars[i].whenExclude(() -> propagateExcludeArrCalled[finalI] = true);
                }
            }
        };
        cp.post(cons);
        sequence.insert(sequence.begin(), 9); // sequence= begin -> 9 -> end
        cp.fixPoint();
        assertIsBoolArrayTrueAt(propagateInsertArrCalled, 9);
        assertIsBoolArrayTrueAt(propagateChangeArrCalled, 9);
        assertIsBoolArrayTrueAt(propagateExcludeArrCalled);
        resetPropagatorsArrays();

        sequence.exclude(5);
        cp.fixPoint();
        assertIsBoolArrayTrueAt(propagateInsertArrCalled);
        assertIsBoolArrayTrueAt(propagateChangeArrCalled, 0, 1, 2, 3, 4, 6, 7, 8); // node 5 and 9 don't have a change in their domain
        assertIsBoolArrayTrueAt(propagateExcludeArrCalled, 5);
        resetPropagatorsArrays();

        sequence.insert(sequence.begin(), 2); // sequence= begin -> 2 -> 9 -> end
        sequence.insert(sequence.begin(), 8); // sequence= begin -> 8 -> 2 -> 9 -> end
        cp.fixPoint();
        assertIsBoolArrayTrueAt(propagateInsertArrCalled, 2, 8);
        assertIsBoolArrayTrueAt(propagateChangeArrCalled, 2, 8);
        assertIsBoolArrayTrueAt(propagateExcludeArrCalled);
        resetPropagatorsArrays();

        sequence.exclude(3);
        cp.fixPoint();
        assertIsBoolArrayTrueAt(propagateInsertArrCalled);
        assertIsBoolArrayTrueAt(propagateChangeArrCalled, 0, 1, 4, 6, 7); // node 8, 2, 9, 5, 3 don't have a change in their domain
        assertIsBoolArrayTrueAt(propagateExcludeArrCalled, 3);
        resetPropagatorsArrays();
    }

    /**
     * train1 for calls to propagation within the sequence
     */
    @Test
    public void testPropagationSequence() {

        CPConstraint cons = new AbstractCPConstraint(cp) {
            @Override
            public void post() {
                sequence.whenFixed(() -> propagateBindCalled = true);
                sequence.whenInsert(() -> propagateInsertCalled = true);
                sequence.whenExclude(() -> propagateExcludeCalled = true);
            }
        };

        cp.post(cons);
        sequence.exclude(3);
        cp.fixPoint();
        assertTrue(propagateExcludeCalled);
        assertFalse(propagateBindCalled);
        assertFalse(propagateInsertCalled);
        resetPropagators();

        sequence.exclude(2);
        cp.fixPoint();
        assertTrue(propagateExcludeCalled);
        assertFalse(propagateBindCalled);
        assertFalse(propagateInsertCalled);
        resetPropagators();

        sequence.insert(sequence.begin(), 8); // sequence: begin -> 8 -> end
        cp.fixPoint();
        assertFalse(propagateExcludeCalled);
        assertFalse(propagateBindCalled);
        assertTrue(propagateInsertCalled);
        resetPropagators();

        sequence.insert(8, 1); // sequence: begin -> 8 -> 1 -> end
        cp.fixPoint();
        assertFalse(propagateExcludeCalled);
        assertFalse(propagateBindCalled);
        assertTrue(propagateInsertCalled);
        resetPropagators();

        sequence.exclude(0);
        sequence.exclude(4);
        sequence.exclude(5);
        sequence.exclude(7);
        sequence.exclude(9);
        cp.fixPoint();
        assertTrue(propagateExcludeCalled);
        assertFalse(propagateBindCalled);
        assertFalse(propagateInsertCalled);
        resetPropagators();

        // only node 6 is unassigned at the moment
        sm.saveState();
        sequence.exclude(6);
        cp.fixPoint();
        assertTrue(propagateExcludeCalled);
        assertTrue(propagateBindCalled);  // no possible node remain
        assertFalse(propagateInsertCalled);
        resetPropagators();

        sm.restoreState();
        sequence.insert(sequence.begin(), 6); // sequence: begin -> 6 -> 8 -> 1 -> end
        cp.fixPoint();
        assertFalse(propagateExcludeCalled);
        assertTrue(propagateBindCalled);  // no possible node remain
        assertTrue(propagateInsertCalled);
        resetPropagators();
    }

    @Test(expected = InconsistencyException.class)
    public void throwInconsistencyDoubleInsert() {
        sequence.insert(sequence.begin(), 4);
        sequence.insert(sequence.begin(), 8); // sequence at this point: begin -> 8 -> 4 -> end
        sequence.insert(4, 8);
    }

    @Test
    public void throwNoInconsistencyDoubleInsert() {
        sequence.insert(sequence.begin(), 8);
        sequence.insert(sequence.begin(), 8); // double insertions at the same point are valid
    }

    @Test
    public void throwNoInconsistencyDoubleExclude() {
        sequence.exclude(8);
        sequence.exclude(8);
    }

    @Test(expected = InconsistencyException.class)
    public void throwInconsistencyExcludeSchedule() {
        sequence.exclude(8);
        sequence.insert(sequence.begin(), 8);
    }

    @Test(expected = InconsistencyException.class)
    public void throwInconsistencyScheduleExclude() {
        sequence.insert(sequence.begin(), 8);
        sequence.exclude(8);
    }

    @Test(expected = InconsistencyException.class)
    public void throwAssertionSchedule() {
        sequence.insert(2, 8);
    }


    @Test(expected = InconsistencyException.class)
    public void testInvalidInsert() {
        sequence.removeInsertion(begin, 4);
        sequence.insert(begin, 4);
    }

    @Test
    public void excludeAll() {
        sequence.insert(begin, 3);
        sequence.insert(3, 9);
        sequence.insert(9, 6);
        sequence.excludeAllPossible();
        int[] members = new int[] {begin, 3, 9, 6, end};
        int[] possible = new int[] {};
        int[] excluded = new int[] {0, 1, 2, 4, 5, 7, 8};
        isSequenceValid(members, possible, excluded);
    }

    @Test
    public void testRemoveInsertionsAfter() {
        sequence.insert(begin, 3);
        sequence.insert(3, 9);
        sequence.insert(9, 6); // sequence at this point: begin -> 3 -> 9 -> 6 -> end
        sequence.removeInsertionAfter(9);
        sequence.removeInsertion(4, 8);
        sequence.removeInsertion(6, 7);
        sequence.removeInsertion(begin, 2);
        int[] members = new int[] {begin, 3, 9, 6, end};
        int[] possible = new int[] {0, 1, 2, 4, 5, 7, 8};
        int[] excluded = new int[] {};
        int[][] memberInsertions = new int[][] {
                {sequence.begin(), 3, 6},
                {sequence.begin(), 3, 6},
                {3, 6},
                {},
                {sequence.begin(), 3, 6},
                {sequence.begin(), 3, 6},
                {},
                {sequence.begin(), 3},
                {sequence.begin(), 3, 6},
                {},
                {},
                {},
        };
        int[][] possibleInsertions = new int[][] {
                {   1, 2,    4, 5,    7, 8,  },
                {0,    2,    4, 5,    7, 8,  },
                {0, 1,       4, 5,    7, 8,  },
                {},
                {0, 1, 2,       5,    7, 8,  },
                {0, 1, 2,    4,       7, 8,  },
                {},
                {0, 1, 2,    4, 5,       8,  },
                {0, 1, 2,       5,    7,     },
                {},
                {},
                {}
        };
        isSequenceValid(sequence, members, possible, excluded, memberInsertions, possibleInsertions);
    }

}
