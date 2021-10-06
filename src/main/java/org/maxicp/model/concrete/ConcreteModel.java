package org.maxicp.model.concrete;

import org.maxicp.model.Model;
import org.maxicp.model.Var;

import java.util.HashMap;

public interface ConcreteModel extends Model {
    HashMap<Var, ConcreteVar> getMapping();
}
