package org.maxicp.model.examples;

import org.maxicp.cp.CPModelInstantiator;
import org.maxicp.cp.examples.TSP;
import org.maxicp.model.Factory;
import org.maxicp.model.IntVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.constraints.*;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.Procedure;
import org.maxicp.util.TimeIt;
import org.maxicp.util.io.InputReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;

import static org.maxicp.BranchingScheme.branch;

public class TSPTW {
    //1273.20
    public static void main(String[] args) {

        //new TSPTW("data/TSPTW/Langevin/N60ft410.dat");
        new TSPTW("data/TSPTW/OhlmannThomas/n150w120.001.txt");

    }

    ModelDispatcher baseModel = Factory.makeModelDispatcher();

    Instance instance;
    IntVar [] arrival;
    IntVar [] transition;
    IntVar [] arrivalPlusTransition;
    IntVar [] earliest;
    IntVar [] latest;
    IntVar [] x;
    IntVar makespan;

    TSPTW(String path) {

        instance = new Instance(path, 100).sort();

        System.out.println(instance);

        earliest = baseModel.intVarArray(instance.n, instance.horizon);

        latest = baseModel.intVarArray(instance.n, instance.horizon);

        arrival = baseModel.intVarArray(instance.n, instance.horizon);


        x = baseModel.intVarArray(instance.n, instance.n);
        arrivalPlusTransition = baseModel.intVarArray(instance.n-1, instance.horizon);


        baseModel.add(new AllDifferent(x));

        baseModel.add(new Equal(arrival[0],0));

        for (int i = 0; i < instance.n; i++) {
            baseModel.add(new Element1D(instance.E, x[i],earliest[i]));
            baseModel.add(new Element1D(instance.L, x[i],latest[i]));
            baseModel.add(new LessOrEqual(arrival[i],latest[i]));
            baseModel.add(new LessOrEqual(earliest[i],arrival[i]));
        }

        for (int i = 0; i < instance.n-1; i++) {

            IntVar transition = baseModel.element(instance.distMatrix,x[i],x[i+1]);
            // baseModel.add(new LessOrEqual(arrival[i].plus(transition[i]),arrival[i+1] ));

            baseModel.add(new Sum(new IntVar[]{arrival[i],transition},arrivalPlusTransition[i]));
            baseModel.add(new LessOrEqual(arrivalPlusTransition[i],arrival[i+1]));
        }
        IntVar makespan = arrival[instance.n-1];

        int [] maxDepth = new int[]{0};
        Supplier<Procedure[]> branching = () -> {
            int idx = -1; // index of the first variable that is not bound
            for (int k = 0; k < x.length; k++)
                if (x[k].size() > 1) {
                    idx=k;
                    break;
                }
            if (idx == -1)
                return new Procedure[0];
            else {
                int old = maxDepth[0];
                maxDepth[0] = Math.max(maxDepth[0],idx);
                if (maxDepth[0] > old) {
                    System.out.println(idx+"/"+ instance.n);
                }
                ArrayList<Procedure>  branches = new ArrayList<>();
                int window = 3;
                for (int i = Math.max(idx-window,0); i < idx+window; i++) {
                    final IntVar qi = x[idx];
                    int v = i;
                    branches.add(() -> baseModel.add(new Equal(qi,v)));
                }
                return branches.toArray(new Procedure[]{});
                /*
                IntVar qi = x[idx];
                int v = qi.min();
                Procedure left = () -> baseModel.add(new Equal(qi, v));
                Procedure right = () -> baseModel.add(new NotEqual(qi, v));
                return branch(left,right);
                */


            }
        };

        System.out.println("--- SIMPLE SOLVING");

        long time = TimeIt.run(() -> {
            baseModel.runAsConcrete(CPModelInstantiator.withTrailing, (cp) -> {
                DFSearch search = cp.dfSearch(branching);
                search.onSolution(() -> {
                    System.out.println(Arrays.toString(x));
                    for (int i = 0; i < instance.n; i++) {
                        System.out.print(x[i].min()+",");
                    }
                    System.out.println();
                    System.out.println(Arrays.toString(transition));
                    System.out.println("solution found makespan:"+makespan);
                });
                search.solve(s -> s.numberOfSolutions() >= 1);
                //search.optimize(cp.minimize(makespan));
            });
        });
        System.out.println("Time taken for simple resolution: " + (time/1000000000.));
    }


    static class Instance {

        int n;
        int [][] distMatrix;
        int [] E, L;
        int horizon = Integer.MIN_VALUE;

        Instance(String file, int scale) {
            InputReader reader = new InputReader(file);
            n = reader.getInt();
            distMatrix = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    distMatrix[i][j] = (int) (scale * reader.getDouble());
                }
            }
            E = new int[n];
            L = new int[n];

            for (int i = 0; i < n; i++) {
                E[i] = scale * reader.getInt();
                L[i] = scale * reader.getInt()-10;
                horizon = Math.max(horizon, L[i]+1);
            }

        }

        private Instance(int [][] distMatrix, int[] E, int [] L) {
            n = E.length;
            this.E = E;
            this.L = L;
            this.distMatrix = distMatrix;
            for (int i = 0; i < n; i++) {
                horizon = Math.max(horizon, L[i]+1);
            }
        }

        public Instance sort() {
            Integer [] perm = new Integer[n];
            for (int i = 0; i < n; i++) {
                perm[i] = i;
            }
            Arrays.sort(perm, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return L[o1]-L[o2];
                }
            });

            int [][] distMatrix_ = new int[n][n];
            int [] E_ = new int[n];
            int [] L_ = new int[n];

            for (int i = 0; i < n; i++) {
                E_[i] = E[perm[i]];
                L_[i] = L[perm[i]];
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    distMatrix_[i][j] = distMatrix[perm[i]][perm[j]];
                }
            }
            return new Instance(distMatrix_,E_, L_);
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


