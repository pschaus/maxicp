package org.maxicp.cp.engine.constraints.sequence;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.cp.engine.core.CPSequenceVarImpl;
import org.maxicp.cp.engine.core.CPSequenceVarTest;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.state.StateManager;
import org.maxicp.util.exception.InconsistencyException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PrecedenceTest extends CPSolverTest {

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
    public void testInitPrecedence() {
        try {
            cp.post(new Precedence(sequence, 0, 1, 2));
        } catch (InconsistencyException e) {
            fail("should not fail");
        }
    }

    @Test
    public void testRemovePredecessors() {
        cp.post(new Precedence(sequence, 0, 2, 4, 7));
        int[][] scheduledInsertions1 = new int[][] {
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
        };
        int[][] possibleInsertions1 = new int[][] {
                {   1,    3,    5, 6,  }, // 0 cannot have 2, 4 nor 7 as predecessor
                {0,    2, 3, 4, 5, 6, 7},
                {0, 1,    3,    5, 6,  }, // 2 cannot have 4 nor 7 as predecessor
                {0, 1, 2,    4, 5, 6, 7},
                {0, 1, 2, 3,    5, 6,  }, // 4 cannot have 7 as predecessor
                {0, 1, 2, 3, 4,    6, 7},
                {0, 1, 2, 3, 4, 5,    7},
                {0, 1, 2, 3, 4, 5, 6   },
        };
        int[] scheduled1 = new int[] {begin, end};
        int[] possible1 = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
        int[] excluded1 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled1, possible1, excluded1, scheduledInsertions1, possibleInsertions1);

        sequence.insert(sequence.begin(), 2);
        cp.fixPoint();
        int[][] scheduledInsertions2 = new int[][] {
                {sequence.begin()}, // 2 cannot be a predecessor
                {sequence.begin(), 2},
                {}, // 2 is scheduled
                {sequence.begin(), 2},
                {2}, // begin node cannot be a predecessor anymore
                {sequence.begin(), 2},
                {sequence.begin(), 2},
                {2}, // begin node cannot be a predecessor anymore
        };
        int[][] possibleInsertions2 = new int[][] {
                {   1,    3,    5, 6,  }, // 0 cannot have 2, 4 nor 7 as predecessor
                {0,       3, 4, 5, 6, 7},
                {}, // scheduled node
                {0, 1,       4, 5, 6, 7},
                {0, 1,    3,    5, 6,  },
                {0, 1,    3, 4,    6, 7},
                {0, 1,    3, 4, 5,    7},
                {0, 1,    3, 4, 5, 6   },
        };
        int[] scheduled2 = new int[] {begin, 2, end};
        int[] possible2 = new int[] {0, 1, 3, 4, 5, 6, 7};
        int[] excluded2 = new int[] {};

        CPSequenceVarTest.isSequenceValid(sequence, scheduled2, possible2, excluded2, scheduledInsertions2, possibleInsertions2);
    }

    @Test
    public void testExcludeMustAppear() {
        cp.post(new Precedence(sequence, true, 0, 2, 4));
        for (int i: new int[] {0, 2, 4}) {
            try {
                sequence.exclude(i);
                cp.fixPoint();
                fail("excluding a node belonging to order should throw an inconsistency");
            } catch (InconsistencyException e) {}
        }
    }

    @Test
    public void testExcludeMustNotAppear() {
        cp.post(new Precedence(sequence, false, 0, 2, 4));
        sm.saveState();
        sequence.exclude(0);
        cp.fixPoint();
        assertTrue(sequence.isExcluded(2));
        assertTrue(sequence.isExcluded(4));
        sm.restoreState();
        sm.saveState();
        sequence.exclude(2);
        cp.fixPoint();
        assertTrue(sequence.isExcluded(0));
        assertTrue(sequence.isExcluded(4));
        sm.restoreState();
        sequence.exclude(4);
        cp.fixPoint();
        assertTrue(sequence.isExcluded(0));
        assertTrue(sequence.isExcluded(2));
    }

    @Test
    public void testInsertionReverseOrder() {
        // insert the nodes in reverse order
        cp.post(new Precedence(sequence, 0, 2, 4, 7));
        int[][] scheduledInsertions1 = new int[][] {
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
                {sequence.begin()},
        };
        int[][] possibleInsertions1 = new int[][] {
                {   1,    3,    5, 6,  }, // 0 cannot have 2, 4 nor 7 as predecessor
                {0,    2, 3, 4, 5, 6, 7},
                {0, 1,    3,    5, 6,  }, // 2 cannot have 4 nor 7 as predecessor
                {0, 1, 2,    4, 5, 6, 7},
                {0, 1, 2, 3,    5, 6,  }, // 4 cannot have 7 as predecessor
                {0, 1, 2, 3, 4,    6, 7},
                {0, 1, 2, 3, 4, 5,    7},
                {0, 1, 2, 3, 4, 5, 6   },
        };
        int[] scheduled1 = new int[] {begin, end};
        int[] possible1 = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
        int[] excluded1 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled1, possible1, excluded1, scheduledInsertions1, possibleInsertions1);

        sequence.insert(sequence.begin(), 7);
        cp.fixPoint();
        int[][] scheduledInsertions2 = new int[][] {
                {sequence.begin()},
                {sequence.begin(), 7},
                {sequence.begin()},
                {sequence.begin(), 7},
                {sequence.begin()},
                {sequence.begin(), 7},
                {sequence.begin(), 7},
                {}, // scheduled
        };
        int[][] possibleInsertions2 = new int[][] {
                {   1,    3,    5, 6,  }, // 0 cannot have 2, 4 nor 7 as predecessor
                {0,    2, 3, 4, 5, 6,  },
                {0, 1,    3,    5, 6,  }, // 2 cannot have 4 nor 7 as predecessor
                {0, 1, 2,    4, 5, 6,  },
                {0, 1, 2, 3,    5, 6,  }, // 4 cannot have 7 as predecessor
                {0, 1, 2, 3, 4,    6,  },
                {0, 1, 2, 3, 4, 5,     },
                {}, // scheduled
        };
        int[] scheduled2 = new int[] {begin, 7, end};
        int[] possible2 = new int[] {0, 1, 2, 3, 4, 5, 6};
        int[] excluded2 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled2, possible2, excluded2, scheduledInsertions2, possibleInsertions2);

        sequence.insert(sequence.begin(), 4);
        cp.fixPoint();
        int[][] scheduledInsertions3 = new int[][] {
                {sequence.begin()},
                {sequence.begin(), 7, 4},
                {sequence.begin()},
                {sequence.begin(), 7, 4},
                {}, // scheduled
                {sequence.begin(), 7, 4},
                {sequence.begin(), 7, 4},
                {}, // scheduled
        };
        int[][] possibleInsertions3 = new int[][] {
                {   1,    3,    5, 6,  }, // 0 cannot have 2, 4 nor 7 as predecessor
                {0,    2, 3,    5, 6,  },
                {0, 1,    3,    5, 6,  }, // 2 cannot have 4 nor 7 as predecessor
                {0, 1, 2,       5, 6,  },
                {}, // scheduled
                {0, 1, 2, 3,       6,  },
                {0, 1, 2, 3,    5,     },
                {}, // scheduled
        };
        int[] scheduled3 = new int[] {begin, 4, 7, end};
        int[] possible3 = new int[] {0, 1, 2, 3, 5, 6};
        int[] excluded3 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled3, possible3, excluded3, scheduledInsertions3, possibleInsertions3);

        sequence.insert(sequence.begin(), 2);
        cp.fixPoint();
        int[][] scheduledInsertions4 = new int[][] {
                {sequence.begin()},
                {sequence.begin(), 7, 4, 2},
                {}, // scheduled
                {sequence.begin(), 7, 4, 2},
                {}, // scheduled
                {sequence.begin(), 7, 4, 2},
                {sequence.begin(), 7, 4, 2},
                {}, // scheduled
        };
        int[][] possibleInsertions4 = new int[][] {
                {   1,    3,    5, 6,  }, // 0 cannot have 2, 4 nor 7 as predecessor
                {0,       3,    5, 6,  },
                {}, // 2 cannot have 4 nor 7 as predecessor
                {0, 1,          5, 6,  },
                {}, // scheduled
                {0, 1,    3,       6,  },
                {0, 1,    3,    5,     },
                {}, // scheduled
        };
        int[] scheduled4 = new int[] {begin, 2, 4, 7, end};
        int[] possible4 = new int[] {0, 1, 3, 5, 6};
        int[] excluded4 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled4, possible4, excluded4, scheduledInsertions4, possibleInsertions4);

        sequence.insert(sequence.begin(), 0);
        cp.fixPoint();
        int[][] scheduledInsertions5 = new int[][] {
                {}, // scheduled
                {sequence.begin(), 7, 4, 2, 0},
                {}, // scheduled
                {sequence.begin(), 7, 4, 2, 0},
                {}, // scheduled
                {sequence.begin(), 7, 4, 2, 0},
                {sequence.begin(), 7, 4, 2, 0},
                {}, // scheduled
        };
        int[][] possibleInsertions5 = new int[][] {
                {}, // scheduled
                {         3,    5, 6,  },
                {}, // scheduled
                {   1,          5, 6,  },
                {}, // scheduled
                {   1,    3,       6,  },
                {   1,    3,    5,     },
                {}, // scheduled
        };
        int[] scheduled5 = new int[] {begin, 0, 2, 4, 7, end};
        int[] possible5 = new int[] {1, 3, 5, 6};
        int[] excluded5 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled5, possible5, excluded5, scheduledInsertions5, possibleInsertions5);
    }

    /**
     * train1 if inserting a node not in the order array within the sequence changes correctly the insertions points
     */
    @Test
    public void removeIntermediateInsertions1() {
        cp.post(new Precedence(sequence, 0, 2, 4, 7));

        sequence.insert(sequence.begin(), 2);
        sequence.insert(sequence.begin(), 5); // sequence: begin - 5 - 2 - end
        cp.fixPoint();

        int[][] scheduledInsertions1 = new int[][] {
                {sequence.begin(), 5},
                {sequence.begin(), 5, 2},
                {}, // 2 is scheduled
                {sequence.begin(), 5, 2},
                {2}, // begin cannot be scheduled anymore for node 4
                {}, // 5 is scheduled
                {sequence.begin(), 5, 2},
                {2}, // begin cannot be scheduled anymore for node 7
        };
        int[][] possibleInsertions1 = new int[][] {
                {   1,    3,       6,  },   // 0 cannot have 2, 4 nor 7 as predecessor
                {0,       3, 4,    6, 7},
                {},                         // 2 is scheduled
                {0, 1,       4,    6, 7},
                {0, 1,    3,       6,  },   // 4 cannot have 7 as predecessor
                {},                         // 5 is scheduled
                {0, 1,    3, 4,       7},
                {0, 1,    3, 4,    6   },   // 7 cannot have 0 as predecessor
        };
        int[] scheduled1 = new int[] {begin, 5, 2, end};
        int[] possible1 = new int[] {0, 1, 3, 4, 6, 7};
        int[] excluded1 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled1, possible1, excluded1, scheduledInsertions1, possibleInsertions1);

        sequence.insert(2, 3); // sequence: begin - 5 - 2 - 3 - end
        cp.fixPoint();

        int[][] scheduledInsertions2 = new int[][] {
                {sequence.begin(), 5},
                {sequence.begin(), 5, 2, 3},
                {}, // 2 is scheduled
                {}, // 3 is scheduled
                {2, 3}, // only 2 and 3 are valid insertions points for node 4
                {}, // 5 is scheduled
                {sequence.begin(), 5, 2, 3},
                {2, 3},
        };
        int[][] possibleInsertions2 = new int[][] {
                {   1,             6,  },   // 0 cannot have 2, 4 nor 7 as predecessor
                {0,          4,    6, 7},
                {},                         // 2 is scheduled
                {},                         // 3 is scheduled
                {0, 1,             6,  },   // 4 cannot have 7 as predecessor
                {},                         // 5 is scheduled
                {0, 1,       4,       7},
                {0, 1,       4,    6,  },
        };
        int[] scheduled2 = new int[] {begin, 5, 2, 3, end};
        int[] possible2 = new int[] {0, 1, 4, 6, 7};
        int[] excluded2 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled2, possible2, excluded2, scheduledInsertions2, possibleInsertions2);

        sequence.insert(3, 7); // sequence: begin - 5 - 2 - 3 - 7 end
        cp.fixPoint();

        int[][] scheduledInsertions3 = new int[][] {
                {sequence.begin(), 5},
                {sequence.begin(), 5, 2, 3, 7},
                {}, // 2 is scheduled
                {}, // 3 is scheduled
                {2, 3}, // only 2 and 3 are valid insertions points for node 4
                {}, // 5 is scheduled
                {sequence.begin(), 5, 2, 3, 7},
                {}, // 7 is scheduled
        };
        int[][] possibleInsertions3 = new int[][] {
                {   1,             6,  },   // 0 cannot have 2, 4 nor 7 as predecessor
                {0,          4,    6,  },
                {},                         // 2 is scheduled
                {},                         // 3 is scheduled
                {0, 1,             6,  },   // 4 cannot have 7 as predecessor
                {},                         // 5 is scheduled
                {0, 1,       4,        },
                {},                         // 7 is scheduled
        };
        int[] scheduled3 = new int[] {begin, 5, 2, 3, 7, end};
        int[] possible3 = new int[] {0, 1, 4, 6};
        int[] excluded3 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled3, possible3, excluded3, scheduledInsertions3, possibleInsertions3);
    }

    @Test
    public void removeIntermediateInsertions2() {
        cp.post(new Precedence(sequence, 0, 2, 4, 7));

        sequence.insert(sequence.begin(), 4);
        sequence.insert(sequence.begin(), 5);
        sequence.insert(4, 1); // sequence: begin - 5 - 4 - 1 - end
        cp.fixPoint();

        int[][] scheduledInsertions1 = new int[][] {
                {sequence.begin(), 5}, // 4 and subsequent nodes cannot be scheduled for node 0
                {}, // scheduled
                {sequence.begin(), 5}, // 4 and subsequent nodes cannot be scheduled for node 2
                {sequence.begin(), 5, 4, 1},
                {}, // scheduled
                {}, // scheduled
                {sequence.begin(), 5, 4, 1},
                {4, 1}, // nodes before 4 cannot be scheduled anymore for node 7
        };
        int[][] possibleInsertions1 = new int[][] {
                {         3,       6,  },   // 0 cannot have 2, 4 nor 7 as predecessor
                {},                         // 1 is scheduled
                {0,       3,       6   },
                {0,    2,          6, 7},
                {},                         // 4 is scheduled
                {},                         // 5 is scheduled
                {0,    2, 3,          7},
                {0,    2, 3,       6   },
        };
        int[] scheduled1 = new int[] {begin, 5, 4, 1, end};
        int[] possible1 = new int[] {0, 2, 3, 6, 7};
        int[] excluded1 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled1, possible1, excluded1, scheduledInsertions1, possibleInsertions1);
        //TODO add more train1 cases
    }

    // train1 when only 2 nodes are in the order array, as the implementation might be a bit different
    @Test
    public void testPrecedence2() {
        cp.post(new Precedence(sequence, 0, 1));

        sequence.insert(sequence.begin(), 4); // not in order
        sequence.insert(sequence.begin(), 1); // in order
        sequence.insert(sequence.begin(), 5); // not in order. sequence: begin - 5 - 1 - 4 - end
        cp.fixPoint();

        int[][] scheduledInsertions1 = new int[][] {
                {sequence.begin(), 5}, // 1 and subsequent nodes cannot be scheduled for node 0
                {}, // scheduled
                {sequence.begin(), 5, 1, 4},
                {sequence.begin(), 5, 1, 4},
                {}, // scheduled
                {}, // scheduled
                {sequence.begin(), 5, 1, 4},
                {sequence.begin(), 5, 1, 4},
        };
        int[][] possibleInsertions1 = new int[][] {
                {      2, 3,       6, 7},
                {},                         // 1 is scheduled
                {0,       3,       6, 7},
                {0,    2,          6, 7},
                {},                         // 4 is scheduled
                {},                         // 5 is scheduled
                {0,    2, 3,          7},
                {0,    2, 3,       6   },
        };
        int[] scheduled1 = new int[] {begin, 5, 1, 4, end};
        int[] possible1 = new int[] {0, 2, 3, 6, 7};
        int[] excluded1 = new int[] {};
        CPSequenceVarTest.isSequenceValid(sequence, scheduled1, possible1, excluded1, scheduledInsertions1, possibleInsertions1);
    }

}

