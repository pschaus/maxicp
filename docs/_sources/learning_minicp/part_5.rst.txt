*****************************************************************
Part 5: Circuit Constraint, TSP and LNS
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======


`Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyqwLy-d3Sc3y6shlNhnHLnG>`_


* `Circuit Constraint <https://www.icloud.com/keynote/085FmanDku6kwb-W78j_KgidQ#05a-circuit>`_
* `CP Branch and Bound Optimization and Large Neighborhood Search <https://www.icloud.com/keynote/0B3GvwWzrQQugkCyRkmlPlHIg#05b-optim-lns>`_

Theoretical Questions
=====================

* `Circuit <https://inginious.org/course/minicp/circuit>`_
* `LNS <https://inginious.org/course/minicp/lns>`_


Circuit Constraint
========================

The circuit constraint enforces a hamiltonian circuit on a successor array.
On the next example the successor array is `[2,4,1,5,3,0]`, where the indices of the array are the origins of the directed edges:

.. image:: ../_static/circuit.svg
    :scale: 50
    :width: 250
    :alt: Circuit


All the successors must be different.
But enforcing the `allDifferent` constraint is not enough.
We must also guarantee it forms a proper circuit (without sub-tours).
This can be done efficiently and incrementally by keeping track of the subchains
appearing during the search.
The data structure for the subchains should be reversible.
Our instance variables used to keep track of the subchains are:

.. code-block:: java

    IntVar [] x;
    ReversibleInt [] dest;
    ReversibleInt [] orig;
    ReversibleInt [] lengthToDest;



* `dest[i]` is the furthest node we can reach from node `i` following the instantiated edges.
* `orig[i]` is the furthest node we can reach from node `i` following the instantiated edges in reverse direction.
* `lengthToDest[i]` is the number of instantiated edges on the path from node `i` to `dest[i]`.

Consider the following example with instantiated edges colored in grey:

.. image:: ../_static/circuit-subtour.svg
    :scale: 50
    :width: 250
    :alt: Circuit

Before the addition of the green edge we have:

.. code-block:: java

    dest = [2,1,2,5,5,5];
    orig = [0,1,0,4,4,4];
    lengthToDest = [1,0,0,1,2,0];

After the addition of the green edge we have:

.. code-block:: java

    dest = [2,1,2,2,2,2];
    orig = [4,1,4,4,4,4];
    lengthToDest = [1,0,0,3,4,2];


In your implementation you must update the reversible integers to reflect
the changes after the addition of every new edge.
You can use the `CPIntVar.whenBind(...)` method for that.

The filtering in itself consists in preventing closing a
sub-tour that would have a length less than `n` (the number of nodes).
Since node 4 has a length to destination (node 2) of 4 (<6), the destination node 2 cannot have 4 as successor
and the red potential edge is deleted.
This filtering was introduced in [TSP1998]_ for solving the TSP with CP.


Implement `Circuit.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Circuit.java?at=master>`_.

Check that your implementation passes the tests `CircuitTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/CircuitTest.java?at=master>`_.


.. [TSP1998] Pesant, G., Gendreau, M., Potvin, J. Y., & Rousseau, J. M. (1998). An exact constraint logic programming algorithm for the traveling salesman problem with time windows. Transportation Science, 32(1), 12-29.




Custom Search for TSP
=================================

Modify `TSP.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/TSP.java?at=master>`_
to implement a custom search strategy.
A skeleton code is the following one:


.. code-block:: java

        DFSearch dfs = makeDfs(cp,
                selectMin(succ,
                        succi -> succi.getSize() > 1, // filter
                        succi -> succi.getSize(), // variable selector
                        succi -> {
                            int v = succi.getMin(); // value selector (TODO)
                            return branch(() -> equal(succi,v),
                                    () -> notEqual(succi,v));
                        }
                ));





* The unbound variable selected is one with smallest domain (first-fail).
* It is then assigned the minimum value in its domain.

This value selection strategy is not well suited for the TSP (and VRP in general).
The one you design should be more similar to the decision you would
make manually in a greedy fashion.
For instance, you can select as a successor for `succi`
a closest city in its domain.

Hint: Since there is no iterator on the domain of a variable, you can
iterate from the minimum value to the maximum one using a `for` loop
and checking if it is in the domain with the `contains` method.
You can also use your iterator from :ref:`Part 2: Domains, Variables, Constraints`.

You can also implement a min-regret variable selection strategy.
It selects a variable with the largest difference between a closest
successor city and a second-closest one.
The idea is that it is critical to decide the successor for this city first
because otherwise one will regret it the most.

Observe the first solution obtained to the provided instance and its objective value:
is it better than upon naive first-fail?
Also observe the time and number of backtracks necessary for proving optimality:
by how much did you reduce the computation time and backtracks?


LNS applied to TSP
=================================================================

Modify further `TSP.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/TSP.java?at=master>`_
to implement an LNS search.
Use the provided 17x17 distance matrix for this exercise.

What you should do:


* Record the assignment of the current best solution. Hint: use the `onSolution` call-back on the `DFSearch` object.
* Implement a restart strategy fixing randomly '10%' of the variables to their value in the current best solution.
* Each restart has a failure limit of 100 backtracks.

An example of LNS search is given in  `QAPLNS.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/QAPLNS.java?at=master>`_.
You can simply copy/paste/modify this implementation for the TSP:


* Does it converge faster to good solutions than the standard DFS? Use the larger instance with 25 facilities.
* What is the impact of the percentage of variables relaxed (experiment with 5, 10 and 20%)?
* What is the impact of the failure limit (experiment with 50, 100 and 1000)?
* Which parameter setting works best? How would you choose it?
* Imagine a different relaxation specific for this problem. Try to relax with higher probability the decision variables that have the strongest impact on the objective (the relaxed variables should still be somehow randomized). You can for instance compute for each facility i the quantity sum_j d[x[i]][x[j]]*w[i][j] and base your decision to relax or not a facility on those values.



From TSP to VRP
=================================================================

Create a new file called `VRP.java` working with the exact same distance matrix as the TSP but assuming
that there are now :math:`k` vehicles (make it a parameter and experiment with :math:`k=3` ).
The depot is the city at index `0`, and every other city must be visited exactly once by any of the :math:`k` vehicles:

* Variant1:  Minimize the total distance traveled by the three vehicles.
* Variant2 (more advanced): Minimize the longest distance traveled by the three vehicles (in order to be fair among the vehicle drivers).


You can also use LNS to speed up the search.
