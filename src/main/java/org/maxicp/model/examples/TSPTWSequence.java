package org.maxicp.model.examples;

import org.maxicp.cp.CPModelInstantiator;
import org.maxicp.model.Factory;
import org.maxicp.model.IntVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.SequenceVar;
import org.maxicp.model.constraints.sequence.Disjoint;
import org.maxicp.model.constraints.sequence.Insert;
import org.maxicp.model.constraints.sequence.TransitionTimes;
import org.maxicp.search.DFSearch;
import org.maxicp.util.Procedure;
import org.maxicp.util.TimeIt;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.io.InputReader;

import java.util.Arrays;
import java.util.function.Supplier;

public class TSPTWSequence {

    public static void main(String[] args) {
        new TSPTWSequence("data/TSPTW/AFG/rbg010a.tw");
    }

    ModelDispatcher baseModel = Factory.makeModelDispatcher();
    IntVar[] time;      // visit time of each node
    IntVar dist;        // traveled distance
    SequenceVar route;  // route between the nodes
    int n;              // number of nodes (including an ending node being a copy of the beginnning depot)


    TSPTWSequence(String path) {
        Instance instance = new Instance(path, 100);
        n = instance.n;
        // departure time from each node
        time = new IntVar[instance.n];
        for (int i = 0 ; i < instance.n ; ++i) {
            time[i] = baseModel.intVar(instance.E[i], instance.L[i]);
        }
        // capture the maximum traveled distance
        dist = baseModel.intVar(0, instance.maxDist * instance.n);
        // route composed of n nodes, the beginning node is at node 0 and the ending node at node n-1
        route = baseModel.sequenceVar(instance.n, instance.begin, instance.end);
        int[] duration = new int[instance.n]; // duration at nodes is zero in this configuration

        // transition between the nodes
        baseModel.add(new TransitionTimes(route, time, dist, instance.distMatrix, duration));
        // no node can be excluded from the sequence
        baseModel.add(new Disjoint(route));

        // search procedure
        Supplier<Procedure[]> branching = () -> {
            if (route.nMember() == n) {
                return new Procedure[0]; // all nodes have been inserted into the route
            }
            // select the node with the less member insertions
            int bestNode = -1;
            int bestInsertions = Integer.MAX_VALUE;
            for (int node = 1 ; node < n ; ++node) {
                if (route.isPossible(node)) {
                    int nInsertions = route.nMemberInsertions(node);
                    if (nInsertions < bestInsertions) {
                        bestNode = node;
                        bestInsertions = nInsertions;
                    }
                }
            }
            if (bestNode == -1 || bestInsertions == 0) { // no node could be inserted within the sequence
                throw InconsistencyException.INCONSISTENCY;
            }
            int node = bestNode;
            // insert the node at all feasible places
            Procedure[] branches = new Procedure[bestInsertions];
            int[] insertions = new int[bestInsertions];
            route.fillMemberInsertion(bestNode, insertions);
            for (int i = 0 ; i < bestInsertions ; ++i) {
                int pred = insertions[i];
                branches[i] = () -> {baseModel.add(new Insert(route, pred, node));};
            }
            return branches;
        };

        System.out.println("--- SIMPLE SOLVING");

        long time = TimeIt.run(() -> {
            baseModel.runAsConcrete(CPModelInstantiator.withTrailing, (cp) -> {
                DFSearch search = cp.dfSearch(branching);
                search.onSolution(() -> {
                    System.out.println(route);
                    System.out.printf("length: %.3f%n", (((double) dist.min()) / instance.scaling));
                });
                search.solve(s -> s.numberOfSolutions() == 1);
                //search.optimize(cp.minimize(dist));
            });
        });
        System.out.println("Time taken for simple resolution: " + (time/1000000000.));

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
