package org.maxicp.cp;

import org.maxicp.cp.engine.core.CPBoolVar;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.model.BoolVar;
import org.maxicp.model.IntVar;

public interface CPModel {
    CPBoolVar get(BoolVar v);
    CPIntVar get(IntVar v);
    CPSolver getSolver();
}
