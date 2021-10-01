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


package org.maxicp.cp.engine.constraints;

import org.maxicp.Factory;
import org.maxicp.cp.engine.constraints.Profile.Rectangle;
import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.util.exception.InconsistencyException;

import java.util.ArrayList;

import static org.maxicp.Factory.minus;
import static org.maxicp.Factory.plus;

/**
 * Cumulative constraint with time-table filtering
 */
public class Cumulative extends AbstractCPConstraint {

    private final CPIntVar[] start;
    private final int[] duration;
    private final CPIntVar[] end;
    private final int[] demand;
    private final int capa;
    private final boolean postMirror;


    /**
     * Creates a cumulative constraint with a time-table filtering.
     * At any time-point t, the sum of the demands
     * of the activities overlapping t do not overlap the capacity.
     *
     * @param start the start time of each activities
     * @param duration the duration of each activities (non negative)
     * @param demand the demand of each activities, non negative
     * @param capa the capacity of the constraint
     */
    public Cumulative(CPIntVar[] start, int[] duration, int[] demand, int capa) {
        this(start, duration, demand, capa, true);
    }

    private Cumulative(CPIntVar[] start, int[] duration, int[] demand, int capa, boolean postMirror) {
        super(start[0].getSolver());
        this.start = start;
        this.duration = duration;
        this.end = Factory.makeIntVarArray(start.length, i -> plus(start[i], duration[i]));
        this.demand = demand;
        this.capa = capa;
        this.postMirror = postMirror;
    }


    @Override
    public void post() {
        for (int i = 0; i < start.length; i++) {
            start[i].propagateOnBoundChange(this);
        }

        if (postMirror) {
            CPIntVar[] startMirror = Factory.makeIntVarArray(start.length, i -> minus(end[i]));
            getSolver().post(new Cumulative(startMirror, duration, demand, capa, false), false);
        }

        propagate();
    }

    @Override
    public void propagate() {
        Profile profile = buildProfile();
        // TODO 2: check that the profile is not exceeding the capa otherwise throw an INCONSISTENCY
        for (int i = 0; i < profile.size(); i++) {
            // STUDENT throw new NotImplementedException("Cumulative");
            // BEGIN STRIP
            if (profile.get(i).height() > capa) {
                throw InconsistencyException.INCONSISTENCY;
            }
            // END STRIP
        }

        for (int i = 0; i < start.length; i++) {
            if (!start[i].isBound()) {
                // j is the index of the profile rectangle overlapping t
                int j = profile.rectangleIndex(start[i].min());
                // TODO 3: push i to the right
                // hint:
                // Check that at every-point on the interval
                // [start[i].getMin() ... start[i].getMin()+duration[i]-1] there is enough space.
                // You may have to look-ahead on the next profile rectangle(s)
                // Be careful that the activity you are currently pushing may have contributed to the profile.
                // STUDENT throw new NotImplementedException();
                // BEGIN STRIP
                int t = start[i].min();
                while (j < profile.size()
                        && profile.get(j).start() < Math.min(t + duration[i], start[i].max())) {
                    if (capa - demand[i]
                          <  profile.get(j).height()) {
                        t = Math.min(profile.get(j).end(), start[i].max());
                    }
                    j++;
                }
                start[i].removeBelow(t);
                // END STRIP
            }
        }
    }

    public Profile buildProfile() {
        ArrayList<Rectangle> mandatoryParts = new ArrayList<Rectangle>();
        for (int i = 0; i < start.length; i++) {
            // TODO 1: add mandatory part of activity i if any
            // STUDENT throw new NotImplementedException("Cumulative");
            // BEGIN STRIP
            if (end[i].min() > start[i].max()) {
                int s = start[i].max();
                int e = end[i].min();
                int d = demand[i];
                mandatoryParts.add(new Rectangle(s, e, d));
            }
            // END STRIP
        }
        return new Profile(mandatoryParts.toArray(new Profile.Rectangle[0]));
    }

}
