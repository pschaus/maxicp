*****************************************************************
Part 4: Sum and Element Constraints
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======


`Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyrUvtxIwsgTQn2PZr55Bp2i>`_


* `Sum Constraint <https://www.icloud.com/keynote/0iQBg25tymcnxOtwCt8MVm76Q#04a-sum-constraint>`_
* `Element Constraint <https://www.icloud.com/keynote/0ySV4sz8KyQ7F0lvHvaTjwi-Q#04b-element-constraints>`_

Theoretical Questions
=====================

* `Element Constraints <https://inginious.org/course/minicp/element>`_


Element Constraint
=================================


Implement `Element1D.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Element1D.java?at=master>`_.


An element constraint is to index an array `T` by an index variable `x` and link the result with a variable `z`.
More exactly the relation `T[x]=z` must hold (where indexing starts from 0).

Assuming `T=[1,3,5,7,3]`, the constraint holds for

.. code-block:: java

    x = 1, z = 3
    x = 3, z = 7


but is violated for

.. code-block:: java

    x = 0, z = 2
    x = 3, z = 3


Check that your implementation passes the tests `Element1DTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/Element1DTest.java?at=master>`_.


Two possibilities:

1. Extend `Element2D` and reformulate `Element1D` as an `Element2D` constraint in a super call of the constructor.
2. Implement a dedicated algo (propagate, etc) for `Element1D` by taking inspiration from `Element2D`.

Does your filtering achieve domain consistency on D(z)? Implement a domain-consistent version, and write tests to make sure it is domain-consistent.


Element Constraint with Array of Variables
==================================================

Implement `Element1DVar.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Element1DVar.java?at=master>`_.


We have already seen the element constraint to index an array of integers `T` by an index variable `x` and link the result with a variable `z`: `T[x]=z`.
This time the constraint is more general since `T` is an array of variables.

We ask you to imagine and implement a filtering algorithm for the `Element1DVar` constraint.
This filtering algorithm is not trivial, at least if you want to do it efficiently.
Two directions of implementation are:

1. The domain-consistent version.
2. The hybrid domain-bound-consistent one, assuming the domain of `z` is a full range but not the domain of `x` in which you can create holes (you can start with this one, easier than the full domain-consistent one).


Check that your implementation passes the tests `Element1DVarTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/Element1DVarTest.java?at=master>`_.
Those tests are not checking that the filtering is domain-consistent. Write additional tests to check domain consistency.


The Stable Matching Problem
===========================

Complete the partial model `StableMatching.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/StableMatching.java?at=master>`_.
This model makes use of the `Element1DVar` constraint you have just implemented and is also a good example of manipulation of logical and reified constraints.
Check that you discover the 6 solutions to the provided instance.

