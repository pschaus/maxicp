package org.maxicp.cp;

import org.maxicp.cp.engine.core.*;
import org.maxicp.model.*;
import org.maxicp.state.copy.Copier;
import org.maxicp.state.trail.Trailer;

public class CPModelInstantiator {
    record Instanciator(boolean useTrailing) implements ModelDispatcher.ModelInstantiator<ConcreteCPModel> {
        @Override
        public ConcreteCPModel instanciate(Model m) {
            CPSolver s = new MiniCP(useTrailing ? new Trailer() : new Copier());
            return new ConcreteCPModel(m.getDispatcher(), s, m.getCstNode());
        }
    }

    static public final Instanciator withTrailing = new Instanciator(true);
    static public final Instanciator withCopying = new Instanciator(false);
    static public final Instanciator base = new Instanciator(true);
}
