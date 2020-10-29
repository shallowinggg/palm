package com.shallowinggg.palm;

import com.shallowinggg.palm.concurrent.TimeProfiler;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

public class TimeProfilerTest {
    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Test
    public void testTimeProfiler() {
        TimeProfiler.start();
        for(int i = 0; i < 10; ++i) {
            func();
            TimeProfiler.calc("task" + i);
        }
        System.out.println(TimeProfiler.shortSummary());
        System.out.println(TimeProfiler.explicitDetail());
        System.out.println(TimeProfiler.profiler());
        TimeProfiler.clear();
    }

    private void func() {
        try {
            Thread.sleep(random.nextInt(300));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
