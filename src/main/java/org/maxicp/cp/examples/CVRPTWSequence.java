package org.maxicp.cp.examples;

import org.maxicp.cp.engine.constraints.Sum;
import org.maxicp.cp.engine.constraints.sequence.*;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.util.Procedure;
import org.maxicp.util.io.InputReader;

import java.util.ArrayList;
import java.util.function.Supplier;

import static org.maxicp.BranchingScheme.*;
import static org.maxicp.cp.CPFactory.*;

/**
 * capacitated vehicle routing, with time windows
 */
public class CVRPTWSequence {

    public static void main(String[] args) {
        Instance instance = new Instance("data/CVRPTW/Solomon/C101.txt", 100);
        int nNode = instance.nRequest + 2 * instance.nVehicle;
        int[][] distMatrix = instance.distMatrix;
        // init variables
        CPSolver cp = makeSolver();
        CPSequenceVar[] vehicles = new CPSequenceVar[instance.nVehicle];
        CPIntVar[] time = new CPIntVar[nNode];
        CPIntVar[] distance = new CPIntVar[instance.nVehicle];
        CPIntVar sumDistance = makeIntVar(cp, 0, instance.nVehicle * instance.depot.twEnd);
        CPIntVar[] vehicleLoad = new CPIntVar[instance.nVehicle];
        int[] duration = new int[nNode];
        int[] load = new int[nNode];
        for (int i = 0 ; i < instance.nRequest ; ++i) {
            time[i] = makeIntVar(cp, instance.requestNode[i].twStart, instance.requestNode[i].twEnd);
            duration[i] = instance.requestNode[i].duration;
            load[i] = instance.requestNode[i].demand;
        }
        for (int i = instance.nRequest ; i < nNode; ++i) {
            time[i] = makeIntVar(cp, instance.depot.twStart, instance.depot.twEnd);
            duration[i] = instance.depot.duration;
            load[i] = instance.depot.demand;
        }
        for (int v = 0; v < instance.nVehicle ; ++v) {
            vehicles[v] = makeSequenceVar(cp, instance.nRequest, instance.nRequest + v * 2, instance.nRequest + v * 2 + 1);
            distance[v] = makeIntVar(cp, 0, instance.depot.twEnd);
            vehicleLoad[v] = makeIntVar(cp, 0, instance.capacity);
        }

        // add the constraints
        for (int v = 0 ; v < instance.nVehicle ; ++v) {
            cp.post(new Capacity(vehicles[v], vehicleLoad[v], load));
            cp.post(new TransitionTimes(vehicles[v], time, distance[v], distMatrix, duration));
        }
        cp.post(new Sum(distance, sumDistance));
        Disjoint disjoint = new Disjoint(vehicles);
        cp.post(disjoint);

        // search procedure
        Integer[] vehicleRange = new Integer[instance.nVehicle];
        for (int i = 0; i < instance.nVehicle ; ++i) {
            vehicleRange[i] = i;
        }
        int[] insertions = new int[instance.nRequest];
        Supplier<Procedure[]> branching = () -> {
            // select the unfixed vehicle with the highest current load
            Integer v = selectMin(vehicleRange,
                    i -> !vehicles[i].isFixed(),
                    i -> -vehicleLoad[i].min());
            if (v == null)
                return EMPTY; // all vehicles are fixed
            // select the node with the minimum number of insertions for the vehicle
            int nPossible = vehicles[v].fillPossible(insertions);
            int bestNode = -1;
            int bestCost = Integer.MAX_VALUE;
            for (int i = 0 ; i < nPossible ; ++i) {
                int node = insertions[i];
                if (vehicles[v].nMemberInsertion(node) < bestCost) {
                    bestCost = vehicles[v].nMemberInsertion(node);
                    bestNode = node;
                }
            }
            // insert the node at the location minimizing the most the increase in distance
            int bestPred = -1;
            bestCost = Integer.MAX_VALUE;
            int nInsert = vehicles[v].fillMemberInsertion(bestNode, insertions);
            for (int i = 0 ; i < nInsert ; ++i) {
                int pred = insertions[i];
                int succ = vehicles[v].nextMember(pred);
                int cost = distMatrix[pred][bestNode] + distMatrix[bestNode][succ] - distMatrix[pred][succ];
                if (cost < bestCost) {
                    bestCost = cost;
                    bestPred = pred;
                }
            }
            int finalPred = bestPred;
            int finalNode = bestNode;
            return branch(() -> {cp.post(new Insert(vehicles[v], finalPred, finalNode));},
                    () -> {cp.post(new RemoveInsert(vehicles[v], finalPred, finalNode));
            });
        };

        DFSearch search = makeDfs(cp, branching);
        Objective distanceObjective = cp.minimize(sumDistance);
        search.onSolution(() -> {
            System.out.printf("Length = %.3f%n", ((double) sumDistance.min()) / Instance.scaling);
            for (CPSequenceVar vi: vehicles) {
                if (vi.nMember(false) > 0)
                    System.out.println(vi);
            }
            System.out.println("-----------------");
        });
        search.optimize(distanceObjective);
    }

    static class Instance {

        public static int scaling;
        public int nVehicle;
        public int capacity;
        public int nRequest;  // begin and end nodes are located at nRequest...nRequest+2*nVehicles
        public int[][] distMatrix;
        public String name;
        public CVRPTWNode[] requestNode;
        public CVRPTWNode depot;
        
        public Instance(String filename, int scaling) {
            Instance.scaling = scaling;
            // name of the instance
            InputReader reader = new InputReader(filename);
            name = reader.getString();
            // next 3 strings are ignored
            for (int i = 0 ; i < 3 ; ++i) {
                String line = reader.getString();
                int a = 0;
            }
            // number of vehicles and capacity
            nVehicle = reader.getInt();
            capacity = reader.getInt();
            // next 12 strings are ignored
            for (int i = 0 ; i < 12 ; ++i) {
                String s = reader.getString();
            }
            ArrayList<CVRPTWNode> nodeList = new ArrayList<>();
            try {
                depot = new CVRPTWNode(reader.getInt(), reader.getInt(), reader.getInt(), reader.getInt(),
                        reader.getInt() * scaling, reader.getInt() * scaling, reader.getInt() * scaling);
                while (true) {
                    nodeList.add(new CVRPTWNode(reader.getInt(), reader.getInt(), reader.getInt(), reader.getInt(),
                            reader.getInt() * scaling, reader.getInt() * scaling, reader.getInt() * scaling));
                }
            } catch (RuntimeException ignored) {
                // end of file
            }
            requestNode = nodeList.toArray(new CVRPTWNode[0]);
            nRequest = requestNode.length;
            // compute the distance matrix
            int n = nRequest + 2 * nVehicle;
            distMatrix = new int[n][n];
            for (int i = 0 ; i < n ; ++i) {
                CVRPTWNode from = i < nRequest ? requestNode[i] : depot;
                for (int j = 0 ; j < n ; ++j) {
                    CVRPTWNode to = j < nRequest ? requestNode[j] : depot;
                    distMatrix[i][j] = from.distance(to);
                }
            }
        }
        
    public record CVRPTWNode(int node, int x, int y, int demand, int twStart, int twEnd, int duration) {

        public int distance (CVRPTWNode o) {
            return (int) Math.floor(Math.sqrt((x - o.x) * (x - o.x) + (y - o.y) * (y - o.y)) * Instance.scaling);
        }

    }
        
    }
    
    
}
