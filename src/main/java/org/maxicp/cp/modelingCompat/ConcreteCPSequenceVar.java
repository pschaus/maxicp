package org.maxicp.cp.modelingCompat;

import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.cp.engine.core.CPVar;
import org.maxicp.model.ModelDispatcher;
import org.maxicp.model.concrete.ConcreteSequenceVar;

public class ConcreteCPSequenceVar implements ConcreteSequenceVar, ConcreteCPVar{

    final CPSequenceVar s;
    final ModelDispatcher md;

    public ConcreteCPSequenceVar(ModelDispatcher md, CPSequenceVar s) {
        this.md = md;
        this.s = s;
    }

    @Override
    public CPVar getVar() {
        return s;
    }

    @Override
    public ModelDispatcher getDispatcher() {
        return md;
    }


    @Override
    public int begin() {
        return s.begin();
    }

    @Override
    public int end() {
        return s.end();
    }

    @Override
    public int nMember() {
        return s.nMember();
    }

    @Override
    public int nPossible() {
        return s.nPossible();
    }

    @Override
    public int nExcluded() {
        return s.nExcluded();
    }

    @Override
    public int nNode() {
        return s.nNode();
    }

    @Override
    public int nInsertion(int node) {
        return s.nInsertion(node);
    }

    @Override
    public int nMemberInsertion(int node) {
        return s.nMemberInsertion(node);
    }

    @Override
    public int nPossibleInsertion(int node) {
        return s.nPossibleInsertion(node);
    }

    @Override
    public boolean isMember(int node) {
        return s.isMember(node);
    }

    @Override
    public boolean isPossible(int node) {
        return s.isPossible(node);
    }

    @Override
    public boolean isExcluded(int node) {
        return s.isExcluded(node);
    }

    @Override
    public int fillMember(int[] dest) {
        return s.fillMember(dest);
    }

    @Override
    public int fillPossible(int[] dest) {
        return s.fillPossible(dest);
    }

    @Override
    public int fillExcluded(int[] dest) {
        return s.fillExcluded(dest);
    }

    @Override
    public int fillMemberInsertion(int node, int[] dest) {
        return s.fillMemberInsertion(node, dest);
    }

    @Override
    public int fillPossibleInsertion(int node, int[] dest) {
        return s.fillPossibleInsertion(node, dest);
    }

    @Override
    public int fillInsertion(int node, int[] dest) {
        return s.fillInsertion(node, dest);
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
