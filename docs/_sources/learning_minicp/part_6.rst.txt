*****************************************************************
Part 6: AllDifferent Constraint
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======


`Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyrExrxGKIuE5QixGhoMugKw>`_

* `AllDifferent Constraint <https://www.icloud.com/keynote/0dCFUILn1rSOatVpn4t0pVGxg#06-alldifferent>`_

Theoretical questions
=====================

* `AllDifferent <https://inginious.org/course/minicp/alldifferent>`_



Forward-checking filtering
=========================================

Implement a dedicated propagator `AllDifferentFW.java` for the all-different constraint that does the same filtering
as `AllDifferentACBinary.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/AllDifferentBinary.java?at=master>`_,
but avoid iteration over bound variables when removing a value.
Implement the sparse-set trick, as in `Sum.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Sum.java?at=master>`_.
Experiment with the 15-Queens problem instance. How much speed-up do you observe for finding all the solutions?

Domain-consistent filtering
===================================

The objective is to implement the filtering algorithm described in  [REGIN94]_
to remove every impossible value for the `AllDifferent` constraint (this is called generalized arc consistency and is also known as domain consistency).
More precisely, you must:

* Implement the constraint `AllDifferentDC.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/AllDifferentDC.java?at=master>`_.
* Test your implementation in `AllDifferentDCTest.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/AllDifferentDCTest.java?at=master>`_


Régin's algorithm proceeds in four steps described in the following figure:

.. image:: ../_static/alldifferent.png
    :scale: 70
    :alt: profile

1. It computes an initial maximum matching in the variable-value graph for the consistency test.
2. It builds a directed graph: matched edges go from right to left, and unmatched edges go from left to right. There is also a dummy node
   with incoming edges from unmatched value nodes, and outgoing edges toward matched value nodes.
3. It computes the strongly connected components.
4. Any edge that is not in the initial maximum matching and connects two nodes from different components is removed.

The two main algorithmic building blocks are provided:

* `MaximumMatching.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/MaximumMatching.java?at=master>`_
  is a class that computes a maximum matching given an array of variables. Instantiate this class once and for all in the constructor and
  then you can simply call `compute` in the `propagate` method.
* `GraphUtil.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/util/GraphUtil.java?at=master>`_
  contains a static method with signature `public static int[] stronglyConnectedComponents(Graph graph)` to compute the strongly connected
  components. The returned array gives for each node its connected component id.

One of the main difficulties of this exercise is to implement the `Graph` interface
to represent the residual graph of the maximum matching:

.. code-block:: java

    public static interface Graph {
        /* the number of nodes in this graph */
        int n();

        /* incoming nodes ids incident to node idx */
        Iterable<Integer> in(int idx);

        /* outgoing nodes ids incident to node idx */
        Iterable<Integer> out(int idx);
    }

It uses an adjacency list that is updated in the method `updateGraph()`.
We advise you to use a dense representation with node ids as illustrated on the black nodes of the example (step2: directed graph).


Once your code passes the tests, you can experiment your new constraint on all the models you have seen so far
to measure the pruning gain on the number of nodes (NQueens, TSP, QAP, etc).

.. [REGIN94] Régin, J.-C. (1994). A filtering algorithm for constraints of difference in CSPs, AAAI-94.
