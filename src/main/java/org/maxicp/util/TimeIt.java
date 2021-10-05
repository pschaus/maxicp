package org.maxicp.util;

import java.util.function.Supplier;

public class TimeIt {
    public static record TimeItResult<R>(long timeInNanoSecs, R retval) {}

    public static <R> TimeItResult<R> run(Supplier<R> fun) {
        long start = System.nanoTime();
        R retval = fun.get();
        long end = System.nanoTime();
        return new TimeItResult<>(end-start, retval);
    }

    public static long run(Runnable fun) {
        return run(() -> { fun.run(); return 0; }).timeInNanoSecs;
    }
}
