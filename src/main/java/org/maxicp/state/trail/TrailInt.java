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

package org.maxicp.state.trail;


import org.maxicp.state.StateInt;
import org.maxicp.state.StateManager;
import org.maxicp.state.trail.Trail;
import org.maxicp.state.trail.Trailer;

/**
 * Implementation of {@link StateInt} with trail strategy
 * @see Trailer
 * @see StateManager#makeStateInt(int)
 */
public class TrailInt extends Trail<Integer> implements StateInt {

    protected TrailInt(Trailer trail, int initial) {
        super(trail, initial);
    }

}
