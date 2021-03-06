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

package org.maxicp.cp;


import org.maxicp.BranchingScheme;
import org.maxicp.search.DFSearch;
import org.maxicp.search.Objective;
import org.maxicp.state.copy.Copier;
import org.maxicp.state.trail.Trailer;
import org.maxicp.util.exception.InconsistencyException;
import org.maxicp.util.Procedure;
import org.maxicp.util.exception.IntOverFlowException;
import org.maxicp.cp.engine.constraints.*;
import org.maxicp.cp.engine.core.*;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Factory to create {@link CPSolver}, {@link CPIntVar}, {@link CPConstraint}, {@link CPSequenceVar}
 * and some modeling utility methods.
 * Example for the n-queens problem:
 * <pre>
 * {@code
 *  Solver cp = Factory.makeSolver(false);
 *  IntVar[] q = Factory.makeIntVarArray(cp, n, n);
 *  for (int i = 0; i < n; i++)
 *    for (int j = i + 1; j < n; j++) {
 *      cp.post(Factory.notEqual(q[i], q[j]));
 *      cp.post(Factory.notEqual(q[i], q[j], j - i));
 *      cp.post(Factory.notEqual(q[i], q[j], i - j));
 *    }
 *  search.onSolution(() ->
 *    System.out.println("solution:" + Arrays.toString(q))
 *  );
 *  DFSearch search = Factory.makeDfs(cp,firstFail(q));
 *  SearchStatistics stats = search.solve();
 * }
 * </pre>
 */
public final class CPFactory {

    private CPFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a constraint programming solver
     * @return a constraint programming solver with trail-based memory management
     */
    public static CPSolver makeSolver() {
        return new MiniCP(new Trailer());
    }
    /**
     * Creates a constraint programming solver
     * @param byCopy a value that should be true to specify
     *               copy-based state management
     *               or falso for a trail-based memory management
     * @return a constraint programming solver
     */
    public static CPSolver makeSolver(boolean byCopy) {
        return new MiniCP(byCopy ? new Copier() : new Trailer());
    }

    /**
     * Creates a variable with a domain of specified arity.
     *
     * @param cp the solver in which the variable is created
     * @param sz a positive value that is the size of the domain
     * @return a variable with domain equal to the set {0,...,sz-1}
     */
    public static CPIntVar makeIntVar(CPSolver cp, int sz) {
        return new CPIntVarImpl(cp, sz);
    }

    /**
     * Creates a variable with a domain equal to the specified range.
     *
     * @param cp the solver in which the variable is created
     * @param min the lower bound of the domain (included)
     * @param max the upper bound of the domain (included) {@code max > min}
     * @return a variable with domain equal to the set {min,...,max}
     */
    public static CPIntVar makeIntVar(CPSolver cp, int min, int max) {
        return new CPIntVarImpl(cp, min, max);
    }

    /**
     * Creates a variable with a domain equal to the specified set of values.
     *
     * @param cp the solver in which the variable is created
     * @param values a set of values
     * @return a variable with domain equal to the set of values
     */
    public static CPIntVar makeIntVar(CPSolver cp, Set<Integer> values) {
        return new CPIntVarImpl(cp, values);
    }

    /**
     * Creates a boolean variable.
     *
     * @param cp the solver in which the variable is created
     * @return an uninstantiated boolean variable
     */
    public static CPBoolVar makeBoolVar(CPSolver cp) {
        return new CPBoolVarImpl(cp);
    }

    /**
     * Creates a sequence variable.
     *
     * @param cp the solver in which the variable is created
     * @return an uninstantiated boolean variable
     */
    public static CPSequenceVar makeSequenceVar(CPSolver cp, int nNodes, int begin, int end) {
        return new CPSequenceVarImpl(cp, nNodes, begin, end);
    }

    /**
     * Creates an array of variables with specified domain size.
     *
     * @param cp the solver in which the variables are created
     * @param n the number of variables to create
     * @param sz a positive value that is the size of the domain
     * @return an array of n variables, each with domain equal to the set {0,...,sz-1}
     */
    public static CPIntVar[] makeIntVarArray(CPSolver cp, int n, int sz) {
        return makeIntVarArray(n, i -> makeIntVar(cp, sz));
    }

    /**
     * Creates an array of variables with specified domain bounds.
     *
     * @param cp the solver in which the variables are created
     * @param n the number of variables to create
     * @param min the lower bound of the domain (included)
     * @param max the upper bound of the domain (included) {@code max > min}
     * @return an array of n variables each with a domain equal to the set {min,...,max}
     */
    public static CPIntVar[] makeIntVarArray(CPSolver cp, int n, int min, int max) {
        return makeIntVarArray(n, i -> makeIntVar(cp, min, max));
    }

