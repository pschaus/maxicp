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

package org.maxicp.cp.examples;

import org.maxicp.cp.engine.constraints.Circuit;
import org.maxicp.cp.engine.constraints.Element1DVar;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.maxicp.BranchingScheme.firstFail;
import static org.maxicp.cp.CPFactory.*;

/**
 * DARP
 * @author Pierre Schaus
 */
public class DARP {

    public static CPIntVar elementVar(CPIntVar[] array, CPIntVar y) {
        CPSolver cp = y.getSolver();
        int min = IntStream.range(0, array.length).map(i -> array[i].min()).min().getAsInt();
        int max = IntStream.range(0, array.length).map(i -> array[i].max()).max().getAsInt();
        CPIntVar z = makeIntVar(cp, min,max);
        cp.post(new Element1DVar(array, y, z));
        return z;
    }


    public static void main(String[] args) {

        // coordinates of the positions involved in this problem
        int [][] coords = new int[][] {
                {10,20},
                {30,30},
                {50,40},
                {40,10},
                {50,20},
                {25,30},
                {10,5}
        };

        // compute the symmetrical transition times as Euclidian distances
        int [][] transitions = new int[coords.length][coords.length];
        for (int i = 0; i < coords.length; i++) {
            for (int j = 0; j < coords.length; j++) {
                int x1 = coords[i][0];
                int y1 = coords[i][1];
                int x2 = coords[j][0];
                int y2 = coords[j][1];
                transitions[i][j] = (int) Math.sqrt(Math.pow(x1-x2,2.0)+Math.pow(y1-y2,2.0));
            }
            System.out.println(Arrays.toString(transitions[i]));
        }


        // the requests defined as {fromPos,toPos,deadline}
        int [][] requests = new int[][] {
                {3,5,120},
                {6,2,200},
                {5,1,100},
                {1,6,60},
                {5,2,150},
                {6,3,150},
        };

        // capacity of the vehicle
        int vehicleCapacity = 2;


        int n = requests.length;
        int [] positionIdx = new int[2*n+1];
        positionIdx[0] = 0; // departure at position index 0
        // [1..n+1] = pickups
        // [n+1..2n+1] = delivery
        for (int i = 0; i < requests.length; i++) {
            positionIdx[i+1] = requests[i][0];
            positionIdx[n+i+1] = requests[i][1];
        }


        CPSolver cp = makeSolver();


        CPIntVar[] succ = makeIntVarArray(cp, 2*n+1,2*n+1);
        CPIntVar[] pred = makeIntVarArray(cp, 2*n+1,2*n+1);
        CPIntVar[] time = makeIntVarArray(cp, 2*n+1,500);
        CPIntVar[] load = makeIntVarArray(cp, 2*n+1,vehicleCapacity+1);

        // departure time from the depot =  0
        cp.post(equal(time[0],0));

        // visit time update
        for (int i = 1; i < 2*n+1; i++) {
            // time[i] = time[pred[i]] + transition[pred[i]][i]
            CPIntVar tPredi = elementVar(time,pred[i]);
            CPIntVar posPredi = element(positionIdx,pred[i]);
            CPIntVar tt = element(transitions[positionIdx[i]],posPredi);
            cp.post(equal(time[i],sum(tPredi,tt)));
        }

        // initial load of the vehicle = 0
        cp.post(equal(load[0],0));


        // load update after a pickup (+1)
        for (int i = 1; i <= n; i++) {
            // load[i] = load[[pred[i]] + 1
            CPIntVar loadPred = elementVar(load, pred[i]);
            cp.post(equal(load[i], plus(loadPred, 1)));
        }

        // load update after a delivery (-1)
        for (int i = n+1; i <= 2*n; i++) {
            // load[i] = load[[pred[i]] - 1
            CPIntVar loadPred = elementVar(load, pred[i]);
            cp.post(equal(load[i], plus(loadPred, -1)));
        }


        cp.post(new Circuit(pred));

        // channeling between pred and succ vectors
        for (int i = 0; i < succ.length; i++) {
            // succ[pred[i]] == i
            cp.post(equal(elementVar(succ,pred[i]),i));
        }

        // precedence between pickup and delivery + deadlines
        for (int i = 0; i < n; i++) {
            // pickup before delivery)
            cp.post(lessOrEqual(time[i+1],time[n+i+1]));
            // delivery before the deadline
            cp.post(lessOrEqual(time[n+i+1],requests[i][2]));
        }



        DFSearch dfs = makeDfs(cp, firstFail(pred));

        dfs.onSolution(() -> {
            System.out.println("solution");
            System.out.println("succ:"+Arrays.toString(succ));
            System.out.println("pred:"+Arrays.toString(pred));
            System.out.println("time:"+Arrays.toString(time));
            System.out.println("load:"+Arrays.toString(load));

            int curr = 0;
            for (int i = 0; i < succ.length; i++) {
                System.out.println("visiting position:"+positionIdx[curr]+" at time:"+time[curr]+" load:"+load[curr]);
                curr = succ[curr].min();
            }

        });

        // search for the first feasible solution
        SearchStatistics stats = dfs.solve(statistics -> {
            return statistics.numberOfSolutions() > 0;
        });

        System.out.println(stats);



    }
}
