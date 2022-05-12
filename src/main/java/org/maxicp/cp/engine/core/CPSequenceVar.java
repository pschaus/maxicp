package org.maxicp.cp.engine.core;

import org.maxicp.util.Procedure;

/**
 * decision variable used to represent a sequence of nodes
 * the nodes are split into 3 categories:
 *  - scheduled nodes, which are part of the sequence and ordered
 *  - possible nodes, which could be part of the sequence
 *  - excluded nodes, which cannot be part of the sequence
 */
public interface CPSequenceVar extends CPVar {

    /**
     * Returns the solver in which this variable was created.
     * @return the solver in which this variable was created
     */
    CPSolver getSolver();

    /**
     * return the first node of the sequence
     * @return first node of the sequence
     */
    int begin();

    /**
     * return the last node of the sequence
     * @return last node of the sequence
     */
    int end();

    /**
     * @return number of scheduled nodes in the sequence, including the {@link #begin()} and {@link #end()} nodes
     */
    int nMember();

    /**
     * @param includeBounds whether to count the {@link #begin()} and {@link #end()} nodes or not
     * @return number of scheduled nodes
     */
    int nMember(boolean includeBounds);

    /**
     * @return number of possible nodes in the sequence
     */
    int nPossible();

    /**
     * @return number of excluded nodes in the sequence
     */
    int nExcluded();

    /**
     * @return number of nodes in the sequence, including the {@link #begin()} and {@link #end()} nodes
     */
    int nNode();

    /**
     * @param includeBounds whether to count the {@link #begin()} and {@link #end()} nodes or not
     * @return number of nodes in the sequence
     */
    int nNode(boolean includeBounds);

    /**
     * set the node into the sequence, right after a predecessor. Fails if the node is in the excluded set, no effect
     * if it is already in the scheduled set
     * @param pred predecessor for the node
     * @param node node to schedule
     */
    void insert(int pred, int node);

    /**
     * tell if a node can be scheduled with a given predecessor
     * @param pred predecessor for the node
     * @param node node trying to be scheduled
     * @return true if the node can be scheduled
     */
    boolean canInsert(int pred, int node);

    /**
     * tell if a node can precede another one
     * a node p can precede a node n if a sequence can be formed where p occurs before n in the order
     * @param pred predecessor for the node
     * @param node node
     * @return true if a sequence can be formed with pred preceding node
     */
    boolean canPrecede(int pred, int node);

    /**
     * exclude the node from the set of possible nodes. Fails if the node is in the scheduled set, no effect if it is
     * already in the excluded set
     * @param node node to exclude
     */
    void exclude(int node);

    /**
     * exclude all possible nodes from the sequence, binding the variable
     */
    void excludeAllPossible();

    /**
     * tell if a node is scheduled
     * @param node node whose state needs to be known
     * @return true if the node is scheduled
     */
    boolean isMember(int node);

    /**
     * tell if a node is possible
     * @param node node whose state needs to be known
     * @return true if the node is possible
     */
    boolean isPossible(int node);

    /**
     * tell if a node is excluded
     * @param node node whose state needs to be known
     * @return true if the node is excluded
     */
    boolean isExcluded(int node);

    /**
     * Copies the scheduled values of the domain into an array.
     * Always contain the {@link #begin()} and {@link #end()} nodes
     *
     * @param dest an array large enough {@code dest.length >= nScheduledNode(true)}
     * @return the size of the scheduled domain and {@code dest[0,...,size-1]} contains
     *         the values in the scheduled domain in an arbitrary order
     */
    int fillMember(int[] dest);

    /**
     * Copies the possible values of the domain into an array.
     *
     * @param dest an array large enough {@code dest.length >= nPossible()}
     * @return the size of the possible domain and {@code dest[0,...,size-1]} contains
     *         the values in the possible domain in an arbitrary order
     */
    int fillPossible(int[] dest);

    /**
     * Copies the excluded values of the domain into an array.
     *
     * @param dest an array large enough {@code dest.length >= nExcluded()}
     * @return the size of the excluded domain and {@code dest[0,...,size-1]} contains
     *         the values in the excluded domain in an arbitrary order
     */
    int fillExcluded(int[] dest);

    /**
     * Copies the scheduled insertions values of the domain into an array.
     *
     * @param node node whose scheduled insertions (predecessor candidate) needs to be known
     * @param dest an array large enough {@code dest.length >= nScheduledInsertions(node)}
     * @return the size of the scheduled insertion domain and {@code dest[0,...,size-1]} contains
     *         the values in the scheduled insertion domain in an arbitrary order
     */
    int fillMemberInsertion(int node, int[] dest);

    /**
     * Copies the possible insertions values of the domain into an array.
     *
     * @param node node whose possible insertions (predecessor candidate) needs to be known
     * @param dest an array large enough {@code dest.length >= nPossibleInsertions(node)}
     * @return the size of the possible insertion domain and {@code dest[0,...,size-1]} contains
     *         the values in the possible insertion domain in an arbitrary order
     */
    int fillPossibleInsertion(int node, int[] dest);

    /**
     * give the number of possible insertions for a node
     * @param node node whose number of possible insertions needs to be known
     * @return number of possible insertions for a node
     */
    int nPossibleInsertion(int node);

