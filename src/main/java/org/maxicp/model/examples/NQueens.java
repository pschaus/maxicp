package org.maxicp.model.examples;


import org.maxicp.cp.CPModelInstantiator;
import org.maxicp.cp.CPInstantiableConstraint;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.cp.modelingCompat.InstanciatedCPModel;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.Factory;
import org.maxicp.model.IntVar;
import org.maxicp.model.constraints.AllDifferent;
import org.maxicp.model.constraints.Equal;
import org.maxicp.model.constraints.NotEqual;
import org.maxicp.search.DFSearch;
import org.maxicp.util.Procedure;

import java.util.function.Supplier;

import static org.maxicp.BranchingScheme.branch;

/**
 * The N-Queens problem.
 * <a href="http://csplib.org/Problems/prob054/">CSPLib</a>.
 */
public class NQueens {
    public static void main(String[] args) {
        int n = 8;
        ModelDispatcher baseModel = Factory.makeModelDispatcher();

        IntVar[] q = baseModel.intVarArray(n, n);
        IntVar[] qLeftDiagonal = baseModel.intVarArray(n, i -> q[i].plus(i));
        IntVar[] qRightDiagonal = baseModel.intVarArray(n, i -> q[i].minus(i));

        baseModel.add(new AllDifferent(q));
        baseModel.add(new AllDifferent(qLeftDiagonal));
        baseModel.add(new AllDifferent(qRightDiagonal));

        baseModel.add(new Equal(q[0], 0));
        //baseModel.add(new AllDifferentPersoCP.mconstraint(q[0], q[1]));

        Supplier<Procedure[]> branching = () -> {
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
                Procedure left = () -> baseModel.add(new Equal(qi, v));
                Procedure right = () -> baseModel.add(new NotEqual(qi, v));
                return branch(left,right);
            }
        };

        baseModel.runAsConcrete(CPModelInstantiator.withTrailing, () -> {
            //DFSearch search = new DFSearch(baseModel,branching);
        });
    }
}


class AllDifferentPersoCP extends AbstractCPConstraint {
    public AllDifferentPersoCP(CPIntVar... x) {
        super(x[0].getSolver());
    }

    static public class mconstraint extends CPInstantiableConstraint {
        IntVar a, b;
        public mconstraint(IntVar a, IntVar b) {
            super(a, b);
            this.a = a;
            this.b = b;
        }

        @Override
        public AbstractCPConstraint instantiate(InstanciatedCPModel model) {
            return new AllDifferentPersoCP(model.getVar(a), model.getVar(b));
        }
    }
}


