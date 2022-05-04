package org.maxicp.cp.examples;

import org.maxicp.cp.engine.constraints.Circuit;
import org.maxicp.cp.engine.constraints.Element1DVar;
import org.maxicp.cp.engine.constraints.Sum;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.io.InputReader;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.maxicp.BranchingScheme.*;
import static org.maxicp.cp.CPFactory.*;

public class TSPTW {

    public static final int DEPOT = 0;
    public static AtomicReference<TSPTWSolution> solution;
    public static final long maxRunTime = 180_000; // in milliseconds

    // my custom parameters, to remove for students
    private static int[][] distMatrix;
    private static CPSolver cp;
    private static CPIntVar[] time;
    private static CPIntVar[] succ;
    private static CPIntVar[] pred;
    private static CPIntVar[] succDist;
    private static CPIntVar traveledDistance;
    static int nNodes;
    static int nNodesWithEndDepot;
    static int END_DEPOT;
    static long initRunTime;

    /**
     * TODO
     * given an instance {@link TSPTWInstance} to solve (a number of  nodes, distance between them and their time windows {@link TimeWindow}),
     * give the order of visit of each nodes minimizing the traveled distance of the vehicle
     * use {@link TSPTWInstance#distances} or {@link TSPTWInstance#distance(int, int)} to compute distances
     * the vehicle can arrive at a node n before the beginning of its time window {@link TimeWindow#getEarliest()}
     *  if this is the case, the departure occurs at the beginning of its time window
     * the depot is always located at node 0 {@link TSPTW#DEPOT} and has the longest time window
     * @param instance instance to solve
     * @return valid solution minimizing the traveled distance of the vehicle
     */
    public static TSPTWSolution solve(TSPTWInstance instance) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchUrgentAndDist();

