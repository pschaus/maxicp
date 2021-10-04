package org.maxicp.cp.modelingCompat;

import org.maxicp.cp.engine.core.CPVar;
import org.maxicp.model.concrete.ConcreteVar;

public interface ConcreteCPVar extends ConcreteVar {
    CPVar getVar();
}
