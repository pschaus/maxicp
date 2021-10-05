package org.maxicp.cp;

import org.maxicp.cp.engine.constraints.AllDifferentDC;
import org.maxicp.cp.engine.core.*;
import org.maxicp.cp.modelingCompat.ConcreteCPIntVar;
import org.maxicp.cp.modelingCompat.ConcreteCPVar;
import org.maxicp.cp.modelingCompat.InstanciatedCPModel;
import org.maxicp.model.*;
import org.maxicp.model.concrete.ConcreteVar;
import org.maxicp.model.constraints.AllDifferent;
import org.maxicp.model.symbolic.IntVarRangeImpl;
import org.maxicp.model.symbolic.IntVarSetImpl;
import org.maxicp.state.copy.Copier;
import org.maxicp.state.trail.Trailer;

import java.util.HashMap;

public class CPModelInstantiator {
    record Instanciator(boolean useTrailing) implements ModelDispatcher.ModelInstantiator<InstanciatedCPModel> {
        @Override
        public InstanciatedCPModel instanciate(Model m) {
            CPSolver s = new MiniCP(useTrailing ? new Trailer() : new Copier());
            return new InstanciatedCPModel(m.getDispatcher(), s, m.getCstNode());
        }
    }

    static public final Instanciator withTrailing = new Instanciator(true);
    static public final Instanciator withCopying = new Instanciator(false);
    static public final Instanciator base = new Instanciator(true);
}
