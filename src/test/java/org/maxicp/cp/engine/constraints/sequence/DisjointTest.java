package org.maxicp.cp.engine.constraints.sequence;

import org.junit.Before;
import org.junit.Test;
import org.maxicp.cp.CPFactory;
import org.maxicp.cp.engine.CPSolverTest;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.state.StateManager;
import org.maxicp.util.exception.InconsistencyException;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DisjointTest extends CPSolverTest {

    CPSolver cp;
    CPSequenceVar[] CPSequenceVars;
    int nSequences = 5;
    int nNodes = 10;

    @Before
    public void SetUp() {
        cp = solverFactory.get();
        CPSequenceVars = new CPSequenceVar[nSequences];
        for (int i = 0; i < nSequences ; ++i) {
            CPSequenceVars[i] = CPFactory.makeSequenceVar(cp, nNodes, nNodes+i, nNodes + nSequences + i);
        }
    }

    @Test
    public void testDisjoint1() {
        for (int i = 0; i < nSequences ; ++i) {
            CPSequenceVars[i] = CPFactory.makeSequenceVar(cp, nNodes, nNodes+i, nNodes + nSequences + i);
        }
        cp.post(new Disjoint(CPSequenceVars));
        // no modifications should have occurred at the moment

        CPSequenceVars[0].insert(CPSequenceVars[0].begin(), 5);
        cp.fixPoint();
        // node 5 should be excluded from all others sequences
        for (int i = 1; i < nSequences ; ++i) {
            assertTrue(CPSequenceVars[i].isExcluded(5));
        }

        CPSequenceVars[1].insert(CPSequenceVars[1].begin(), 2);
        cp.fixPoint();
        // node 2 should be excluded from all others sequences
        for (int i = 0; i < nSequences ; ++i) {
            if (i != 1)
                assertTrue(CPSequenceVars[i].isExcluded(2));
        }

        CPSequenceVars[4].insert(CPSequenceVars[4].begin(), 8);
        cp.fixPoint();
        // node 2 should be excluded from all others sequences
        for (int i = 0; i < nSequences ; ++i) {
            if (i != 4)
                assertTrue(CPSequenceVars[i].isExcluded(8));
        }

    }

    @Test
    public void testDisjoint2() {
        CPSolver cp = solverFactory.get();
        StateManager sm = cp.getStateManager();
        int nSequences = 5;
        int nNodes = 10;
        CPSequenceVar[] CPSequenceVars = new CPSequenceVar[nSequences];

        for (int i = 0; i < nSequences ; ++i) {
            CPSequenceVars[i] = CPFactory.makeSequenceVar(cp, nNodes, nNodes+i, nNodes + nSequences + i);
        }
        // a node cannot be excluded from all sequences
        sm.saveState();
        cp.post(new Disjoint(true, CPSequenceVars));
        CPSequenceVar chosen = CPSequenceVars[new Random().nextInt(CPSequenceVars.length)];
        for (CPSequenceVar s: CPSequenceVars) {
            if (s != chosen) {
                s.exclude(3);
                try {
                    cp.fixPoint();
                } catch (InconsistencyException e) {
                    fail("failed to exclude a node from a sequence when it was still possible in another sequence");
                }
            }
        }
        cp.getStateManager().saveState();
        try {
            chosen.exclude(3);
            cp.fixPoint();
            fail("a node was excluded from all sequences without raising an inconsistency");
        } catch (InconsistencyException e) {

        }
        cp.getStateManager().restoreState();
        try {
            chosen.insert(chosen.begin(), 3);
        } catch (InconsistencyException e) {
            fail("failed to schedule a node when it was possible");
        }
    }

    @Test(expected = InconsistencyException.class)
    public void testDisjoint3() {
        // cannot schedule a node in more than 2 sequences
        cp.post(new Disjoint(CPSequenceVars));
        if (CPSequenceVars.length <= 1)
            return;
        Random random = new Random();
        int i = random.nextInt(CPSequenceVars.length);
        int j = random.nextInt(CPSequenceVars.length);
        while (j == i) {
            j = random.nextInt(CPSequenceVars.length);
        }
        int node = random.nextInt(nNodes);
        CPSequenceVars[i].insert(CPSequenceVars[i].begin(), node);
        CPSequenceVars[j].insert(CPSequenceVars[j].begin(), node);
        cp.fixPoint();
    }

    @Test(expected = InconsistencyException.class)
    public void testDisjointOneSequence1() {
        CPSequenceVars[0].exclude(2);
        cp.post(new Disjoint(CPSequenceVars[0]));
    }

    @Test(expected = InconsistencyException.class)
    public void testDisjointOneSequence2() {
        try {
            cp.post(new Disjoint(CPSequenceVars[0]));
        } catch (InconsistencyException e) {
            fail("inconsistency should not be thrown when no node is excluded");
        }
        CPSequenceVars[0].exclude(2);
        cp.fixPoint();
    }

    @Test(expected = InconsistencyException.class)
    public void testExcludeInMultipleSequence() {
        try {
            cp.post(new Disjoint(CPSequenceVars));
        } catch (InconsistencyException e) {
            fail("inconsistency should not be thrown when no node is excluded");
        }
        for (CPSequenceVar seq : CPSequenceVars) {
            seq.exclude(2);
        }
        cp.fixPoint();
    }

    @Test(expected = InconsistencyException.class)
    public void testDisjointAfterExclusion() {
        for (CPSequenceVar seq : CPSequenceVars) {
            seq.exclude(2);
        }
        cp.post(new Disjoint(CPSequenceVars));
    }
}
