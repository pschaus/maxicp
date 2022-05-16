package org.maxicp.cp.examples;

import org.maxicp.cp.engine.constraints.LessOrEqual;
import org.maxicp.cp.engine.constraints.sequence.Cumulative;
import org.maxicp.cp.engine.constraints.sequence.Disjoint;
import org.maxicp.cp.engine.constraints.sequence.Insert;
import org.maxicp.cp.engine.constraints.sequence.TransitionTimes;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.state.datastructures.StateSparseSet;
import org.maxicp.util.Procedure;
import org.maxicp.util.io.InputReader;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.maxicp.BranchingScheme.*;
import static org.maxicp.cp.CPFactory.*;

/**
 * Dial-A-Ride problem
 */
public class DARPSequence {

    public static void main(String[] args) {
        Instance instance = new Instance("data/DARP/Cordeau2003/pr01", 100);
        int n = instance.nRequest * 2 + instance.nVehicle * 2;
        int rangeDepot = instance.nRequest * 2;

        // initialize the variables
        CPSolver cp = makeSolver();
        CPSequenceVar[] routes = new CPSequenceVar[instance.nVehicle];
        CPIntVar[] time = new CPIntVar[n];
        CPIntVar[] distance = new CPIntVar[instance.nVehicle];
        int[] load = new int[n];
        int[] duration = new int[n];
        for (int v = 0 ; v < instance.nVehicle ; ++v) { // variables for the sequences, beginning depot and end depot
            routes[v] = makeSequenceVar(cp, rangeDepot, rangeDepot + v, rangeDepot + instance.nVehicle + v);
            time[rangeDepot + v] = makeIntVar(cp, instance.depot.twStart, instance.depot.twEnd);
            time[rangeDepot + instance.nVehicle + v] = makeIntVar(cp, instance.depot.twStart, instance.depot.twEnd);
            distance[v] = makeIntVar(cp, 0, instance.depot.twEnd);
            duration[rangeDepot + v] = instance.depot.duration;
            duration[rangeDepot + instance.nVehicle + v] = instance.depot.duration;
        }
        for (int i = 0 ; i < rangeDepot ; ++i) { // variables for the nodes to visit
            time[i] = makeIntVar(cp, instance.nodes[i].twStart, instance.nodes[i].twEnd);
            duration[i] = instance.nodes[i].duration;
            load[i] = instance.nodes[i].load;
        }
        CPIntVar sumDistance = sum(distance);

        // post the constraints
        int[] pickups = IntStream.range(0, instance.nRequest).toArray();
        int[] drops = IntStream.range(instance.nRequest, rangeDepot).toArray();
        for (int v = 0 ; v < instance.nVehicle ; ++v) { // variables for the sequences, beginning depot and end depot
            // transition time between the nodes
            cp.post(new TransitionTimes(routes[v], time, distance[v], instance.distMatrix, duration));
            // a vehicle has a limited capacity when visiting pickups and drops
            cp.post(new Cumulative(routes[v], pickups, drops, instance.capacity, load));
        }
        // max ride time constraint
        for (int i = 0 ; i < instance.nRequest ; ++i) { // time[drop] - (time[pickup] + duration[pickup]) <= maxRideTime
            // time[drop] <= time[pickup] + duration[pickup] + maxRideTime
            cp.post(new LessOrEqual(time[i + instance.nRequest], plus(time[i], duration[i] + instance.maxRideTime)));
        }
        // max route duration
        for (int i = rangeDepot ; i < rangeDepot + instance.nVehicle ; ++i) { // time[end] - time[begin] <= maxRouteDuration
            // time[end] <= time[begin] + maxRouteDuration
            cp.post(new LessOrEqual(time[i + instance.nVehicle], plus(time[i], instance.maxRouteDuration)));
        }
        // a node can be visited once across all routes
        cp.post(new Disjoint(routes));

        // search procedure
        int[] insertionsPickup = new int[instance.nRequest * 2];
        int[] insertionsDrop = new int[instance.nRequest * 2];
        Supplier<Procedure[]> branching = () -> {
            // select the request that can be inserted at the fewest location
            int bestRequest = -1;
            int minInsert = Integer.MAX_VALUE;
            for (int request = 0; request < instance.nRequest ; ++request) {
                int nInsert = 0;
                for (CPSequenceVar route: routes) {
                    // min number of insert for the pickup and the drop
                    nInsert += route.nMemberInsertion(request) * route.nMemberInsertion(request + instance.nRequest);
                }
                if (nInsert < minInsert && nInsert != 0) {
                    minInsert = nInsert;
                    bestRequest = request;
                }
            }

            if (bestRequest == -1)
                return EMPTY;
            // insert the request at all feasible location across all vehicles
            int pickup = bestRequest;
            int drop = bestRequest + instance.nRequest;
            Procedure[] branches = new Procedure[minInsert];
            int i = 0 ;
            for (CPSequenceVar route: routes) {
                int nPickup = route.fillMemberInsertion(pickup, insertionsPickup);
                int nDrop = route.fillMemberInsertion(drop, insertionsDrop);
                for (int j = 0  ; j < nPickup ; ++j) {
                    int predPickup = insertionsPickup[j]; // predecessor for the pickup
                    for (int k = 0 ; k < nDrop; ++k) {
                        // predecessor for the drop. corresponds to the pickup if equal to predPickup
                        int predDrop = insertionsDrop[k] == predPickup ? pickup : insertionsDrop[k];
                        branches[i++] = () -> {
                            cp.post(new Insert(route, predPickup, pickup));
                            cp.post(new Insert(route, predDrop, drop));
                        };
                    }
                }
            }
            return branches;
        };

        DFSearch search = makeDfs(cp, branching);
        search.onSolution(() -> {
            System.out.printf("length = %.3f", ((double) sumDistance.min()) / Instance.scaling);
            for (CPSequenceVar route: routes) {
                System.out.println(route);
            }
            System.out.println("-----------------");
        });
        Objective totalDistance = cp.minimize(sumDistance);
        search.optimize(totalDistance);

    }

