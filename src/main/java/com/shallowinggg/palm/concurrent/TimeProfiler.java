package com.shallowinggg.palm.concurrent;

import com.shallowinggg.palm.StringUtil;
import com.shallowinggg.palm.reflect.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 此类的功能是一个计时器。
 * 当开始计时时，请调用{@link #start()} 方法，此方法会自己获取测试方法的名称并作为
 * 任务的名称，你也可以调用{@link #start(String)}方法指定任务名称。
 * 除了进行一段计时以外，你还可以进行多段计时，调用{@link #calc()}方法。同时，你还
 * 可以调用{@link #calc(String)}方法，提供某一段计时的任务名称。你可以调用
 * {@link #totalTimeMills()}获取此次计时的总时间，或者调用{@link #shortSummary()}方法
 * 获取一个简短的总结信息。如果你想要获取详尽的计时信息，请调用{@link #explicitDetail()}
 * 方法。
 * <p>
 * 计时器默认使用MILLS作为时间单位，计时器还提供了一个耗时阈值，默认为200ms，
 * 你可以调用{@link #setTimeCostThreshold(long, TimeUnit)}方法设置你想要的阈值。
 * 当某段任务的执行时长超过此阈值时，计时器将会记录此任务的信息，以供查阅。
 * 除了计时信息以外，你还可以调用{@link #profiler()}方法获取此次计时的分析。
 * 记住，每次计时完成后要调用{@link #clear()}方法重置计数器。
 * <p>
 * 这个类是并发安全的，你可以随意使用，无需顾忌并发导致的测量结果相互影响的问题，
 * 但是对于线程调度引发的测试时间失真，此类并没有解决。
 *
 * @author dingshimin
 * @since 1.0
 */
public class TimeProfiler {
    private static final Logger LOG = LoggerFactory.getLogger(TimeProfiler.class);

    /**
     * 上一次计时开始时间，为{@link #calc()}方法提供帮助。
     */
    private static final ThreadLocal<Long> PREV = ThreadLocal.withInitial(() -> Long.MAX_VALUE);

    /**
     * 启动定时器的调用方法名称
     */
    private static final ThreadLocal<String> CALLER = new ThreadLocal<>();

    /**
     * 计时器上下文
     */
    private static final ThreadLocal<TimeContext> CONTEXT = ThreadLocal.withInitial(TimeContext::new);

    private TimeProfiler() {
    }

    /**
     * 启动计时器，任务名称为调用此方法的方法名称
     */
    public static void start() {
        PREV.set(System.currentTimeMillis());
        CALLER.set(Thread.currentThread().getName() + "#" + MethodUtil.getCaller());
    }

    /**
     * 启动计时器，任务名称为给定的名字
     *
     * @param task 任务名称
     */
    public static void start(String task) {
        PREV.set(System.currentTimeMillis());
        CALLER.set(task);
    }

    /**
     * 进行一次未命名计时，此方法需要在测量块执行完成后调用。
     */
    public static void calc() {
        long now = System.currentTimeMillis();
        long cost = now - PREV.get();
        PREV.set(now);
        CONTEXT.get().record(null, cost);
    }

    /**
     * 进行一次命名计时，此方法需要在测量块执行完成后调用。
     *
     * @param subTaskName 子任务名称
     */
    public static void calc(String subTaskName) {
        long now = System.currentTimeMillis();
        long cost = now - PREV.get();
        PREV.set(now);
        CONTEXT.get().record(subTaskName, cost);
    }

    /**
     * 获取全部任务执行时间
     */
    public static long totalTimeMills() {
        return CONTEXT.get().getTotalTimeMillis();
    }

    /**
     * 获取一个简短的总结信息
     */
    public static String shortSummary() {
        return CONTEXT.get().shortSummary();
    }

    /**
     * 获取详细的计时信息
     */
    public static String explicitDetail() {
        return CONTEXT.get().prettyPrint();
    }

    /**
     * 获取计时分析
     */
    public static String profiler() {
        return CONTEXT.get().profiler();
    }

    /**
     * 重置计时器
     */
    public static void clear() {
        if(LOG.isDebugEnabled()) {
            LOG.debug(explicitDetail() + profiler());
        }
        PREV.remove();
        CALLER.remove();
        CONTEXT.remove();
    }

    /**
     * 设置任务执行时间阈值。
     * 当某个任务的执行时间超过此阈值时，将会被记录下来。
     */
    public static void setTimeCostThreshold(long threshold, TimeUnit unit) {
        CONTEXT.get().setTimeoutThreshold(unit.toMillis(threshold));
    }

    /**
     * 计时上下文。
     * 储存计时数据，对结果进行分析，输出
     */
    private static class TimeContext {
        private static final String DEFAULT_TASK_NAME = "unnamed-task";

        List<TaskInfo> taskInfos = new ArrayList<>();
        List<TaskInfo> timeoutTasks = new ArrayList<>();
        int unnamed;

        long max;
        long min = Long.MAX_VALUE;
        long mean;
        long totalMills;
        long threshold = 200L;

        void record(String taskName, long executionTime) {
            if (StringUtil.isBlank(taskName)) {
                taskName = DEFAULT_TASK_NAME + unnamed;
                unnamed++;
            }
            TaskInfo taskInfo = new TaskInfo(taskName, executionTime);
            taskInfos.add(taskInfo);
            totalMills += executionTime;
            mean = totalMills / taskInfos.size();

            if (executionTime > max) {
                max = executionTime;
            }
            if (executionTime < min) {
                min = executionTime;
            }
            if (executionTime >= threshold) {
                timeoutTasks.add(taskInfo);
            }
        }

        long getTotalTimeMillis() {
            return totalMills;
        }

        double getTotalTimeSeconds() {
            return totalMills / 1000.0d;
        }

        String shortSummary() {
            return "TimeProfiler '" + CALLER.get() + "': running time (millis) = " + getTotalTimeMillis();
        }

        String prettyPrint() {
            StringBuilder sb = new StringBuilder(shortSummary());
            sb.append('\n');

            sb.append("-----------------------------------------\n");
            sb.append("ms     %     Task name\n");
            sb.append("-----------------------------------------\n");
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(5);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);
            for (TaskInfo task : taskInfos) {
                sb.append(nf.format(task.getExecTime())).append("  ");
                sb.append(pf.format(task.getExecTimeSeconds() / getTotalTimeSeconds())).append("  ");
                sb.append(task.getTaskName()).append("\n");
            }

            return sb.toString();
        }

        String profiler() {
            int size = taskInfos.size();
            long[] times = sortTimes();
            StringBuilder sb = new StringBuilder(shortSummary());
            sb.append('\n');

            sb.append("-----------------------------------------\n");
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);
            sb.append("count: ").append(taskInfos.size()).append("\n");
            sb.append("  min: ").append(min).append(" ms\n");
            sb.append("  max: ").append(max).append(" ms\n");
            sb.append(" mean: ").append(totalMills / size).append(" ms\n");
            sb.append(" 50% <= ").append(times[(int) (size * 0.5)]).append(" ms\n");
            sb.append(" 75% <= ").append(times[(int) (size * 0.75)]).append(" ms\n");
            sb.append(" 90% <= ").append(times[(int) (size * 0.9)]).append(" ms\n");
            sb.append(pf.format((double) timeoutTasks.size() / size)).append(" >= ").append(threshold).append(" ms\n");
            for (TaskInfo task : timeoutTasks) {
                sb.append(task.getTaskName()).append("  ");
                sb.append(task.getExecTime()).append("ms\n");
            }

            return sb.toString();
        }

        void setTimeoutThreshold(long threshold) {
            this.threshold = threshold;
        }

        long[] sortTimes() {
            long[] times = new long[taskInfos.size()];
            int i = 0;
            for(TaskInfo task : taskInfos) {
                times[i] = task.getExecTime();
                i++;
            }
            Arrays.sort(times);

            return times;
        }

    }

    private static class TaskInfo {
        String taskName;
        long execTime;

        TaskInfo(String taskName, long execTime) {
            this.taskName = taskName;
            this.execTime = execTime;
        }

        String getTaskName() {
            return taskName;
        }

        long getExecTime() {
            return execTime;
        }

        double getExecTimeSeconds() {
            return execTime / 1000.0d;
        }
    }

}
