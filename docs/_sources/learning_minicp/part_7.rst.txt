*****************************************************************
Part 7: Table Constraints
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======


`Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyqVAjb5pUWPUQnrzcZMosRe>`_

* `Table Constraints <https://www.icloud.com/keynote/0Nr2LcZGY2xQop312SgMGs37Q#07-table-constraints>`_

Theoretical questions
=====================

* `table <https://inginious.org/course/minicp/table>`_



Table Constraint
================

The table constraint (also called the extension constraint)
specifies the list of solutions (tuples) assignable to a vector of variables.

More precisely, given an array `X` containing `n` variables, and an array `T` of size `m*n`, this constraint holds:

.. math::

    \exists i: \forall j: T_{i,j} = X_j

That is, each row of the table is a valid assignment to `X`.

Here is an example of a table, with five tuples and four variables:

+-------------+------+------+------+------+
| Tuple index | X[0] | X[1] | X[2] | X[3] |
+=============+======+======+======+======+
|           1 |    0 |    1 |    2 |    3 |
+-------------+------+------+------+------+
|           2 |    0 |    0 |    3 |    2 |
+-------------+------+------+------+------+
|           3 |    2 |    1 |    0 |    3 |
+-------------+------+------+------+------+
|           4 |    3 |    2 |    1 |    2 |
+-------------+------+------+------+------+
|           5 |    3 |    0 |    1 |    1 |
+-------------+------+------+------+------+

In this particular example, the assignment `X = {2, 1, 0, 3}` is valid, but not `X = {4, 3, 3, 3}` as there is no
such line in the table.

Many algorithms exist for filtering table constraints.

One of the fastest filtering algorithms nowadays is Compact Table (CT) [CT2016]_.
In this exercise you'll implement a simple version of CT.

CT works in two steps:

1. Compute the list of supported tuples. A tuple `T[i]` is supported if, *for each* index `j` of the tuple, the domain of the variable `X[j]` contains the value `T[i][j]`.
2. Filter the domains. For each variable `X[j]` and value `v` in its domain, the value `v` can be removed if it is not used by any supported tuple.


Your task is to finish the implementation in
`TableCT.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/TableCT.java?at=master>`_.


`TableCT` maintains for each pair
variable/value the set of tuples the pair maintains as an array of bitsets:

.. code-block:: java

    private BitSet[][] supports;


where `supports[j][v]` is
the (bit)set of supported tuples for the assignment `X[j]=v`.

Example
-------

As an example, consider that variable `X[0]` has domain `{0, 1, 3}`. Here are some values for `supports`:
`supports[0][0] = {1, 2}`,
`supports[0][1] = {}`, and
`supports[0][3] = {4,5}`.

We can infer two things from this example. First, value `1` does not support any tuples, so it can be removed safely
from the domain of `X[0]`. Moreover, the tuples supported by `X[0]` form the union of the tuples supported by its values;
we immediately see that tuple `3` is not supported by `X[0]` and can be discarded from further calculations.

If we push the example further and we say that variable `X[2]` has domain `{0, 1}`, we immediately see that tuples `1`
and `2` are not supported by variable `X[2]`, and, as such, can be discarded. From this, we can infer that the value
`0` can be removed from the domain of variable `X[0]` as they don't support any tuples anymore.


Using bit sets
--------------

You may have assumed that the type of `supports` would have been `List<Integer>[][] supportedByVarVal`.
This is not the approach used by CT.

CT uses the concept of bit sets. A bit set is an array-like data structure that stores bits. Each bit is accessible by
its index. A bitset is in fact composed of an array of `Long`, which we call in this context a *word*.
Each of these words stores 64 bits from the bitset.

Using this structure is convenient for our goal:

* Each supported tuple is encoded as a `1` in the bitset, while `0` encodes unsupported tuples. In the traditional list/array
  representation, each supported tuple would have taken 32 bits to be represented.
* Computing the intersection and union of bit sets (and these are the main operations that will be made on `supportedByVarVal`)
  is very easy, thanks to the usage of bitwise operators included in all modern CPUs.

Java provides a default implementation of bit sets in the class BitSet, which we will use in this exercise.
We encourage you to read its documentation before going on.

A basic implementation
----------------------

You will implement a version of CT that makes no use of the reversible structure (therefore it is probably much less efficient that the real CT algorithm).

You have to implement the `propagate()` method of the class `TableCT`. All class variables have already been initialized
for you.

You "simply" have to compute, for each call to `propagate()`:

* The tuples supported by each variable, which are the union of the tuples supported by the value in the domain of the
  variable.
* The intersection of the tuples supported by each variable is the set of globally supported tuples.
* You can now intersect the set of globally supported tuples with each variable/value pair in `supports`.
  If the value supports no tuple (i.e., the intersection is empty), then it can be removed.

Make sure your implementation passes all the tests `TableTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/TableTest.java?at=master>`_.



.. [CT2016] Demeulenaere, J., Hartert, R., Lecoutre, C., Perez, G., Perron, L., Régin, J. C., & Schaus, P. (2016). Compact-table: Efficiently filtering table constraints with reversible sparse bit-sets. In International Conference on Principles and Practice of Constraint Programming (pp. 207-223). Springer.

Eternity Problem
======================

Fill in all the gaps in order to solve the Eternity II problem.

Your task is to finish the implementation in
`Eternity.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/Eternity.java?at=master>`_:

* Create the table.
* Model the problem using table constraints.
* Search for a feasible solution using branching combinators.



Compac- table algorithm for table constraints with short tuples
==================================================================

Implement `ShortTableCT.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/ShortTableCT.java?at=master>`_.


Of course you should get strong inspiration from the
`TableCT.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/TableCT.java?at=master>`_
implementation you did in a previous exercise.



Check that your implementation passes the tests `ShortTableTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/ShortTableTest.java?at=master>`_.