        solution = new AtomicReference<>(new TSPTWSolution(instance));
        int[] bestOrdering = new int[nNodesWithEndDepot];
        AtomicInteger cnt = new AtomicInteger();

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            //printTimeObjective(solution.get());
            System.out.println(solution.get());
            cnt.addAndGet(1);
        });

        search.solve();
        System.out.println("#solutions:" + cnt.get());
        return solution.get();
    }

    private static CPIntVar elementVar(CPIntVar[] array, CPIntVar y) {
        CPSolver cp = y.getSolver();
        int min = Arrays.stream(array).mapToInt(CPIntVar::min).min().getAsInt();
        int max = Arrays.stream(array).mapToInt(CPIntVar::max).max().getAsInt();
        CPIntVar z = makeIntVar(cp, min,max);
        cp.post(new Element1DVar(array, y, z));
        return z;
    }

    // TODO remove for students

    private static void initCpVars(TSPTWInstance instance) {
        cp = makeSolver();
        nNodes = instance.nNodes;
        nNodesWithEndDepot = nNodes + 1;
        END_DEPOT = nNodes;
        time = new CPIntVar[nNodesWithEndDepot]; // time when arriving at a node
        succ = new CPIntVar[nNodesWithEndDepot]; // successor of a node
        pred = new CPIntVar[nNodesWithEndDepot]; // predecessor of a node
        succDist = new CPIntVar[nNodesWithEndDepot]; // distance between a node and its successor
        traveledDistance = makeIntVar(cp, 0, instance.timeWindows[DEPOT].getLatest());
        for (int i = 0 ; i < instance.nNodes ; ++i) {
            time[i] = makeIntVar(cp, instance.timeWindows[i].getEarliest(), instance.timeWindows[i].getLatest());
            succDist[i] = makeIntVar(cp, 0, instance.timeWindows[DEPOT].getLatest());
            succ[i] = makeIntVar(cp, 1, nNodesWithEndDepot);
            if (i != DEPOT)
                pred[i] = makeIntVar(cp, 0, nNodesWithEndDepot);
        }
        time[END_DEPOT] = makeIntVar(cp, instance.timeWindows[DEPOT].getEarliest(), instance.timeWindows[DEPOT].getLatest());
        succDist[END_DEPOT] = makeIntVar(cp, 0, 0);
        succ[END_DEPOT] = makeIntVar(cp, DEPOT, DEPOT);
        pred[DEPOT] = makeIntVar(cp, END_DEPOT, END_DEPOT);
        pred[END_DEPOT] = makeIntVar(cp, 0, nNodes);

        // extend the distance matrix
        distMatrix = new int[nNodesWithEndDepot][nNodesWithEndDepot];
        for (int i = 0 ; i < nNodes ; ++i) {
            for (int j = 0 ; j < nNodes ; ++j) {
                distMatrix[i][j] = instance.distance(i, j);
            }
        }
        for (int i = 0 ; i < nNodes ; ++i) {
            distMatrix[i][END_DEPOT] = instance.distance(i, DEPOT);
        }
    }

    /**
     * post optimisation constraints, objective is to minimize traveled distance
     */
    private static void postConstraints(TSPTWInstance instance) {
        cp.post(new Circuit(succ));
        for (int i = 0; i < instance.nNodes; ++i) {
            // time[i] + dist[i][succ[i]] <= time[succ[i]]
            CPIntVar timeSucc = elementVar(time, succ[i]);
            succDist[i] = element(distMatrix[i], succ[i]);
            //System.out.println("posting time for " + i);
            cp.post(lessOrEqual(sum(time[i], succDist[i]), timeSucc));
        }
        //System.out.println("posting sum");
        cp.post(new Sum(succDist, traveledDistance));
        // channeling between pred and succ vectors
        for (int i = 0; i < nNodesWithEndDepot; i++) {
            // succ[pred[i]] == i
            cp.post(equal(elementVar(succ,pred[i]),i));
            cp.post(equal(elementVar(pred,succ[i]),i));
        }
    }

    public static DFSearch searchFirstFail() {
        DFSearch search = makeDfs(cp, firstFail(succ));
        return search;
    }

    public static DFSearch searchMinDist() {
        int[] values = new int[nNodes];
        Integer[] indexes = new Integer[nNodes];
        for (int i = 0; i < nNodes; ++i)
            indexes[i] = i;
        DFSearch search = makeDfs(cp, () -> {
            // select the node with least successors
            Integer id = selectMin(indexes,
                    xi -> !succ[xi].isFixed(),
                    xi -> succ[xi].size());
            // go the nearest node
            if (id == null)
                return EMPTY;
            CPIntVar xs = succ[id];
            int succ_to_chose = -1;
            int nearest_dist = Integer.MAX_VALUE;
            int n = xs.fillArray(values);
            for (int i = 0 ; i < n ; ++i) {
                int candidate = values[i];
                int dist = distMatrix[id][candidate];
                if (dist < nearest_dist) {
                    nearest_dist = dist;
                    succ_to_chose = candidate;
                }
            }
            int finalSucc_to_chose = succ_to_chose;
            return branch(() -> {cp.post(equal(xs, finalSucc_to_chose));},
                    () -> {cp.post(notEqual(xs, finalSucc_to_chose));});

        });
        return search;
    }

    public static DFSearch searchMostUrgent() {
        int[] values = new int[nNodes];
        Integer[] indexes = new Integer[nNodes];
        for (int i = 0; i < nNodes; ++i)
            indexes[i] = i;

        DFSearch search = makeDfs(cp, () -> {
            // select the node with a fixed predecessor and no fixed successor
            Integer id = selectMin(indexes,
                    xi -> !succ[xi].isFixed() && pred[xi].isFixed(),
                    xi -> xi == END_DEPOT ? Integer.MAX_VALUE : succ[xi].size());
            if (id == null)
                return EMPTY;
            CPIntVar xs = succ[id];
            // goes to the most urgent node to visit
            int succ_to_chose = -1;
            int lowest_slack = Integer.MAX_VALUE;
            //int current_time = time[id].min();
            int n = xs.fillArray(values);  // fill the successors
            for (int i = 0 ; i < n ; ++i) {
                int candidate = values[i];
                int slack_left = time[candidate].max();
                if (slack_left < lowest_slack) {
                    lowest_slack = slack_left;
                    succ_to_chose = candidate;
                }
            }
            int finalSucc_to_chose = succ_to_chose;
            return branch(() -> {cp.post(equal(xs, finalSucc_to_chose));},
                    () -> {cp.post(notEqual(xs, finalSucc_to_chose));});

        });
        return search;
    }

    public static DFSearch searchUrgentAndDist() {
        int ALPHA = 80;
        int BETA = 20;

        int[] values = new int[nNodes];
        Integer[] indexes = new Integer[nNodes];
        for (int i = 0; i < nNodes; ++i)
            indexes[i] = i;

        DFSearch search = makeDfs(cp, () -> {
            // select the node with a fixed predecessor and no fixed successor
            Integer id = selectMin(indexes,
                    xi -> !succ[xi].isFixed() && pred[xi].isFixed(),
                    xi -> xi == END_DEPOT ? Integer.MAX_VALUE : succ[xi].size());
            if (id == null)
                return EMPTY;
            CPIntVar xs = succ[id];
            // goes to the most urgent node to visit
            int succ_to_chose = -1;
            int best_heuristic_val = Integer.MAX_VALUE;
            //int current_time = time[id].min();
            int n = xs.fillArray(values);  // fill the successors
            for (int i = 0 ; i < n ; ++i) {
                int candidate = values[i];
                int slack_left = time[candidate].max();
                int dist = distMatrix[id][candidate];
                int heuristic_val = ALPHA * dist + BETA * slack_left;
                if (heuristic_val < best_heuristic_val) {
                    best_heuristic_val = heuristic_val;
                    succ_to_chose = candidate;
                }
            }
            int finalSucc_to_chose = succ_to_chose;
            return branch(() -> {cp.post(equal(xs, finalSucc_to_chose));},
                    () -> {cp.post(notEqual(xs, finalSucc_to_chose));});

        });
        return search;
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static boolean isRunning() {
        return getCurrentTime() - initRunTime <= maxRunTime;
    }

    public static void initClock() {
        initRunTime = getCurrentTime();
    }

    public static long getElapsedTime() {
        return getCurrentTime() - initRunTime;
    }

    public static String elapsedTimeString() {
        double elapsed = ((double) getElapsedTime()) / 1000;
        return String.format("%.3f [s]", elapsed);
    }

    public static void printTimeObjective(TSPTWSolution solution) {
        System.out.println("time = " + elapsedTimeString() + '\n' + solution.toString());
    }

    public static TSPTWSolution solveFirstFail(TSPTWInstance instance) {
        return solveFirstFail(instance, false);
    }

    public static TSPTWSolution solveFirstFail(TSPTWInstance instance, boolean stopFirstSol) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchFirstFail();

        solution = new AtomicReference<>(new TSPTWSolution(instance));

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                solution.get().addVisit(current);
                pred = current;
            }
            //System.out.print(solution.get());
        });

        Objective objective = cp.minimize(traveledDistance);
        //System.out.println("solving");
        if (stopFirstSol) {
            SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning() || searchStatistics.numberOfSolutions() > 0);
        } else {
            SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning());
        }
        //System.out.println(stats);
        return solution.get();
    }

    public static TSPTWSolution solveMinDist(TSPTWInstance instance) {
        return solveMinDist(instance, false);
    }

    public static TSPTWSolution solveMinDist(TSPTWInstance instance, boolean stopFirstSol) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchMinDist();

        solution = new AtomicReference<>(new TSPTWSolution(instance));

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                solution.get().addVisit(current);
                pred = current;
            }
            //System.out.print(solution.get());
        });

        Objective objective = cp.minimize(traveledDistance);
        //System.out.println("solving");
        if (stopFirstSol) {
            SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning() || searchStatistics.numberOfSolutions() > 0);
        } else {
            SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning());
        }
        //System.out.println(stats);
        return solution.get();
    }

    public static TSPTWSolution solveMostUrgent(TSPTWInstance instance) {
        return solveMostUrgent(instance, false);
    }

    public static TSPTWSolution solveMostUrgent(TSPTWInstance instance, boolean stopFirstSol) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchMostUrgent();

        solution = new AtomicReference<>(new TSPTWSolution(instance));

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                solution.get().addVisit(current);
                pred = current;
            }
            //System.out.print(solution.get());
        });

        Objective objective = cp.minimize(traveledDistance);
        //System.out.println("solving");
        if (stopFirstSol) {
            SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning() || searchStatistics.numberOfSolutions() > 0);
        } else {
            SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning());
        }
        //System.out.println(stats);
        return solution.get();
    }

    public static TSPTWSolution solveMostUrgentAndDist(TSPTWInstance instance) {
        return solveMostUrgentAndDist(instance, false);
    }

    public static TSPTWSolution solveMostUrgentAndDist(TSPTWInstance instance, boolean stopFirstSol) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchUrgentAndDist();

        solution = new AtomicReference<>(new TSPTWSolution(instance));

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                solution.get().addVisit(current);
                pred = current;
            }
            //System.out.print(solution.get());
        });

        Objective objective = cp.minimize(traveledDistance);
        //System.out.println("solving");
        if (stopFirstSol) {
            SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning() || searchStatistics.numberOfSolutions() > 0);
        } else {
            SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning());
        }
        //System.out.println(stats);
        return solution.get();
    }

    public static TSPTWSolution solveUrgentDistAndDistLNS(TSPTWInstance instance) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchUrgentAndDist();

        solution = new AtomicReference<>(new TSPTWSolution(instance));
        int[] bestOrdering = new int[nNodesWithEndDepot];

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            printTimeObjective(solution.get());
            //System.out.println(solution.get());
        });

        Objective objective = cp.minimize(traveledDistance);
        //System.out.println("solving");
        SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning() || searchStatistics.numberOfSolutions() > 1);
        //System.out.println(stats);
        // LNS
        search = searchMinDist();
        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            printTimeObjective(solution.get());
            //System.out.println(solution.get());
        });


        Random rand = new Random(42);
        while (isRunning()) {
            int percentagePrevious = 80; // percentage previously kept
            int failureLimit = 1000;
            search.optimizeSubjectTo(objective, statistics -> statistics.numberOfFailures() >= failureLimit, () -> {
                for (int i = 0; i < nNodesWithEndDepot - 1; ++i) {
                    int current = bestOrdering[i];
                    if (rand.nextInt(100) < percentagePrevious) {
                        cp.post(equal(succ[current], bestOrdering[i+1]));
                    }
                }
            });
        }

        return solution.get();
    }

    public static TSPTWSolution solveUrgentDistAndDistLNSConsecutive(TSPTWInstance instance) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchUrgentAndDist();

        solution = new AtomicReference<>(new TSPTWSolution(instance));
        int[] bestOrdering = new int[nNodesWithEndDepot];

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            printTimeObjective(solution.get());
            //System.out.println(solution.get());
        });

        Objective objective = cp.minimize(traveledDistance);
        //System.out.println("solving");
        SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning() || searchStatistics.numberOfSolutions() > 1);
        //System.out.println(stats);
        // LNS
        search = searchMinDist();
        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            printTimeObjective(solution.get());
            //System.out.println(solution.get());
        });


        Random rand = new Random(42);
        while (isRunning()) {
            int nNodesRelaxed = 15;
            int failureLimit = 1000;
            search.optimizeSubjectTo(objective, statistics -> statistics.numberOfFailures() >= failureLimit, () -> {
                int idRelaxed = rand.nextInt(nNodes - nNodesRelaxed - 1);
                for (int i = 0; i < nNodesWithEndDepot - 1; ++i) {
                    if (i == idRelaxed)
                        i += nNodesRelaxed;
                    else {
                        int current = bestOrdering[i];
                        cp.post(equal(succ[current], bestOrdering[i + 1]));
                    }
                }
            });
        }

        return solution.get();
    }

    public static TSPTWSolution solveUrgentDistAndUrgentDistLNS(TSPTWInstance instance) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchUrgentAndDist();

        solution = new AtomicReference<>(new TSPTWSolution(instance));
        int[] bestOrdering = new int[nNodesWithEndDepot];

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            printTimeObjective(solution.get());
            //System.out.println(solution.get());
        });

        Objective objective = cp.minimize(traveledDistance);
        //System.out.println("solving");
        SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning() || searchStatistics.numberOfSolutions() > 1);
        //System.out.println(stats);
        // LNS
        search = searchUrgentAndDist();
        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            printTimeObjective(solution.get());
            //System.out.println(solution.get());
        });


        Random rand = new Random(42);
        while (isRunning()) {
            int percentagePrevious = 80; // percentage previously kept
            int failureLimit = 1000;
            search.optimizeSubjectTo(objective, statistics -> statistics.numberOfFailures() >= failureLimit, () -> {
                for (int i = 0; i < nNodesWithEndDepot - 1; ++i) {
                    int current = bestOrdering[i];
                    if (rand.nextInt(100) < percentagePrevious) {
                        cp.post(equal(succ[current], bestOrdering[i+1]));
                    }
                }
            });
        }

        return solution.get();
    }

    public static TSPTWSolution solveUrgentDistAndUrgentDistLNSConsecutive(TSPTWInstance instance) {
        initClock();
        initCpVars(instance);
        postConstraints(instance);
        DFSearch search = searchUrgentAndDist();

        solution = new AtomicReference<>(new TSPTWSolution(instance));
        int[] bestOrdering = new int[nNodesWithEndDepot];

        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            printTimeObjective(solution.get());
            //System.out.println(solution.get());
        });

        Objective objective = cp.minimize(traveledDistance);
        //System.out.println("solving");
        SearchStatistics stats = search.optimize(objective, (searchStatistics) -> !isRunning() || searchStatistics.numberOfSolutions() > 1);
        //System.out.println(stats);
        // LNS
        search = searchUrgentAndDist();
        search.onSolution(() -> {
            solution.get().clear();
            int pred = DEPOT;
            for (int i = 0; i < nNodes - 1 ; ++i) {
                int current = succ[pred].min();
                bestOrdering[i+1] = current;
                solution.get().addVisit(current);
                pred = current;
            }
            bestOrdering[DEPOT] = DEPOT;
            bestOrdering[END_DEPOT] = END_DEPOT;
            printTimeObjective(solution.get());
            //System.out.println(solution.get());
        });


        Random rand = new Random(42);
        while (isRunning()) {
            int nNodesRelaxed = 15;
            int failureLimit = 1000;
            search.optimizeSubjectTo(objective, statistics -> statistics.numberOfFailures() >= failureLimit, () -> {
                int idRelaxed = rand.nextInt(nNodes - nNodesRelaxed - 1);
                for (int i = 0; i < nNodesWithEndDepot - 1; ++i) {
                    if (i == idRelaxed)
                        i += nNodesRelaxed;
                    else {
                        int current = bestOrdering[i];
                        cp.post(equal(succ[current], bestOrdering[i + 1]));
                    }
                }
            });
        }

        return solution.get();
    }


    /**
     * A solution. To create one, first do new TSPTWSolution, then
     * add, in order, the id of the node being visited {@link TSPTWSolution#addVisit(int)}
     * You can also add the whole ordering in one go {@link TSPTWSolution#setVisitOrder(int[])}
     * Do not add the first nor the last stop to the depot, it is implicit
     * <p>
     * You can check the validity of your solution with {@link TSPTWSolution#compute()}, which returns the total distance
     * and throws a {@link RuntimeException} if something is invalid or {@link TSPTWSolution#isValid()}, which only tells
     * if the solution is valid or not
     * <p>
     * DO NOT MODIFY THIS CLASS.
     */
    public static class TSPTWSolution {

        private final int[] ordering; // inner ordering of the solution
        private final TSPTWInstance instance; // instance related to the solution
        private int nVisitedNodes = 0; // number of visited nodes in the provided solution

        /**
         * output the solution on two lines: its length (objective value) and its ordering
         * @return solution as string
         */
        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("Length: ");
            b.append(compute());
            b.append('\n');
            b.append(0);
            b.append(' ');
            for (int node: ordering) {
                b.append(node);
                b.append(' ');
            }
            b.append('\n');
            return b.toString();
        }

        /**
         * create a solution to a TSPTW instance
         * the visit order is added through {@link TSPTWSolution#addVisit(int)}
         * or {@link TSPTWSolution#setVisitOrder(int[])} (int)}
         * @param instance instance whose solution will be represented
         */
        public TSPTWSolution(TSPTWInstance instance) {
            this.instance = instance;
            ordering = new int[instance.nNodes - 1]; // depot is not encoded
        }

        /**
         * set the last visited node in the current travel
         * @param node last node being visited (do not include the depot)
         */
        public void addVisit(int node) {
            if (node <= 0 || node >= instance.nNodes)
                throw new RuntimeException(String.format("Node %3d is invalid for the instance", node));
            ordering[nVisitedNodes++] = node;
        }

        /**
         * set the order of visits of nodes
         * @param ordering order of nodes, not containing the depot
         */
        public void setVisitOrder(int[] ordering) {
            if (ordering.length != instance.nNodes - 1) // too many / not enough nodes in the visit
                return;
            System.arraycopy(ordering, 0, this.ordering, 0, ordering.length);
            nVisitedNodes = ordering.length;
        }

        /**
         * compute the value of the solution
         * throws a {@link RuntimeException} if the solution is invalid
         * @return objective value
         */
        public int compute() {
            if (nVisitedNodes < ordering.length)
                throw new RuntimeException("Not all nodes have been visited");
            HashSet<Integer> seenStops = new HashSet<>();
            int distance = 0;
            int pred = DEPOT;
            int currentTime = instance.timeWindows[DEPOT].getEarliest();
            int current;
            for (int i = 0 ; i < nVisitedNodes ; ++i) {
                current = ordering[i];
                if (current <= 0 || current >= instance.nNodes)
                    throw new RuntimeException(String.format("Node %3d cannot be specified in the solution", current));
                if (seenStops.contains(current))
                    throw new RuntimeException(String.format("Node %3d visited twice", current));
                seenStops.add(current);

                int edgeDist = instance.distance(pred, current);
                currentTime += edgeDist;
                // waiting at a node is allowed but the node is then processed at its earliest time
                if (currentTime < instance.timeWindows[current].getEarliest())
                    currentTime = instance.timeWindows[current].getEarliest();

                if (currentTime > instance.timeWindows[current].getLatest())
                    throw new RuntimeException(String.format("Node %3d visited too late (transition %3d -> %3d)", current, pred, current));
                distance += edgeDist;

                // goes to the next node
                pred = current;
            }
            if (seenStops.size() != nVisitedNodes)
                throw new RuntimeException("Not all nodes have been visited");
            if (seenStops.contains(0))
                throw new RuntimeException("Do not specify the depot in your solution, it is implicit at both " +
                        "the beginning and end of the travel");

            int edgeDist = instance.distance(pred, DEPOT);
            currentTime += edgeDist;
            if (currentTime > instance.timeWindows[DEPOT].getLatest())
                throw new RuntimeException("Route ended too late");
            distance += edgeDist;
            return distance;
        }

        /**
         * tell if the solution is valid or not
         * @return true if the solution is valid
         */
        public boolean isValid() {
            try {
                compute();
                return true;
            } catch (RuntimeException exception) {
                return false;
            }
        }

        /**
         * clear the solution, forgetting the order of visit that was given previously
         */
        public void clear() {
            nVisitedNodes = 0;
        }
        
    }
    
    public static class TSPTWInstance {
        int nNodes; // number of nodes in the problem (including depot)
        int[][] distances; // distance matrix
        TimeWindow[] timeWindows; // time window of each node

        /**
         * a TSPTW instance
         * @param nNodes number of nodes in the instance
         * @param distances distance between nodes
         * @param tw time windows of each node
         */
        public TSPTWInstance(final int nNodes, final int[][] distances, final TimeWindow[] tw) {
            this.nNodes = nNodes;
            this.distances = distances;
            this.timeWindows = tw;
        }

        /**
         * create an instance from a file
         * @param filePath file where the instance is written
         * @return instance related to the file
         */
        public static TSPTWInstance fromFile(String filePath) {
            InputReader reader = new InputReader(filePath);
            int nNodes = reader.getInt();
            int[][] distMatrix = reader.getIntMatrix(nNodes, nNodes);
            TimeWindow[] tw = new TimeWindow[nNodes];

            for (int i = 0 ; i < nNodes ; ++i) {
                tw[i] = new TimeWindow(reader.getInt(), reader.getInt());
            }

            TSPTWInstance instance = new TSPTWInstance(nNodes, distMatrix, tw);
            return instance;
        }

        /**
         * gives the cost associated to a visit ordering
         *
         * @param ordering order of visit for the nodes. First node == 0 == begin depot
         * @return routing cost associated with the visit of the nodes
         */
        public int cost(int[] ordering) {
            int cost = 0;
            int pred = 0;
            int current = -1;
            for (int i = 1; i < nNodes; ++i) {
                current = ordering[i];
                cost += distances[pred][current];
                pred = current;
            }
            cost += distances[current][0]; // closes the route
            return cost;
        }

        /**
         * give the distance between two nodes
         * @param from origin node
         * @param to destination node
         * @return distance to travel from node "from" until node "to"
         */
        public int distance(int from, int to) {
            return distances[from][to];
        }

        @Override
        public String toString() {
            return "TSPTWInstance{" +
                    nNodes + " nodes, " +
                    "depot window = [" + timeWindows[DEPOT].getEarliest() + "..." + timeWindows[DEPOT].getLatest() + "]" +
                    '}';
        }

    }

    public static class TimeWindow {
        final int earliest; // earliest visit time
        final int latest; // latest visit time

        /**
         * a time window, containing the earliest visit time allowed and the latest visit time allowed
         * @param earliest earliest visit time
         * @param latest latest visit time
         */
        public TimeWindow(final int earliest, final int latest) {
            this.earliest = earliest;
            this.latest   = latest;
        }

        /**
         * @return earliest visit time of the node
         */
        public int getEarliest() {
            return earliest;
        }

        /**
         * @return latest visit time of the node
         */
        public int getLatest() {
            return latest;
        }

    }

    public static void testSatisfiabilityBunchOfFiles() {

        String[] instances = {
                "data/tsptw/custom0", // custom 0 VALID
                "data/tsptw/custom1", // custom 1 VALID
                "data/tsptw/custom2", // custom 2 VALID
                "data/tsptw/custom3", // custom 3 VALID
        };

        for (String instanceFile: instances) {
            TSPTWInstance instance = TSPTWInstance.fromFile(instanceFile);
            System.out.println(instanceFile);
            TSPTWSolution sol;
            sol = solveMostUrgentAndDist(instance, true);
            System.out.println("\t- most urgent and min dist " + (sol.isValid() ? "OK" : "KO") + " in " + elapsedTimeString());
            sol = solveMinDist(instance, true);
            System.out.println("\t- min dist " + (sol.isValid() ? "OK" : "KO") + " in " + elapsedTimeString());
            sol = solveFirstFail(instance, true);
            System.out.println("\t- first fail " + (sol.isValid() ? "OK" : "KO") + " in " + elapsedTimeString());
            sol = solveMostUrgent(instance, true);
            System.out.println("\t- most urgent " + (sol.isValid() ? "OK" : "KO") + " in " + elapsedTimeString());
        }
    }

    public static void testObjectiveValues() {
        String[] instances = {
                "data/tsptw/custom0", // custom 0 VALID
                "data/tsptw/custom1", // custom 1 VALID
                "data/tsptw/custom2", // custom 2 VALID
                "data/tsptw/custom3", // custom 3 VALID
        };
        try {
            for (String instanceFile : instances) {
                TSPTWInstance instance = TSPTWInstance.fromFile(instanceFile);
                System.out.println(instanceFile);
                TSPTWSolution sol;
                String[] a = instanceFile.split("/");
                String instance_file = a[a.length - 1];
                String folder = "data/tsptw/students_expected_results/" + instance_file + "/";

                System.setOut(System.out);
                String filename = folder + "dist_lns.txt";
                System.out.println("\t" + filename);
                System.setOut(outputFile(filename));
                sol = solveUrgentDistAndDistLNS(instance);

                System.setOut(System.out);
                filename = folder + "dist_lns_consecutive.txt";
                System.out.println("\t" + filename);
                System.setOut(outputFile(filename));
                sol = solveUrgentDistAndDistLNSConsecutive(instance);

                System.setOut(System.out);
                filename = folder + "dist_time_lns.txt";
                System.out.println("\t" + filename);
                System.setOut(outputFile(filename));
                sol = solveUrgentDistAndUrgentDistLNS(instance);

                System.setOut(System.out);
                filename = folder + "dist_time_lns_consecutive.txt";
                System.out.println("\t" + filename);
                System.setOut(outputFile(filename));
                sol = solveUrgentDistAndUrgentDistLNSConsecutive(instance);
            }
        } catch (FileNotFoundException e) {
            System.out.println("failed to find a file");
        }
    }

    protected static PrintStream outputFile(String name) throws FileNotFoundException {
        return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)), true);
    }

    /**
     * solve a TSPTW instance using the time allowed
     * @param args contains the path to the instance
     */
    public static void main(String[] args) {
        // Reading the data

        TSPTWInstance instance = TSPTWInstance.fromFile("data/TSPTW/AFG/rbg010a.tw");
        TSPTWSolution sol;
        sol = solve(instance);
    }
    
}
