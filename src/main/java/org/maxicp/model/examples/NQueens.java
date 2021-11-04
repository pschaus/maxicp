package org.maxicp.model.examples;


import org.maxicp.cp.CPModelInstantiator;
import org.maxicp.cp.CPInstantiableConstraint;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.ConcreteCPModel;
import org.maxicp.model.Model;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.Factory;
import org.maxicp.model.IntVar;
import org.maxicp.model.constraints.AllDifferent;
import org.maxicp.model.constraints.Equal;
import org.maxicp.model.constraints.NotEqual;
import org.maxicp.model.symbolic.SymbolicModel;
import org.maxicp.search.*;
import org.maxicp.util.Procedure;
import org.maxicp.util.TimeIt;

import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.maxicp.BranchingScheme.branch;

/**
 * The N-Queens problem.
 * <a href="http://csplib.org/Problems/prob054/">CSPLib</a>.
 */
public class NQueens {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int n = 11;
        ModelDispatcher baseModel = Factory.makeModelDispatcher();

        IntVar[] q = baseModel.intVarArray(n, n);
        IntVar[] qLeftDiagonal = baseModel.intVarArray(n, i -> q[i].plus(i));
        IntVar[] qRightDiagonal = baseModel.intVarArray(n, i -> q[i].minus(i));

        baseModel.add(new AllDifferent(q));
        baseModel.add(new AllDifferent(qLeftDiagonal));
        baseModel.add(new AllDifferent(qRightDiagonal));

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

        //
        // Basic standard solving demo
        //
        System.out.println("--- SIMPLE SOLVING");
        long time = TimeIt.run(() -> {

            baseModel.runAsConcrete(CPModelInstantiator.withTrailing, (cp) -> {
                DFSearch search = cp.dfSearch(branching);
                System.out.println("Total number of solutions: " + search.solve().numberOfSolutions());
            });
        });
        System.out.println("Time taken for simple resolution: " + (time/1000000000.));


        //
        // Basic EPS solving demo
        //
        System.out.println("--- EPS (DFS for decomposition)");
        long time2 = TimeIt.run(() -> {
            ExecutorService executorService = Executors.newFixedThreadPool(8);

            Function<Model, SearchStatistics> epsSolve = (m) -> {
                return baseModel.runAsConcrete(CPModelInstantiator.withTrailing, m, (cp) -> {
                    DFSearch search = cp.dfSearch(branching);
                    return search.solve();
                });
            };
            LinkedList<Future<SearchStatistics>> results = new LinkedList<>();

            // Create subproblems and start EPS
            baseModel.runAsConcrete(CPModelInstantiator.withTrailing, (cp) -> {
                DFSearch search = cp.dfSearch(new LimitedDepthBranching(branching, 10));
                search.onSolution(() -> {
                    Model m = cp.symbolicCopy();
                    results.add(executorService.submit(() -> epsSolve.apply(m)));
                });
                System.out.println("Number of EPS subproblems generated: " + search.solve().numberOfSolutions());
            });

            int count = 0;
            for (var fr : results) {
                try {
                    count += fr.get().numberOfSolutions();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Total number of solutions (in EPS): " + count);
            executorService.shutdown();
        });
        System.out.println("Time taken for EPS resolution: " + (time2/1000000000.));

        //
        // EPS with bigger-cartesian-product-first decomposition solving demo
        //
        System.out.println("--- EPS (BestFirstSearch based on Cartesian Space for decomposition)");
        long time3 = TimeIt.run(() -> {
            ExecutorService executorService = Executors.newFixedThreadPool(8);

            Function<Model, SearchStatistics> epsSolve = (m) -> {
                return baseModel.runAsConcrete(CPModelInstantiator.withTrailing, m, (cp) -> {
                    DFSearch search = cp.dfSearch(branching);
                    return search.solve();
                });
            };
            LinkedList<Future<SearchStatistics>> results = new LinkedList<>();

            // Create subproblems and start EPS
            baseModel.runAsConcrete(CPModelInstantiator.withTrailing, (cp) -> {
                BestFirstSearch<Double> search = cp.bestFirstSearch(branching, () -> -CartesianSpaceEvaluator.evaluate(q));
                search.onSolution(() -> {
                    Model m = cp.symbolicCopy();
                    results.add(executorService.submit(() -> epsSolve.apply(m)));
                });
                int count = search.solve(ss -> ss.numberOfNodes() > 1000).numberOfSolutions();
                for(SymbolicModel m: search.getUnexploredModels()) {
                    results.add(executorService.submit(() -> epsSolve.apply(m)));
                    count += 1;
                }
                System.out.println("Number of EPS subproblems generated: " + count);
            });

            int count = 0;
            for (var fr : results) {
                try {
                    count += fr.get().numberOfSolutions();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Total number of solutions (in EPS): " + count);
            executorService.shutdown();
        });
        System.out.println("Time taken for EPS resolution: " + (time3/1000000000.));
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
        public AbstractCPConstraint instantiate(ConcreteCPModel model) {
            return new AllDifferentPersoCP(model.getVar(a), model.getVar(b));
        }
    }
}


