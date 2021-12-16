package com.td.recommend.video.utils;


import com.codahale.metrics.Histogram;
import com.github.sps.metrics.TaggedMetricRegistry;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import com.td.recommend.commons.metrics.TaggedMetricRegisterSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by chencheng on 5/17/16.
 */
public class JvmPauseMonitor {

  private static final Logger logger = LoggerFactory.getLogger(JvmPauseMonitor.class);

  /**
   * The target sleep time
   */
  private static final long SLEEP_INTERVAL_MS = 500;
  private static final String WARN_THRESHOLD_KEY = "jvm-pause-warn-threshold.ms";
  private static final long WARN_THRESHOLD_DEFAULT = 100;
  private static final String INFO_THRESHOLD_KEY = "jvm-pause-info-threshold.ms";
  private static final long INFO_THRESHOLD_DEFAULT = 50;
  /**
   * log WARN if we detect a pause longer than this threshold
   */
  private final long warnThresholdMs;
  /**
   * log INFO if we detect a pause longer than this threshold
   */
  private final long infoThresholdMs;
  private Thread monitorThread;
  private volatile boolean shouldRun = true;

  public JvmPauseMonitor(Config conf) {
    this.warnThresholdMs = conf.hasPath(WARN_THRESHOLD_KEY) ? conf.getLong(WARN_THRESHOLD_KEY)
        : WARN_THRESHOLD_DEFAULT;
    this.infoThresholdMs = conf.hasPath(INFO_THRESHOLD_KEY) ? conf.getLong(INFO_THRESHOLD_KEY)
        : INFO_THRESHOLD_DEFAULT;
  }
  public JvmPauseMonitor() {
    this.warnThresholdMs = WARN_THRESHOLD_DEFAULT;
    this.infoThresholdMs = INFO_THRESHOLD_DEFAULT;
  }

  /**
   * Simple 'main' to facilitate manual testing of the pause monitor. This main function just leaks
   * memory into a list. Running this class with a 1GB heap will very quickly go into "GC hell" and
   * result in log messages about the GC pauses.
   */
  public static void main(String[] args) throws Exception {
    //	    new JvmPauseMonitor().start();
    //	    List<String> list = Lists.newArrayList();
    //	    int i = 0;
    //	    while (true) {
    //	      list.add(String.valueOf(i++));
    //	    }
  }

  public void start() {
    Preconditions.checkState(monitorThread == null, "Already started");
    monitorThread = new Thread(new Monitor());
    monitorThread.setDaemon(true);
    monitorThread.setName("JvmPauseMonitor");
    monitorThread.start();
    logger.info("start jvmPauseMonitor.");
  }

  public void stop() {
    logger.info("stopping jvmPauseMonitor.");
    shouldRun = false;
    monitorThread.interrupt();
    try {
      monitorThread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private String formatMessage(long extraSleepTime, Map<String, GcTimes> gcTimesAfterSleep,
      Map<String, GcTimes> gcTimesBeforeSleep) {

    Set<String> gcBeanNames =
        Sets.intersection(gcTimesAfterSleep.keySet(), gcTimesBeforeSleep.keySet());
    List<String> gcDiffs = Lists.newArrayList();
    for (String name : gcBeanNames) {
      GcTimes diff = gcTimesAfterSleep.get(name).subtract(gcTimesBeforeSleep.get(name));
      if (diff.gcCount != 0) {
        gcDiffs.add("GC pool '" + name + "' had collection(s): " + diff.toString());
      }
    }

    String ret = "Detected pause in JVM or host machine (eg GC): " + "pause of approximately "
        + extraSleepTime + "ms\n";
    if (gcDiffs.isEmpty()) {
      ret += "No GCs detected";
    } else {
      ret += Joiner.on("\n").join(gcDiffs);
    }
    return ret;
  }

  private Map<String, GcTimes> getGcTimes() {
    Map<String, GcTimes> map = Maps.newHashMap();
    List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    for (GarbageCollectorMXBean gcBean : gcBeans) {
      map.put(gcBean.getName(), new GcTimes(gcBean));
    }
    return map;
  }

  private static class GcTimes {

    private long gcCount;
    private long gcTimeMillis;

    private GcTimes(GarbageCollectorMXBean gcBean) {
      gcCount = gcBean.getCollectionCount();
      gcTimeMillis = gcBean.getCollectionTime();
    }

    private GcTimes(long count, long time) {
      this.gcCount = count;
      this.gcTimeMillis = time;
    }

    private GcTimes subtract(GcTimes other) {
      return new GcTimes(this.gcCount - other.gcCount, this.gcTimeMillis - other.gcTimeMillis);
    }

    @Override
    public String toString() {
      return "count=" + gcCount + " time=" + gcTimeMillis + "ms";
    }
  }

  private class Monitor implements Runnable {

    @Override
    public void run() {
      Stopwatch sw = Stopwatch.createStarted();
      Map<String, GcTimes> gcTimesBeforeSleep = getGcTimes();
      while (shouldRun) {
        sw.reset().start();
        try {
          Thread.sleep(SLEEP_INTERVAL_MS);
        } catch (InterruptedException ie) {
          return;
        }
        long extraSleepTime = sw.elapsed(TimeUnit.MILLISECONDS) - SLEEP_INTERVAL_MS;
        Map<String, GcTimes> gcTimesAfterSleep = getGcTimes();
        if (extraSleepTime > warnThresholdMs) {
          logger.warn(formatMessage(extraSleepTime, gcTimesAfterSleep, gcTimesBeforeSleep));
        } else if (extraSleepTime > infoThresholdMs) {
          logger.info(formatMessage(extraSleepTime, gcTimesAfterSleep, gcTimesBeforeSleep));
        }
        gcTimesBeforeSleep = gcTimesAfterSleep;
        TaggedMetricRegistry taggedMetricRegistry = TaggedMetricRegisterSingleton.getInstance()
            .getTaggedMetricRegistry();
        Histogram histogram = taggedMetricRegistry.histogram("recom.gc.histogram");
        histogram.update(extraSleepTime);
      }
    }
  }
}