    static class Instance {

        /* ordering of nodes:
        0..nRequest: pickup
        nRequest..nRequest*2: drop
        nRequest*2..nRequest*2+nVehicle: begin depot
        nRequest*2+nVehicle..: ending depot
         */
        static int scaling;
        int nVehicle;
        int nRequest;
        int maxRouteDuration;
        int capacity;
        int maxRideTime;
        DARPNode depot;
        DARPNode[] nodes;
        int[][] distMatrix;

        public Instance(String filename, int scaling) {
            Instance.scaling = scaling;
            InputReader reader = new InputReader(filename);
            nVehicle = reader.getInt();
            reader.getInt(); // ignore, some instances do not use the same format for encoding nodes
            maxRouteDuration = reader.getInt() * scaling;
            capacity = reader.getInt() * scaling;
            maxRideTime = reader.getInt() * scaling;
            reader.getInt(); //id of the node, ignored
            depot = new DARPNode(reader.getDouble(), reader.getDouble(), reader.getInt(), reader.getInt(),
                    reader.getInt() * scaling, reader.getInt() * scaling);
            ArrayList<DARPNode> nodeList = new ArrayList<>();
            try {
                while (true) {
                    reader.getInt(); // id of the node, ignored
                    nodeList.add(new DARPNode(reader.getDouble(), reader.getDouble(), reader.getInt(), reader.getInt(),
                            reader.getInt() * scaling, reader.getInt() * scaling));
                }
            } catch (RuntimeException ignored) {

            }
            nodes = nodeList.toArray(new DARPNode[0]);
            nRequest = nodes.length / 2;

            // compute the distance matrix
            int n = nVehicle * 2 + nRequest * 2;
            distMatrix = new int[n][n];
            for (int i = 0 ; i < n ; ++i) {
                DARPNode from = i < nRequest * 2 ? nodes[i] : depot;
                for (int j = 0 ; j < n ; ++j) {
                    DARPNode to = j < nRequest * 2 ? nodes[j] : depot;
                    distMatrix[i][j] = from.distance(to);
                }
            }
        }

        public record DARPNode(double x, double y, int duration, int load, int twStart, int twEnd) {

            public int distance(DARPNode o) {
                return (int) Math.round(Math.sqrt((x - o.x) * (x - o.x) + (y - o.y) * (y - o.y)) * Instance.scaling);
            }

            public boolean isPickup() {
                return load > 0;
            }

            public boolean isDrop() {
                return load < 0;
            }

            public boolean isDepot() {
                return load == 0;
            }

        }



    }

}
