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

package org.maxicp.engine;


import com.github.guillaumederval.javagrading.GradingRunnerWithParametersFactory;
import org.maxicp.engine.core.MiniCP;
import org.maxicp.engine.core.Solver;
import org.maxicp.state.Copier;
import org.maxicp.state.Trailer;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(GradingRunnerWithParametersFactory.class)
public abstract class SolverTest {

    @Parameterized.Parameters
    public static Supplier<Solver>[] data() {
        return new Supplier[]{
                () -> new MiniCP(new Trailer()),
                () -> new MiniCP(new Copier()),
        };
    }

    @Parameterized.Parameter
    public Supplier<Solver> solverFactory;
}