    /**
     * Creates an array of variables with specified lambda function
     *
     * @param n the number of variables to create
     * @param body the function that given the index i in the array creates/map the corresponding {@link CPIntVar}
     * @return an array of n variables
     *         with variable at index <i>i</i> generated as {@code body.get(i)}
     */
    public static CPIntVar[] makeIntVarArray(int n, Function<Integer, CPIntVar> body) {
        CPIntVar[] t = new CPIntVar[n];
        for (int i = 0; i < n; i++)
            t[i] = body.apply(i);
        return t;
    }

    /**
     * Creates a Depth First Search with custom branching heuristic
     * <pre>
     * // Example of binary search: At each node it selects
     * // the first free variable qi from the array q,
     * // and creates two branches qi=v, qi!=v where v is the min value domain
     * {@code
     * DFSearch search = Factory.makeDfs(cp, () -> {
     *     IntVar qi = Arrays.stream(q).reduce(null, (a, b) -> b.size() > 1 && a == null ? b : a);
     *     if (qi == null) {
     *        return return EMPTY;
     *     } else {
     *        int v = qi.min();
     *        Procedure left = () -> equal(qi, v); // left branch
     *        Procedure right = () -> notEqual(qi, v); // right branch
     *        return branch(left, right);
     *     }
     * });
     * }
     * </pre>
     *
     * @param cp the solver that will be used for the search
     * @param branching a generator that is called at each node of the depth first search
     *                 tree to generate an array of {@link Procedure} objects
     *                 that will be used to commit to child nodes.
     *                 It should return {@link BranchingScheme#EMPTY} whenever the current state
     *                  is a solution.
     *
     * @return the depth first search object ready to execute with
     *         {@link DFSearch#solve()} or
     *         {@link DFSearch#optimize(Objective)}
     *         using the given branching scheme
     * @see BranchingScheme#firstFail(CPIntVar...)
     * @see BranchingScheme#branch(Procedure...)
     */
    public static DFSearch makeDfs(CPSolver cp, Supplier<Procedure[]> branching) {
        return new DFSearch(cp.getStateManager(), branching);
    }

    // -------------- constraints -----------------------

    /**
     * A variable that is a view of {@code x*a}.
     *
     * @param x a variable
     * @param a a constant to multiply x with
     * @return a variable that is a view of {@code x*a}
     */
    public static CPIntVar mul(CPIntVar x, int a) {
        if (a == 0) return makeIntVar(x.getSolver(), 0, 0);
        else if (a == 1) return x;
        else if (a < 0) {
            return minus(new CPIntVarViewMul(x, -a));
        } else {
            return new CPIntVarViewMul(x, a);
        }
    }

    /**
     * A variable that is a view of {@code -x}.
     *
     * @param x a variable
     * @return a variable that is a view of {@code -x}
     */
    public static CPIntVar minus(CPIntVar x) {
        return new CPIntVarViewOpposite(x);
    }

    /**
     *  A boolean variable that is a view of {@code !b}.
     *
     * @param b a boolean variable
     * @return a boolean variable that is a view of {@code !b}
     */
    public static CPBoolVar not(CPBoolVar b) {
        return new CPBoolVarImpl(plus(minus(b),1));
    }

    /**
     * A variable that is a view of {@code x+v}.
     *
     * @param x a variable
     * @param v a value
     * @return a variable that is a view of {@code x+v}
     */
    public static CPIntVar plus(CPIntVar x, int v) {
        return new CPIntVarViewOffset(x, v);
    }

    /**
     * A variable that is a view of {@code x-v}.
     *
     * @param x a variable
     * @param v a value
     * @return a variable that is a view of {@code x-v}
     */
    public static CPIntVar minus(CPIntVar x, int v) {
        return new CPIntVarViewOffset(x, -v);
    }

    /**
     * Computes a variable that is the absolute value of the given variable.
     * This relation is enforced by the {@link Absolute} constraint
     * posted by calling this method.
     *
     * @param x a variable
     * @return a variable that represents the absolute value of x
     */
    public static CPIntVar abs(CPIntVar x) {
        CPIntVar r = makeIntVar(x.getSolver(), 0, x.max());
        x.getSolver().post(new Absolute(x, r));
        return r;
    }

