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

import org.maxicp.cp.engine.constraints.Disjunctive;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.BranchingScheme;
import org.maxicp.cp.CPFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * The JobShop Problem.
 * <a href="https://en.wikipedia.org/wiki/Job_shop_scheduling">Wikipedia.</a>
 */
public class JobShop {

    public static CPIntVar[] flatten(CPIntVar[][] x) {
        return Arrays.stream(x).flatMap(Arrays::stream).toArray(CPIntVar[]::new);
    }

    public static void main(String[] args) {

        // Reading the data

        try {
            FileInputStream istream = new FileInputStream("data/jobshop/sascha/jobshop-7-7-4");
            BufferedReader in = new BufferedReader(new InputStreamReader(istream));
            in.readLine();
            in.readLine();
            in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(in.readLine());
            int nJobs = Integer.parseInt(tokenizer.nextToken());
            int nMachines = Integer.parseInt(tokenizer.nextToken());

            System.out.println(nJobs + " " + nMachines);
            int[][] duration = new int[nJobs][nMachines];
            int[][] machine = new int[nJobs][nMachines];
            int horizon = 0;
            for (int i = 0; i < nJobs; i++) {
                tokenizer = new StringTokenizer(in.readLine());
                for (int j = 0; j < nMachines; j++) {
                    machine[i][j] = Integer.parseInt(tokenizer.nextToken());
                    duration[i][j] = Integer.parseInt(tokenizer.nextToken());
                    horizon += duration[i][j];
                }
            }

            CPSolver cp = CPFactory.makeSolver();

            CPIntVar[][] start = new CPIntVar[nJobs][nMachines];
            CPIntVar[][] end = new CPIntVar[nJobs][nMachines];
            ArrayList<CPIntVar>[] startOnMachine = new ArrayList[nMachines];
            ArrayList<Integer>[] durationsOnMachine = new ArrayList[nMachines];
            for (int m = 0; m < nMachines; m++) {
                startOnMachine[m] = new ArrayList<CPIntVar>();
                durationsOnMachine[m] = new ArrayList<Integer>();
            }

            CPIntVar[] endLast = new CPIntVar[nJobs];
            for (int i = 0; i < nJobs; i++) {

                for (int j = 0; j < nMachines; j++) {

                    start[i][j] = CPFactory.makeIntVar(cp, 0, horizon);
                    end[i][j] = CPFactory.plus(start[i][j], duration[i][j]);
                    int m = machine[i][j];
                    startOnMachine[m].add(start[i][j]);
                    durationsOnMachine[m].add(duration[i][j]);

                    if (j > 0) {
                        // precedence constraint
                        cp.post(CPFactory.lessOrEqual(end[i][j - 1], start[i][j]));
                    }
                }
                endLast[i] = end[i][nMachines - 1];
            }


            for (int m = 0; m < nMachines; m++) {

                int[] durations = new int[nJobs];
                for (int i = 0; i < nJobs; i++) {
                    durations[i] = durationsOnMachine[m].get(i);
                }
                CPIntVar[] starts = startOnMachine[m].toArray(new CPIntVar[]{});
                cp.post(new Disjunctive(starts, durations));
            }

            CPIntVar makespan = CPFactory.maximum(endLast);


            Objective obj = cp.minimize(makespan);

            DFSearch dfs = CPFactory.makeDfs(cp, BranchingScheme.firstFail(flatten(start)));


            dfs.onSolution(() ->
                    System.out.println("makespan:" + makespan)
            );

            SearchStatistics stats = dfs.optimize(obj);

            System.out.format("Statistics: %s\n", stats);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistencyException e) {
            e.printStackTrace();
        }


    }
}
