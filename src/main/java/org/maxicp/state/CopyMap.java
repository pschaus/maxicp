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
 * Implementation of {@link StateMap} with copy strategy
 * @see Copier
 * @see StateManager#makeStateMap()
 */
public class CopyMap<K, V> implements Storage, StateMap<K, V> {

    class CopyMapStateEntry implements StateEntry {
        private final Map<K, V> map;

        CopyMapStateEntry(Map<K, V> map) {
            this.map = map;
        }

        public void restore() {
            CopyMap.this.map = map;
        }
    }

    private Map<K, V> map;

    protected CopyMap() {
        map = new IdentityHashMap<>();
    }

    protected CopyMap(Map<K, V> m) {
        map = new IdentityHashMap<>();
        for (Map.Entry<K, V> me : m.entrySet())
            m.put(me.getKey(), me.getValue());
    }

    @Override
    public void put(K k, V v) {
        map.put(k, v);
    }

    @Override
    public V get(K k) {
        return map.get(k);
    }

    @Override
    public StateEntry save() {
        Map<K, V> mapCopy = new IdentityHashMap<>();
        for (Map.Entry<K, V> me : map.entrySet())
            mapCopy.put(me.getKey(), me.getValue());
        return new CopyMapStateEntry(mapCopy);
    }

}