    /**
     * Computes a variable that is the maximum of a set of variables.
     * This relation is enforced by the {@link Maximum} constraint
     * posted by calling this method.
     *
     * @param x the variables on which to compute the maximum
     * @return a variable that represents the maximum on x
     * @see CPFactory#minimum(CPIntVar...)
     */
    public static CPIntVar maximum(CPIntVar... x) {
        CPSolver cp = x[0].getSolver();
        int min = Arrays.stream(x).mapToInt(CPIntVar::min).min().getAsInt();
        int max = Arrays.stream(x).mapToInt(CPIntVar::max).max().getAsInt();
        CPIntVar y = makeIntVar(cp, min, max);
        cp.post(new Maximum(x, y));
        return y;
    }

    /**
     * Computes a variable that is the minimum of a set of variables.
     * This relation is enforced by the {@link Maximum} constraint
     * posted by calling this method.
     *
     * @param x the variables on which to compute the minimum
     * @return a variable that represents the minimum on x
     * @see CPFactory#maximum(CPIntVar...) (IntVar...)
     */
    public static CPIntVar minimum(CPIntVar... x) {
        CPIntVar[] minusX = Arrays.stream(x).map(CPFactory::minus).toArray(CPIntVar[]::new);
        return minus(maximum(minusX));
    }

    /**
     * Returns a constraint imposing that the variable is
     * equal to some given value.
     *
     * @param x the variable to be assigned to v
     * @param v the value that must be assigned to x
     * @return a constraint so that {@code x = v}
     */
    public static CPConstraint equal(CPIntVar x, int v) {
        return new AbstractCPConstraint(x.getSolver()) {
            @Override
            public void post() {
                x.fix(v);
            }
        };
    }

    /**
     * Returns a constraint imposing that the variable is less or
     * equal to some given value.
     *
     * @param x the variable that is constrained bo be less or equal to v
     * @param v the value that must be the upper bound on x
     * @return a constraint so that {@code x <= v}
     */
    public static CPConstraint lessOrEqual(CPIntVar x, int v) {
        return new AbstractCPConstraint(x.getSolver()) {
            @Override
            public void post() {
                x.removeAbove(v);
            }
        };
    }

    /**
     * Returns a constraint imposing that the variable is larger or
     * equal to some given value.
     *
     * @param x the variable that is constrained bo be larger or equal to v
     * @param v the value that must be the lower bound on x
     * @return a constraint so that {@code x >= v}
     */
    public static CPConstraint largerOrEqual(CPIntVar x, int v) {
        return new AbstractCPConstraint(x.getSolver()) {
            @Override
            public void post() {
                x.removeBelow(v);
            }
        };
    }

    /**
     * Returns a constraint imposing that the variable is different
     * from some given value.
     *
     * @param x the variable that is constrained bo be different from v
     * @param v the value that must be different from x
     * @return a constraint so that {@code x != y}
     */
    public static CPConstraint notEqual(CPIntVar x, int v) {
        return new AbstractCPConstraint(x.getSolver()) {
            @Override
            public void post() {
                x.remove(v);
            }
        };
    }

    /**
     * Returns a constraint imposing that the two different variables
     * must take different values.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x != y}
     */
    public static CPConstraint notEqual(CPIntVar x, CPIntVar y) {
        return new NotEqual(x, y);
    }


    /**
     * Returns a constraint imposing that the two different variables
     * must take the value.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x = y}
     */
    public static CPConstraint equal(CPIntVar x, CPIntVar y) {
        return new Equal(x, y);
    }

