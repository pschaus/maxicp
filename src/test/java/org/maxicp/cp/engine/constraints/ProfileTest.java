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


import org.maxicp.util.exception.NotImplementedException;
import org.maxicp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;


public class ProfileTest {


    private boolean checkProfile(Profile.Rectangle... rectangles) {
        Profile p = new Profile(rectangles);
        int[] discreteProfile_p = discreteProfile(p.rectangles());
        int[] discreteProfile_r = discreteProfile(rectangles);
        if (!Arrays.equals(discreteProfile_p, discreteProfile_r)) {
            System.out.println("not same profile");
            return false;
        }
        Profile.Rectangle[] rects = p.rectangles();
        for (int i = 0; i < rects.length - 1; i++) {
            if (rects[i].end() != rects[i + 1].start()) {
                System.out.println("not continuous rectangles");
                return false;
            }
        }
        return p.size() <= 2 * rectangles.length + 2;
    }


    private int[] discreteProfile(Profile.Rectangle... rectangles) {
        int min = Arrays.stream(rectangles).filter(r -> r.height() > 0).map(r -> r.start()).min(Integer::compare).get();
        int max = Arrays.stream(rectangles).filter(r -> r.height() > 0).map(r -> r.end()).max(Integer::compare).get();
        int[] heights = new int[max - min];
        // discrete profileRectangles of rectangles
        for (Profile.Rectangle r : rectangles) {
            if (r.height() > 0) {
                for (int i = r.start(); i < r.end(); i++) {
                    heights[i - min] += r.height();
                }
            }
        }
        return heights;
    }


    @Test
    public void testProfile1() {
        try {

            Profile.Rectangle r1 = new Profile.Rectangle(7, 11, 3);
            Profile.Rectangle r2 = new Profile.Rectangle(2, 10, 1);
            Profile.Rectangle r3 = new Profile.Rectangle(3, 4, 2);
            assert (checkProfile(r1, r2, r3));

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testProfile2() {
        try {

            Profile.Rectangle r1 = new Profile.Rectangle(1, 10, 3);
            Profile.Rectangle r2 = new Profile.Rectangle(1, 10, 1);
            Profile.Rectangle r3 = new Profile.Rectangle(1, 10, 2);
            assert (checkProfile(r1, r2, r3));

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testProfileRandom() {
        try {

            Random r = new Random(0);

            for (int iter = 0; iter < 10; iter++) {
                Profile.Rectangle[] rects = IntStream.range(0, 10).mapToObj(i -> {
                    int start = r.nextInt(100);
                    int end = start + 1 + r.nextInt(30);
                    int height = 1 + r.nextInt(30);
                    return new Profile.Rectangle(start, end, height);
                }).toArray(Profile.Rectangle[]::new);
                assert (checkProfile(rects));

            }

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
