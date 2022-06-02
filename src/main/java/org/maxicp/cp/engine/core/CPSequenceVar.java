package org.maxicp.cp.engine.core;

import org.maxicp.util.Procedure;

/**
 * Decision variable used to represent a sequence of nodes
 * As an invariant of the domain, the nodes are partitioned into 3 categories:
 *  - member ones, which are part of the sequence and ordered
 *  - possible ones, which could be part of the sequence
 *  - excluded ones, which cannot be part of the sequence
 *
 * The constraints pruning a {@link CPSequenceVar}
 * does it by remove insertions points from
 * the {@link CPInsertionVar} contained within the sequence or
 * by can exclude them.
 */
public interface CPSequenceVar extends CPVar {

    /**
     * Returns the solver in which this variable was created.
     *
     * @return the solver in which this variable was created.
     */
    CPSolver getSolver();

    /**
     * Returns the first node of the sequence.
     *
     * @return first node of the sequence.
     */
    int begin();

    /**
     * Return the last node of the sequence
     *
     * @return last node of the sequence
     */
    int end();

    /**
     * Returns the number of elements already in the partial sequence
     *
     * @return number of scheduled nodes in the sequence, including the {@link #begin()} and {@link #end()} nodes
     */
    int nMember();

    /**
     * Returns the number of elements already in the partial sequence, possibly excluding the
     * the count of {@link #begin()} and {@link #end()}.
     *
     * @param includeBounds whether to count the {@link #begin()} and {@link #end()} nodes or not
     * @return number of scheduled nodes
     */
    int nMember(boolean includeBounds);

    /**
     * Returns the number of possible elements that can be added to the partial sequence.
     *
     * @return number of possible nodes in the sequence
     */
    int nPossible();

    /**
     * Returns the number of excluded nudes in the sequence.
     *
     * @return number of excluded nodes in the sequence
     */
    int nExcluded();

    /**
     * Returns the number of nodes in the sequence.
     *
     * @return number of nodes in the sequence,
     *         including the {@link #begin()} and {@link #end()} nodes
     */
    int nNode();

    /**
     * Returns the number of nodes in the sequence, possibly excluding bounds.
     *
     * @param includeBounds whether to count the {@link #begin()} and {@link #end()} nodes or not.
     * @return number of nodes in the sequence.
     */
    int nNode(boolean includeBounds);

    /**
     * Inserts the node into the sequence, right after the given predecessor.
     *
     * @param pred predecessor for the node, a member of the sequence
     * @param node node to insert, a possible node
     * @throws org.maxicp.util.exception.InconsistencyException if {@code pred} is not a member
     *         or if {@code node} is not a possible node.
     */
    void insert(int pred, int node);

    /**
     * Tells if a node can be scheduled with a given predecessor.
     *
     * @param pred predecessor for the node.
     * @param node node trying to be scheduled.
     * @return true if the node can be scheduled.
     */
    boolean canInsert(int pred, int node);


    /**
     * Tells if a node can precede another one.
     * A node {@code p} can precede a node {@code n}
     * if a sequence can be formed where {@code p} occurs before {@code n} in the order.
     *
     * @param pred predecessor for the node.
     * @param node node.
     * @return {@code true} if a sequence can be formed with {@code pred} preceding node.
     */
    boolean canPrecede(int pred, int node);

    /**
     * Excludes the node from the set of possible nodes.
     *
     * @param node node to exclude.
     * @throws org.maxicp.util.exception.InconsistencyException if {@code node}
     *         is a member of the partial sequence.
     */
    void exclude(int node);

    /**
     * Excludes all possible nodes from the sequence, fixing the variable.
     */
    void excludeAllPossible();

    /**
     * Tells if a node is a member of the sequence.
     *
     * @param node the node whose state needs to be known.
     * @return true if the node is a member of the sequence.
     */
    boolean isMember(int node);

    /**
     * Tells if a node is a possible one.
     *
     * @param node node whose state needs to be known.
     * @return true if the node is possible.
     */
    boolean isPossible(int node);

    /**
     * Tells if a node is excluded.
     *
     * @param node node whose state needs to be known.
     * @return true if the node is excluded.
     */
    boolean isExcluded(int node);