    /**
     * Returns a constraint imposing that the
     * the first variable differs from the second
     * one minus a constant value.
     *
     * @param x a variable
     * @param y a variable
     * @param c a constant
     * @return a constraint so that {@code x != y+c}
     */
    public static CPConstraint notEqual(CPIntVar x, CPIntVar y, int c) {
        return new NotEqual(x, y, c);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is equal to the given constant.
     * This relation is enforced by the {@link IsEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if x takes the value c
     * @see IsEqual
     */
    public static CPBoolVar isEqual(CPIntVar x, final int c) {
        CPBoolVar b = makeBoolVar(x.getSolver());
        CPSolver cp = x.getSolver();
        try {
            cp.post(new IsEqual(b, x, c));
        } catch (InconsistencyException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is less or equal to the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     *         x takes a value less or equal to c
     */
    public static CPBoolVar isLessOrEqual(CPIntVar x, final int c) {
        CPBoolVar b = makeBoolVar(x.getSolver());
        CPSolver cp = x.getSolver();
        cp.post(new IsLessOrEqual(b, x, c));
        return b;
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is less than the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     *         x takes a value less than c
     */
    public static CPBoolVar isLess(CPIntVar x, final int c) {
        return isLessOrEqual(x, c - 1);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is larger or equal to the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     *         x takes a value larger or equal to c
     */
    public static CPBoolVar isLargerOrEqual(CPIntVar x, final int c) {
        return isLessOrEqual(minus(x), -c);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is larger than the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     *         x takes a value larger than c
     */
    public static CPBoolVar isLarger(CPIntVar x, final int c) {
        return isLargerOrEqual(x, c + 1);
    }

    /**
     * Returns a constraint imposing that the
     * a first variable is less or equal to a second one.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x <= y}
     */
    public static CPConstraint lessOrEqual(CPIntVar x, CPIntVar y) {
        return new LessOrEqual(x, y);
    }

    /**
     * Returns a constraint imposing that the
     * a first variable is larger or equal to a second one.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x >= y}
     */
    public static CPConstraint largerOrEqual(CPIntVar x, CPIntVar y) {
        return new LessOrEqual(y, x);
    }

    /**
     * Returns a variable representing
     * the value in an array at the position
     * specified by the given index variable
     * This relation is enforced by the {@link Element1D} constraint
     * posted by calling this method.
     *
     * @param array the array of values
     * @param y the variable
     * @return a variable equal to {@code array[y]}
     */
    public static CPIntVar element(int[] array, CPIntVar y) {
        CPSolver cp = y.getSolver();
        CPIntVar z = makeIntVar(cp, IntStream.of(array).min().getAsInt(), IntStream.of(array).max().getAsInt());
        cp.post(new Element1D(array, y, z));
        return z;
    }

    /**
     * Returns a variable representing
     * the value in a matrix at the position
     * specified by the two given row and column index variables
     * This relation is enforced by the {@link Element2D} constraint
     * posted by calling this method.
     *
     * @param matrix the n x m 2D array of values
     * @param x the row variable with domain included in 0..n-1
     * @param y the column variable with domain included in 0..m-1
     * @return a variable equal to {@code matrix[x][y]}
     */
    public static CPIntVar element(int[][] matrix, CPIntVar x, CPIntVar y) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                min = Math.min(min, matrix[i][j]);
                max = Math.max(max, matrix[i][j]);
            }
        }
        CPIntVar z = makeIntVar(x.getSolver(), min, max);
        x.getSolver().post(new Element2D(matrix, x, y, z));
        return z;
    }

    /**
     * Returns a variable representing
     * the sum of a given set of variables.
     * This relation is enforced by the {@link Sum} constraint
     * posted by calling this method.
     *
     * @param x the n variables to sum
     * @return a variable equal to {@code x[0]+x[1]+...+x[n-1]}
     */
    public static CPIntVar sum(CPIntVar... x) {
        long sumMin = 0;
        long sumMax = 0;
        for (int i = 0; i < x.length; i++) {
            sumMin += x[i].min();
            sumMax += x[i].max();
        }
        if (sumMin < (long) Integer.MIN_VALUE || sumMax > (long) Integer.MAX_VALUE) {
            throw new IntOverFlowException("domains are too large for sum constraint and would exceed Integer bounds");
        }
        CPSolver cp = x[0].getSolver();
        CPIntVar s = makeIntVar(cp, (int) sumMin, (int) sumMax);
        cp.post(new Sum(x, s));
        return s;
    }

    /**
     * Returns a sum constraint.
     *
     * @param x an array of variables
     * @param y a variable
     * @return a constraint so that {@code y = x[0]+x[1]+...+x[n-1]}
     */
    public static CPConstraint sum(CPIntVar[] x, CPIntVar y) {
        return new Sum(x, y);
    }

    /**
     * Returns a sum constraint.
     *
     * @param x an array of variables
     * @param y a constant
     * @return a constraint so that {@code y = x[0]+x[1]+...+x[n-1]}
     */
    public static CPConstraint sum(CPIntVar[] x, int y) {
        return new Sum(x, y);
    }

    /**
     * Returns a sum constraint.
     * <p>
     * Uses a _parameter pack_ to automatically bundle a list of IntVar as an array
     *
     * @param y the target value for the sum (a constant)
     * @param x a parameter pack of IntVar representing an array of variables
     * @return a constraint so that {@code y = x[0] + ... + x[n-1]}
     */
    public static CPConstraint sum(int y, CPIntVar... x) {
        return new Sum(x, y);
    }

    /**
     * Returns a binary decomposition of the allDifferent constraint.
     *
     * @param x an array of variables
     * @return a constraint so that {@code x[i] != x[j] for all i < j}
     */
    public static CPConstraint allDifferent(CPIntVar[] x) {
        return new AllDifferentBinary(x);
    }

    /**
     * Returns an allDifferent constraint that enforces
     * global arc consistency.
     *
     * @param x an array of variables
     * @return a constraint so that {@code x[i] != x[j] for all i < j}
     */
    public static CPConstraint allDifferentAC(CPIntVar[] x) {
        return new AllDifferentDC(x);
    }
}
