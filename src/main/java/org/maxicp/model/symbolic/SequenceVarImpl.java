package org.maxicp.model.symbolic;

import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.concrete.ConcreteModel;
import org.maxicp.model.concrete.ConcreteSequenceVar;

public class SequenceVarImpl implements SymbolicSequenceVar {

    final ModelDispatcher bm;
    private final int begin;
    private final int end;
    private final int nNodes;

    public SequenceVarImpl(ModelDispatcher bm, int nNodes, int begin, int end) {
        this.bm = bm;
        this.nNodes = nNodes;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public int begin() {
        return begin;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public int nNode() {
        return nNodes;
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return bm;
    }

    @Override
    public int initNMember() {
        return 2;
    }

    @Override
    public int initNPossible() {
        return 0;
    }

    @Override
    public int initNExcluded() {
        return 0;
    }

    @Override
    public int initNInsertions(int node) {
        return node - 1;
    }

    @Override
    public int initNMemberInsertions(int node) {
        return 1;
    }

    @Override
    public int initNPossibleInsertions(int node) {
        return node - 2;
    }

    @Override
    public boolean initIsMember(int node) {
        return false;
    }

    @Override
    public boolean initIsPossible(int node) {
        return false;
    }

    @Override
    public boolean initIsExcluded(int node) {
        return false;
    }

    @Override
    public int initFillMembers(int[] dest) {
        return 0;
    }

    @Override
    public int initFillPossible(int[] dest) {
        return 0;
    }

    @Override
    public int initFillExcluded(int[] dest) {
        return 0;
    }

    @Override
    public int initFillMembersInsertions(int node, int[] dest) {
        return 0;
    }

    @Override
    public int initFillPossibleInsertions(int node, int[] dest) {
        return 0;
    }

    @Override
    public int initFillInsertions(int node, int[] dest) {
        return 0;
    }

    @Override
    public String toString() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm) {
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).toString();
        }
        return "sequenceVar";
    }

}
