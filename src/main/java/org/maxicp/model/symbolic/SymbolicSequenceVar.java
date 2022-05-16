package org.maxicp.model.symbolic;

import org.maxicp.model.SequenceVar;
import org.maxicp.model.concrete.ConcreteModel;
import org.maxicp.model.concrete.ConcreteSequenceVar;

public interface SymbolicSequenceVar extends SequenceVar, SymbolicVar {

    default int nMember() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).nMember();
        return initNMember();
    }

    default int nPossible() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).nPossible();
        return initNPossible();
    }

    default int nExcluded() {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).nExcluded();
        return initNExcluded();
    }


    default boolean isMember(int node) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).isMember(node);
        return initIsMember(node);
    }

    default boolean isPossible(int node) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).isPossible(node);
        return initIsPossible(node);
    }

    default boolean isExcluded(int node) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).isExcluded(node);
        return initIsExcluded(node);
    }

    default int fillMember(int[] array) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).fillMember(array);
        return initFillMembers(array);
    }

    default int fillPossible(int[] array) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).fillPossible(array);
        return initFillPossible(array);
    }

    default int fillExcluded(int[] array) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).fillExcluded(array);
        return initFillExcluded(array);
    }

    default int fillMemberInsertion(int node, int[] array) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).fillMemberInsertion(node, array);
        return initFillMembersInsertions(node, array);
    }

    default int fillPossibleInsertion(int node, int[] array) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).fillPossibleInsertion(node, array);
        return initFillPossibleInsertions(node, array);
    }

    default int fillInsertion(int node, int[] array) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).fillInsertion(node, array);
        return initFillInsertions(node, array);
    }

    default int nInsertion(int node) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).nInsertion(node);
        return initNInsertions(node);
    }

    default int nMemberInsertion(int node) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).nMemberInsertion(node);
        return initNMemberInsertions(node);
    }

    default int nPossibleInsertion(int node) {
        if (getDispatcher().getModel() instanceof ConcreteModel cm)
            return ((ConcreteSequenceVar) cm.getMapping().get(this)).nPossibleInsertion(node);
        return initNPossibleInsertions(node);
    }

    int initNMember();
    int initNPossible();
    int initNExcluded();
    int initNInsertions(int node);
    int initNMemberInsertions(int node);
    int initNPossibleInsertions(int node);
    boolean initIsMember(int node);
    boolean initIsPossible(int node);
    boolean initIsExcluded(int node);
    int initFillMembers(int[] array);
    int initFillPossible(int[] array);
    int initFillExcluded(int[] array);
    int initFillMembersInsertions(int node, int[] array);
    int initFillPossibleInsertions(int node, int[] array);
    int initFillInsertions(int node, int[] array);

}
