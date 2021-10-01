package org.maxicp.model.examples;


import org.maxicp.cp.CPModelInstantiator;
import org.maxicp.cp.CPInstantiableConstraint;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.model.Factory;
import org.maxicp.model.IntVar;
import org.maxicp.model.Model;
import org.maxicp.model.constraints.AllDifferent;

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

        model.add(new AllDifferent(q));

        model.add(new AllDifferentPersoCP.mconstraint(q[0], q[1]));

        InstanciatedCPModel s = CPModelInstantiator.instantiate(model);

        //s.getSolver();
        //s.get(var);
        //s.get(q) ->
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


