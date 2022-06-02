/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package org.maxicp.cp.engine.core;

/**
 * Interface for set domain implementation.
 * A domain is encapsulated in an implementation.
 * A domain is like a set of integers.
 */
public interface SetDomain {

    void includeAll(SetDomainListener l);

    void excludeAll(SetDomainListener l);

    void exclude(int v, SetDomainListener l);

    void include(int v, SetDomainListener l);

    @Override
    String toString();
}