    /**
     * Copies the member nodes into an array.
     * The copied values always contain the {@link #begin()} and {@link #end()} nodes
     *
     * @param dest an array large enough {@code dest.length >= nScheduledNode(true)}
     * @return the size of the scheduled domain and {@code dest[0,...,size-1]} contains
     *         the values in the scheduled domain in an arbitrary order.
     */
    int fillMember(int[] dest);

    /**
     * Copies the possible values of the domain into an array.
     *
     * @param dest an array large enough {@code dest.length >= nPossible()}
     * @return the size of the possible domain and {@code dest[0,...,size-1]} contains
     *         the values in the possible domain in an arbitrary order.
     */
    int fillPossible(int[] dest);

    /**
     * Copies the excluded values of the domain into an array.
     *
     * @param dest an array large enough {@code dest.length >= nExcluded()}
     * @return the size of the excluded domain and {@code dest[0,...,size-1]} contains
     *         the values in the excluded domain in an arbitrary order.
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
     * Removes all insertions having the specified node as predecessor.
     *
     * @param node node after which no insertion can occur.
     */
    void removeInsertionAfter(int node);

    /**
     * Gives the {@link CPInsertionVar} related to a node in the sequence.
     *
     * @param i id of the InsertionVar
     * @return InsertionVar with id i
     */
    CPInsertionVar getInsertionVar(int i);

    /**
     * Asks that the closure is called whenever the domain
     * of this variable is reduced to a single setValue.
     *
     * @param f the closure
     */
    void whenFixed(Procedure f);

    /**
     * Asks that the closure is called whenever
     * a new node is scheduled into the sequence.
     *
     * @param f the closure
     */
    void whenInsert(Procedure f);

    /**
     * Asks that the closure is called whenever
     * a new node is excluded from the sequence.
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
     * Tells if the variable is fixed.
     *
     * @return true when no more node belongs to the set of possible nodes
     */
    boolean isFixed();

    /**
     * Returns the next member node in the sequence just after the one given in parameter.
     *
     * @param node node member of the sequence.
     * @return index of the successor of the node. Irrelevant if the node is not in the sequence
     */
    int nextMember(int node);

    /**
     * Returns the predecessor member node in the sequence just before the one given in parameter.
     *
     * @param node node member of the sequence.
     * @return index of the predecessor of the node. Irrelevant if the node is not in the sequence
     */
    int predMember(int node);

    /**
     * Fills the current order of the sequence into an array
     * including {@link #begin()} and {@link #end()} node.
     *
     * @param dest array where to store the order of the sequence. The array should be large enough.
     * @return number of elements in the sequence, including beginning and ending node
     */
    int fillOrder(int[] dest);

    /**
     * Fills the current order of the sequence into an array.
     *
     * @param dest array where to store the order of the sequence. The array should be large enough.
     * @param includeBounds if True, includes the beginning and ending node into the order array
     * @return number of elements in the sequence
     */
    int fillOrder(int[] dest, boolean includeBounds);

    /**
     * Gives a string representation of the ordering of nodes with the {@link #begin()} and {@link #end()} nodes.
     *
     * @return ordering of the sequence, with " -> " between 2 consecutive nodes, incudling the {@link #begin()} and
     *  {@link #end()} nodes
     */
    String ordering();

    /**
     * Gives a string representation of the ordering of nodes with possibly the {@link #begin()} and {@link #end()} nodes.
     *
     * @param includeBounds if the bounds ({@link #begin()} and {@link #end()}) must be included or not
     * @return ordering of the sequence, with " -> " between 2 consecutive nodes
     */
    String ordering(boolean includeBounds);

    /**
     * Gives a string representation of ordering of nodes with the beginning and end nodes.
     *
     * @param includeBounds if the bounds ({@link #begin()} and {@link #end()}) must be included or not
     * @param join string that must be used to join two consecutive nodes
     * @return ordering of the sequence, nodes being joined on the specified string
     */
    String ordering(boolean includeBounds, String join);

    /**
     * Tells if a given node precedes another one in the sequence.
     *
     * @param pred node occuring before
     * @param node node occuring after
     * @return true iff pred precedes node in the sequence
     */
    boolean precede(int pred, int node);

}
