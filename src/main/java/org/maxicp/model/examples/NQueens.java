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
import org.maxicp.search.DFSearch;
import org.maxicp.util.Procedure;

import java.util.function.Supplier;

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

        baseModel.add(new AllDifferentPersoCP.mconstraint(q[0], q[1]));

        baseModel.runAsConcrete(CPModelInstantiator.withTrailing, () -> {
            //Here the model is instanciated
            System.out.println("hello!");
        });

        /*Supplier<Procedure[]> branching = new Supplier<Procedure[]>() {
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

        CPModelInstantiator.instantiate(baseModel, () -> {
            DFSearch search = new DFSearch(model,branching);
        });*/
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


