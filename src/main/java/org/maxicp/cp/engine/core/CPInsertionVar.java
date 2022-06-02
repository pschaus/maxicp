package org.maxicp.cp.engine.core;


import org.maxicp.util.Procedure;

/**
 * This variable represents the set of nodes after which
 * this node can be inserted.
 *
 * An insertion point is defined by an integer i
 * and is supposed to be valid if this node can be inserted after node i.
 * Invalid insertion points are assumed to be removed by the constraints.
 * A {@link CPInsertionVar} can be empty domain and still be valid.
 * However, a constraint can be added to throw an inconsistency whenever an InsertionVar is empty.
 */
public interface CPInsertionVar {

    /**
     * Returns the solver in which this variable was created.
     *
     * @return the solver in which this variable was created.
     */
    CPSolver getSolver();

    /**
     * Tells if the variable is fixed, that is
     * it cannot be inserted after any other node.
     *
     * @return true when the no insertions points exist anymore.
     */
    boolean isFixed();

    /**
     * Removes an insertion point from the set of possible insertions points.
     *
     * @param i predecessor candidate for the beginning of the request.
     */
    void removeInsert(int i);

    /**
     * Removes all insertion points.
     */
    void removeAllInsert();

    /**
     * Removes all insertion points except the specified one.
     *
     * @param i predecessor for the beginning of the request.
     */
    void removeAllInsertBut(int i);

    /**
     * Tells if the insertion belongs to the set of insertions points.
     * @param i a node
     * @return
     */
    boolean contains(int i);

    /**
     * id of the node
     * @return id of the node
     */
    int node();

    /**
     * Copies the values of the insertions points into an array.
     * each entry of the array contains a valid predecessor
     *
     * @param dest an array large enough {@code dest.length >= size() && dest[0,...,size-1].length >= 2}
     * @return the size of the domain and {@code dest[0,...,size-1]} contains
     *         the values in the domain in an arbitrary order
     */
    int fillInsertion(int[] dest);

    /**
     * @return number of possible insertions points for the request
     */
    int size();

    /**
     * Asks that the closure is called whenever the domain
     * of this variable is reduced to a single setValue
     *
     * @param f the closure
     */
    void whenInsert(Procedure f);

    /**
     * Asks that {@link CPConstraint#propagate()} is called whenever the domain
     * of this variable is reduced to a singleton.
     * In such a state the variable is bind and we say that a <i>bind</i> event occurs.
     *
     * @param c the constraint for which the {@link CPConstraint#propagate()}
     *          method should be called on bind events of this variable.
     */
    void propagateOnInsert(CPConstraint c);

    /**
     * Asks that the closure is called whenever the domain change
     * of this variable changes
     *
     * @param f the closure
     */
    void whenDomainChange(Procedure f);

    /**
     * Asks that {@link CPConstraint#propagate()} is called whenever the domain
     * of this variable changes.
     * We say that a <i>change</i> event occurs.
     *
     * @param c the constraint for which the {@link CPConstraint#propagate()}
     *          method should be called on change events of this variable.
     */
    void propagateOnDomainChange(CPConstraint c);

    /**
     * Asks that the closure is called whenever the insertion point is excluded
     *
     * @param f the closure
     */
    void whenExclude(Procedure f);

    /**
     * Asks that {@link CPConstraint#propagate()} is called whenever the insertion point is excluded
     * We say that a <i>exclude</i> event occurs.
     *
     * @param c the constraint for which the {@link CPConstraint#propagate()}
     *          method should be called on change events of this variable.
     */
    void propagateOnExclude(CPConstraint c);

    /**
     * Asks that the closure is called whenever no inserted point remained for this variable
     * this occurs when the insertion variable is either inserted or excluded
     *
     * @param f the closure
     */
    void whenFixed(Procedure f);

    /**
     * Asks that {@link CPConstraint#propagate()} no inserted point remained for this variable
     * this occurs when the insertion variable is either inserted or excluded
     * In such a state the variable is bind and we say that a <i>bind</i> event occurs.
     *
     * @param c the constraint for which the {@link CPConstraint#propagate()}
     *          method should be called on bind events of this variable.
     */
    void propagateOnFix(CPConstraint c);

}
