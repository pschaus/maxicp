package org.maxicp.search;

import org.maxicp.model.Model;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.concrete.ConcreteModel;
import org.maxicp.model.symbolic.SymbolicModel;
import org.maxicp.state.StateManager;
import org.maxicp.util.Procedure;
import org.maxicp.util.exception.InconsistencyException;

import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BestFirstSearch<T extends Comparable<T>> extends AbstractSearchMethod {
    private Supplier<T> nodeEvaluator;
    private ModelDispatcher md;
    private PriorityQueue<PQEntry<T>> pq = new PriorityQueue<>();

    record PQEntry<T extends Comparable<T>>(T order, SymbolicModel m) implements Comparable<PQEntry<T>> {
        @Override
        public int compareTo(PQEntry<T> o) {
            return this.order.compareTo(o.order);
        }
    }

    public BestFirstSearch(ModelDispatcher md, StateManager sm, Supplier<Procedure[]> branching, Supplier<T> nodeEvaluator) {
        super(sm, branching);
        this.md = md;
        this.nodeEvaluator = nodeEvaluator;
    }

    @Override
    protected void startSolve(SearchStatistics statistics, Predicate<SearchStatistics> limit) {
        pq.clear();
        pq.add(new PQEntry<>(nodeEvaluator.get(), md.getModel().symbolicCopy()));

        while (!pq.isEmpty()) {
            if (limit.test(statistics))
                throw new StopSearchException();

            SymbolicModel m = pq.poll().m;
            statistics.incrNodes();

            sm.saveState();
            ((ConcreteModel)md.getModel()).jumpTo(m);
            Procedure[] alts = branching.get();

            if(alts.length == 0) {
                statistics.incrSolutions();
                notifySolution();
            }
            else {
                for (Procedure b : alts) {
                    sm.saveState();
                    try {
                        b.call();
                        pq.add(new PQEntry<>(nodeEvaluator.get(), md.getModel().symbolicCopy()));
                    }
                    catch (InconsistencyException e) {
                        statistics.incrFailures();
                        notifyFailure();
                    }
                    sm.restoreState();
                }
            }
        }
    }

    public SymbolicModel[] getUnexploredModels() {
        return pq.stream().map(x -> x.m).toArray(SymbolicModel[]::new);
    }
}
