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

package org.maxicp.state;


import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Implementation of {@link StateMap} with trail strategy
 * @see Trailer
 * @see StateManager#makeStateMap()
 */
public class TrailMap<K, V> implements StateMap<K, V> {

    private Trailer trail;
    private Map<K, V> map = new IdentityHashMap<>();

    protected TrailMap(Trailer trail) {
        this.trail = trail;
    }

    public void put(K k, V v) {
        if (!map.containsKey(k)) {
            trail.pushState(new StateEntry() {
                @Override
                public void restore() {
                    map.remove(k);
                }
            });
        } else {
            V vOld = map.get(k);
            trail.pushState(new StateEntry() {
                @Override
                public void restore() {
                    map.put(k, vOld);
                }
            });
        }
        map.put(k, v);
    }

    public V get(K k) {
        return map.get(k);
    }
}
