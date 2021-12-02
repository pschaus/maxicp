package org.maxicp.model.examples;

import org.maxicp.cp.CPModelInstantiator;
import org.maxicp.cp.examples.TSP;
import org.maxicp.model.Factory;
import org.maxicp.model.IntVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.constraints.*;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.util.Procedure;
import org.maxicp.util.TimeIt;
import org.maxicp.util.io.InputReader;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

import static org.maxicp.BranchingScheme.branch;

public class TSPTW {

    public static void main(String[] args) {
        new TSPTW("data/TSPTW/SolomonPotvinBengio/rc_201.1.txt");
    }

    ModelDispatcher baseModel = Factory.makeModelDispatcher();

    Instance instance;
    IntVar [] arrival;
    IntVar [] transition;
    IntVar [] arrivalPlusTransition;
    IntVar [] earliest;
    IntVar [] latest;
    IntVar [] x;
    IntVar totalTime;

    TSPTW(String path) {

        instance = new Instance(path, 100);

        System.out.println(instance);

        earliest = baseModel.intVarArray(instance.n, instance.horizon);
        latest = baseModel.intVarArray(instance.n, instance.horizon);

        arrival = baseModel.intVarArray(instance.n, instance.horizon);


        x = baseModel.intVarArray(instance.n, instance.n);
        transition = baseModel.intVarArray(instance.n-1, instance.horizon);
        arrivalPlusTransition = baseModel.intVarArray(instance.n-1, instance.horizon);
        totalTime = baseModel.intVar(0, instance.horizon);

        totalTime = baseModel.intVar(0, instance.horizon);

        baseModel.add(new AllDifferent(x));

        baseModel.add(new Equal(arrival[0],0));

        for (int i = 0; i < instance.n; i++) {
            baseModel.add(new Element1D(instance.E, x[i],earliest[i]));
            baseModel.add(new Element1D(instance.L, x[i],latest[i]));
            baseModel.add(new LessOrEqual(arrival[i],latest[i]));
            baseModel.add(new LessOrEqual(earliest[i],arrival[i]));
        }

        for (int i = 0; i < instance.n-1; i++) {
            baseModel.add(new Element2D(instance.distMatrix,x[i],x[i+1],transition[i]));
            baseModel.add(new Sum(new IntVar[]{arrival[i],transition[i]},arrivalPlusTransition[i]));
            baseModel.add(new LessOrEqual(arrivalPlusTransition[i],arrival[i+1]));
        }
        baseModel.add(new Sum(transition,totalTime));


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
                IntVar qi = x[idx];
                int v = qi.min();
                Procedure left = () -> baseModel.add(new Equal(qi, v));
                Procedure right = () -> baseModel.add(new NotEqual(qi, v));
                return branch(left,right);
            }
        };

        System.out.println("--- SIMPLE SOLVING");
        long time = TimeIt.run(() -> {
            baseModel.runAsConcrete(CPModelInstantiator.withTrailing, (cp) -> {
                DFSearch search = cp.dfSearch(branching);
                search.onSolution(() -> {
                    System.out.println(Arrays.toString(x));
                    System.out.println(Arrays.toString(transition));
                    System.out.println("solution found objective:"+totalTime);
                });
                search.optimize(cp.minimize(totalTime));
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
                L[i] = scale * reader.getInt();
                horizon = Math.max(horizon, L[i]+1);
            }
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


