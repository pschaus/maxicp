package org.maxicp.model;

public interface SequenceVar extends Var {

    int begin();
    int end();
    int nMember();
    int nPossible();
    int nExcluded();
    int nNode();
    int nInsertion(int node);
    int nMemberInsertions(int node);
    int nPossibleInsertions(int node);
    boolean isMember(int node);
    boolean isPossible(int node);
    boolean isExcluded(int node);
    int fillMember(int[] dest);
    int fillPossible(int[] dest);
    int fillExcluded(int[] dest);
    int fillMemberInsertion(int node, int[] dest);
    int fillPossibleInsertion(int node, int[] dest);
    int fillInsertion(int node, int[] dest);

}