    /**
     * give the number of scheduled insertions for a node
     * @param node node whose number of scheduled insertions needs to be known
     * @return number of scheduled insertions for a node
     */
    int nMemberInsertion(int node);

    /**
     * give the total number of insertions for a node
     * @param node node whose number of insertions needs to be known
     * @return number of insertions for a node {@code nInsertions(n) == nPossibleInsertions(n) + nScheduledInsertions(n)}
     */
    int nInsertion(int node);

    /**
     * Copies the insertions values of the domain into an array.
     *
     * @param node node whose insertions (predecessor candidate) needs to be known
     * @param dest an array large enough {@code dest.length >= nInsertions(node)}
     * @return the size of the insertion domain and {@code dest[0,...,size-1]} contains
     *         the values in the insertion domain in an arbitrary order
     */
    int fillInsertion(int node, int[] dest);

    /**
     * tell if a node is an insertion (possible predecessor) of another node
     * @param pred insertion candidate
     * @param node node that could be inserted after pred
     * @return true if node can be inserted after pred
     */
    boolean isInsertion(int pred, int node);

    /**
     * remove a node from the set of insertions of another node
     * @param pred insertion that will be removed
     * @param node node who will lose the insertion
     */
    void removeInsertion(int pred, int node);

    /**
     * remove all insertion having the specified node as predecessor
     * @param node node after which no insertion can occur
     */
    void removeInsertionAfter(int node);

    /**
     * gives the {@link CPInsertionVar} related to a node in the sequence
     * @param i id of the InsertionVar
     * @return InsertionVar with id i
     */
    CPInsertionVar getInsertionVar(int i);

    /**
     * Asks that the closure is called whenever the domain
     * of this variable is reduced to a single setValue
     *
     * @param f the closure
     */
    void whenFix(Procedure f);

    /**
     * Asks that the closure is called whenever
     * a new node is scheduled into the sequence
     *
     * @param f the closure
     */
    void whenInsert(Procedure f);

    /**
     * Asks that the closure is called whenever
     * a new node is excluded from the sequence
     *
     * @param f the closure
     */
    void whenExclude(Procedure f);

    /**
     * Asks that {@link CPConstraint#propagate()} is called whenever the domain
     * of this variable is reduced to a singleton.
     * In such a state the variable is bind and we say that a <i>bind</i> event occurs.
     *
     * @param c the constraint for which the {@link CPConstraint#propagate()}
     *          method should be called on bind events of this variable.
     */
    void propagateOnFix(CPConstraint c);

    /**
     * Asks that {@link CPConstraint#propagate()} is called whenever
     * a new node is scheduled into the sequence
     * We say that a <i>bound change</i> event occurs in this case.
     *
     * @param c the constraint for which the {@link CPConstraint#propagate()}
     *          method should be called on bound change events of this variable.
     */
    void propagateOnInsert(CPConstraint c);

    /**
     * Asks that {@link CPConstraint#propagate()} is called whenever
     * a new node is excluded from the sequence
     * We say that a <i>bound change</i> event occurs in this case.
     *
     * @param c the constraint for which the {@link CPConstraint#propagate()}
     *          method should be called on bound change events of this variable.
     */
    void propagateOnExclude(CPConstraint c);

    /**
     * @return true when no more node belongs to the set of possible nodes
     */
    boolean isFix();

    /**
     * @param node node in the scheduled sequence
     * @return index of the successor of the node. Irrelevant if the node is not in the sequence
     */
    int nextMember(int node);

    /**
     * @param node node in the scheduled sequence
     * @return index of the predecessor of the node. Irrelevant if the node is not in the sequence
     */
    int predMember(int node);

    /**
     * fill the current order of the sequence into an array large enough, including {@link #begin()} and {@link #end()}
     * node
     * @param dest array where to store the order of the sequence
     * @return number of elements in the sequence, including beginning and ending node
     */
    int fillOrder(int[] dest);

    /**
     * fill the current order of the sequence into an array large enough
     * @param dest array where to store the order of the sequence
     * @param includeBounds if True, includes the beginning and ending node into the order array
     * @return number of elements in the sequence
     */
    int fillOrder(int[] dest, boolean includeBounds);

    /**
     * give the ordering of nodes with the {@link #begin()} and {@link #end()} nodes
     * @return ordering of the sequence, with " -> " between 2 consecutive nodes, incudling the {@link #begin()} and
     *  {@link #end()} nodes
     */
    String ordering();

    /**
     * give the ordering of nodes with possibly the {@link #begin()} and {@link #end()} nodes
     * @param includeBounds if the bounds ({@link #begin()} and {@link #end()}) must be included or not
     * @return ordering of the sequence, with " -> " between 2 consecutive nodes
     */
    String ordering(boolean includeBounds);

    /**
     * give the ordering of nodes with the beginning and end nodes
     * @param includeBounds if the bounds ({@link #begin()} and {@link #end()}) must be included or not
     * @param join string that must be used to join two consecutive nodes
     * @return ordering of the sequence, nodes being joined on the specified string
     */
    String ordering(boolean includeBounds, String join);

    /**
     * tell if pred occurs before node in the sequence
     * @param pred node occuring before
     * @param node node occuring after
     * @return true if pred precedes node in the sequence
     */
    boolean precede(int pred, int node);

}
