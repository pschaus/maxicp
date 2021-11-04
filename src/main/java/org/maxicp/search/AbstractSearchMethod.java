/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package org.maxicp.search;

import org.maxicp.state.StateManager;
import org.maxicp.util.Procedure;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.exception.NotImplementedException;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An abstract search method, implementing all the needed parts, but the search method itself.
 */
public abstract class AbstractSearchMethod {

    protected Supplier<Procedure[]> branching;
    protected StateManager sm;

    protected List<Procedure> solutionListeners = new LinkedList<Procedure>();
    protected List<Procedure> failureListeners = new LinkedList<Procedure>();

    public AbstractSearchMethod(StateManager sm, Supplier<Procedure[]> branching) {
        this.sm = sm;
        this.branching = branching;
    }

    /**
     * Adds a listener that is called on each solution.
     *
     * @param listener the closure to be called whenever a solution is found
     */
    public void onSolution(Procedure listener) {
        solutionListeners.add(listener);
    }

    /**
     * Adds a listener that is called whenever a failure occurs
     * and the search backtracks.
     * This happensthat when a {@link InconsistencyException} is thrown
     * when executing the closure generated by the branching.
     *
     * @param listener the closure to be called whenever a failure occurs and
     *                 the search need to backtrack
     */
    public void onFailure(Procedure listener) {
        failureListeners.add(listener);
    }

    protected void notifySolution() {
        solutionListeners.forEach(s -> s.call());
    }

    protected void notifyFailure() {
        failureListeners.forEach(s -> s.call());
    }

    protected SearchStatistics solve(SearchStatistics statistics, Predicate<SearchStatistics> limit) {
        sm.withNewState(() -> {
            try {
                startSolve(statistics, limit);
                statistics.setCompleted();
            }
            catch (StopSearchException ignored) {

            }
        });
        return statistics;
    }


    /**
     * Start the solving process
     *
     * @return an object with the statistics on the search
     */
    public SearchStatistics solve() {
        SearchStatistics statistics = new SearchStatistics();
        return solve(statistics, stats -> false);
    }

    /**
     * Start the solving process
     * with a given predicate called at each node
     * to stop the search when it becomes true.
     *
     * @param limit a predicate called at each node
     *             that stops the search when it becomes true
     * @return an object with the statistics on the search
     */
    public SearchStatistics solve(Predicate<SearchStatistics> limit) {
        SearchStatistics statistics = new SearchStatistics();
        return solve(statistics, limit);
    }

    /**
     * Start the solving process
     * with a given predicate called at each node
     * to stop the search when it becomes true.
     * The state manager saves the state
     * before executing the closure
     * and restores it after the search.
     * Any {@link InconsistencyException} that may
     * be throw when executing the closure is also catched.
     *
     * @param limit a predicate called at each node
     *             that stops the search when it becomes true
     * @param subjectTo the closure to execute prior to the search starts
     * @return an object with the statistics on the search
     */
    public SearchStatistics solveSubjectTo(Predicate<SearchStatistics> limit, Procedure subjectTo) {
        SearchStatistics statistics = new SearchStatistics();
        sm.withNewState(() -> {
            try {
                subjectTo.call();
                solve(statistics, limit);
            } catch (InconsistencyException ignored) {
            }
        });
        return statistics;
    }

    /**
     * Start the solving process with a given objective.
     *
     * @param obj the objective to optimize that is tightened each
     *            time a new solution is found
     * @return an object with the statistics on the search
     */
    public SearchStatistics optimize(Objective obj) {
        return optimize(obj, stats -> false);
    }

    /**
     * Start the solving process with a given objective
     * and with a given predicate called at each node
     * to stop the search when it becomes true.
     *
     * @param obj the objective to optimize that is tightened each
     *            time a new solution is found
     * @param limit a predicate called at each node
     *             that stops the search when it becomes true
     * @return an object with the statistics on the search
     */
    public SearchStatistics optimize(Objective obj, Predicate<SearchStatistics> limit) {
        SearchStatistics statistics = new SearchStatistics();
        onSolution(obj::tighten);
        return solve(statistics, limit);
    }

    /**
     * Executes a closure prior to effectively
     * starting a branch and bound depth first search
     * with a given objective to optimize
     * and a given predicate called at each node
     * to stop the search when it becomes true.
     * The state manager saves the state
     * before executing the closure
     * and restores it after the search.
     * Any {@link InconsistencyException} that may
     * be throw when executing the closure is also catched.
     *
     * @param obj the objective to optimize that is tightened each
     *            time a new solution is found
     * @param limit a predicate called at each node
     *             that stops the search when it becomes true
     * @param subjectTo the closure to execute prior to the search starts
     * @return an object with the statistics on the search
     */
    public SearchStatistics optimizeSubjectTo(Objective obj, Predicate<SearchStatistics> limit, Procedure subjectTo) {
        SearchStatistics statistics = new SearchStatistics();
        sm.withNewState(() -> {
            try {
                subjectTo.call();
                optimize(obj, limit);
            }
            catch (InconsistencyException ignored) {
            }
        });
        return statistics;
    }

    /**
     * Start the solving process.
     *
     * This method must be implemented by subclasses and do the heavy work.
     * It must call notifySolution/notifyFailure when a solution is found or a failure occurs.
     *
     * @param statistics
     * @param limit
     */
    protected abstract void startSolve(SearchStatistics statistics, Predicate<SearchStatistics> limit);
}
