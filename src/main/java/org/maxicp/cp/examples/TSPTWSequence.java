package org.maxicp.cp.examples;

import org.maxicp.cp.engine.constraints.sequence.Disjoint;
import org.maxicp.cp.engine.constraints.sequence.Insert;
import org.maxicp.cp.engine.constraints.sequence.TransitionTimes;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.util.Procedure;
import org.maxicp.util.io.InputReader;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.maxicp.BranchingScheme.EMPTY;
import static org.maxicp.cp.CPFactory.*;
import static org.maxicp.util.exception.InconsistencyException.INCONSISTENCY;

/**
 * Traveling Salesman Problem With Time Window
 */
public class TSPTWSequence {

    public static void main(String[] args) {
        // read the instance
        Instance instance = new Instance("data/TSPTW/AFG/rbg010a.tw", 100);
        int[] duration = new int[instance.n]; // duration of 0 in this configuration
        
        // init the variables
        CPSolver cp = makeSolver();
        CPIntVar[] time = new CPIntVar[instance.n]; // departure time of each node
        CPIntVar distance = makeIntVar(cp, 0, instance.L[0]); // total traveled distance
        for (int i = 0 ; i < instance.n ; ++i)
            time[i] = makeIntVar(cp, instance.E[i], instance.L[i]);
        CPSequenceVar route = makeSequenceVar(cp, instance.n, instance.begin, instance.end);
        
        // add constraints
        cp.post(new TransitionTimes(route, time, distance, instance.distMatrix, duration));
        cp.post(new Disjoint(route));
        
        // search procedure
        int[] insertions = new int[instance.n];
        Supplier<Procedure[]> branching = () -> {
            if (route.nMember() == instance.n) {
                return EMPTY; // all nodes have been inserted into the route
            }
            // select the node with the less member insertions
            int bestNode = -1;
            int bestInsertions = Integer.MAX_VALUE;
            for (int node = 1 ; node < instance.n ; ++node) {
                if (route.isPossible(node)) {
                    int nInsertions = route.nMemberInsertion(node);
                    if (nInsertions < bestInsertions) {
                        bestNode = node;
                        bestInsertions = nInsertions;
                    }
                }
            }
            if (bestNode == -1 || bestInsertions == 0) { // no node could be inserted within the sequence
                throw INCONSISTENCY;
            }
            int node = bestNode;
            // insert the node at all feasible places
            int nInsertion = route.fillMemberInsertion(bestNode, insertions);
            Procedure[] branches = new Procedure[nInsertion];
            for (int i = 0 ; i < nInsertion ; ++i) {
                int pred = insertions[i];
                branches[i] = () -> {cp.post(new Insert(route, pred, node));};
            }
            return branches;
        };

        DFSearch search = makeDfs(cp, branching);
        search.onSolution(() -> {
            System.out.printf("Length: %.3f%n%s%n", ((double) distance.min()) / instance.scaling, route);
        });
        Objective objective = cp.minimize(distance);
        search.optimize(objective);
        
    }
    
    static class Instance {

        final int n; // includes the end depot
        final int begin = 0;
        final int end;
        final int [][] distMatrix;
        final int [] E, L;
        int horizon = Integer.MIN_VALUE;
        int maxDist = Integer.MIN_VALUE;
        final int scaling;


        /**
         * an instance with a dummy node introduced at the end, copy of the depot
         * @param file path where the instance is written
         * @param scale scale to convert distances into integers
         */
        public Instance(String file, int scale) {
            scaling = scale;
            InputReader reader = new InputReader(file);
            n = reader.getInt() + 1;
            end = n - 1;
            distMatrix = new int[n][n];
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - 1; j++) {
                    distMatrix[i][j] = (int) (scale * reader.getDouble());
                    maxDist = Math.max(maxDist, distMatrix[i][j]);
                }
                distMatrix[i][end] = distMatrix[i][begin];
                maxDist = Math.max(maxDist, distMatrix[i][end]);
            }
            System.arraycopy(distMatrix[begin], 1, distMatrix[end], 1, n - 2);
            E = new int[n];
            L = new int[n];

            for (int i = 0; i < n - 1; i++) {
                E[i] = scale * reader.getInt();
                L[i] = scale * reader.getInt()-10;
                horizon = Math.max(horizon, L[i]+1);
            }
            E[end] = E[begin];
            L[end] = L[begin];
        }

        @Override
        public String toString() {
            return "Instance{" +
                    "n=" + n + "\n" +
                    ", distMatrix=" + Arrays.deepToString(distMatrix) + "\n" +
                    ", E=" + Arrays.toString(E) + "\n" +
                    ", L=" + Arrays.toString(L) + "\n" +
                    ", horizon=" + horizon +
                    '}';
        }

    }
    
}
