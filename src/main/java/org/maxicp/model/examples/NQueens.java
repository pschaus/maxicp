package org.maxicp.model.examples;


import org.maxicp.cp.CPModelInstantiator;
import org.maxicp.cp.CPInstantiableConstraint;
import org.maxicp.cp.InstanciatedCPModel;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.model.Factory;
import org.maxicp.model.IntVar;
import org.maxicp.model.Model;
import org.maxicp.model.constraints.AllDifferent;
import org.maxicp.search.DFSearch;
import org.maxicp.state.StateManager;
import org.maxicp.util.Procedure;

import java.util.function.Supplier;

import static org.maxicp.cp.CPFactory.makeIntVarArray;

/**
 * The N-Queens problem.
 * <a href="http://csplib.org/Problems/prob054/">CSPLib</a>.
 */
public class NQueens {
    public static void main(String[] args) {
        int n = 8;
        Model model = Factory.model();

        IntVar[] q = Factory.intVarArray(n, n);
        IntVar[] qLeftDiagonal = Factory.intVarArray(n, i -> q[i].plus(i));
        IntVar[] qRightDiagonal = Factory.intVarArray(n, i -> q[i].minus(i));

        model.add(new AllDifferent(q));
        model.add(new AllDifferent(qLeftDiagonal));
        model.add(new AllDifferent(qRightDiagonal));

        model.add(new AllDifferentPersoCP.mconstraint(q[0], q[1]));

        InstanciatedCPModel s = CPModelInstantiator.instantiate(model);

        Supplier<Procedure[]> branching = new Supplier<Procedure[]>() {
            @Override
            public Procedure[] get() {
                int idx = -1; // index of the first variable that is not bound
                for (int k = 0; k < q.length; k++)
                    if (q[k].size() > 1) {
                        idx=k;
                        break;
                    }
                if (idx == -1)
                    return new Procedure[0];
                else {
                    IntVar qi = q[idx];
                    int v = qi.min();
                    Procedure left = () -> Factory.equal(qi, v);
                    Procedure right = () -> Factory.notEqual(qi, v);
                    return branch(left,right);
                }
            }
        };

        CPModelInstantiator.instantiate(model, () -> {
            DFSearch search = new DFSearch(model,branching);
        });

        ThreadLocal<Integer> test;
        StateManager sm = s.solver.getStateManager(); // to replace



        //

    }
}


class AllDifferentPersoCP extends AbstractCPConstraint {
    public AllDifferentPersoCP(CPSolver cp) {
        super(cp);
    }

    static public class mconstraint extends CPInstantiableConstraint {
        IntVar a, b;
        public mconstraint(IntVar a, IntVar b) {
            super(a, b);
            this.a = a;
            this.b = b;
        }

        @Override
        public AbstractCPConstraint instantiate(CPSolver cpSolver) {
            return new AllDifferentPersoCP(cpSolver/*cpSolver.get(a), cpSolver.get(b)*/);
        }
    }
}


