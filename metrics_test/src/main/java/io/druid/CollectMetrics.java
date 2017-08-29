/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid;
import cn.enncloud.common.util.EnnException;
import cn.enncloud.metric.EnnMetricsThread;
import cn.enncloud.metric.EnnMetricsThreadModule;
import cn.enncloud.metric.OpentsdbHttpReporterModule;
import cn.enncloud.metric.config.EnnMetricsConfig;
import cn.enncloud.metric.config.OpentsdbConfig;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.net.UnknownHostException;

public class CollectMetrics {
    public static void startMetricsCollector()
            throws EnnException, UnknownHostException {

        Injector injector =
                Guice.createInjector(
                        new EnnMetricsThreadModule(
                                EnnMetricsConfig.getConfigWithFreq(1, "metricsTest")),
                        new OpentsdbHttpReporterModule(
                                OpentsdbConfig.newBuilder()
                                        .setHostname("localhost")
                                        .setPort(4242)
                                        .build()));

        EnnMetricsThread metricsTread = injector.getInstance(EnnMetricsThread.class);
        Thread thread = new Thread(metricsTread);
        thread.setDaemon(true);
        thread.start();
    }
    //time for query
    public static final String queryTimeName = "metrics.collect.test.QueryTime";
    public static final String queryTimeSequenceName = "metrics.collect.test.QueryTimeSequence";
    public static final String queryTimeToYielderName = "metrics.collect.test.QueryTimeToYielder";
    public static final String queryFromCacheBrokerName = "metrics.collect.test.queryFromCacheBroker";
    public static final String queryFromServerBrokerName = "metrics.collect.test.queryFromServerBroker";
    public static final String queryFromSingleServerBrokerName = "metrics.collect.test.queryFromSingleServerBroker";
    public static final String queryNodeTtfbName = "metrics.collect.test.QueryNodeTtfb";
    public static final String queryNodeTimeName = "metrics.collect.test.QueryNodeTime";
    public static final String querySegmentTimeName = "metrics.collect.test.QuerySegmentTime";
    public static final String queryNodeMergeResultsName = "metrics.collect.test.QueryNodeMergeResults"; //all query type call this method

    //time for io
    public static final String querySegmentTimeseriesAggregateName = "metrics.collect.test.QuerySegmentTimeseriesAggregate";
    public static final String querySegmentBitmapConstructionName = "metrics.collect.test.QuerySegmentBitmapConstruction";//prefilters not null construct bitmap
    public static final String querySegmentMakeCursorName = "metrics.collect.test.QuerySegmentMakeCursor";//cursor create after postfilters
    public static final String queryLoadSegmentOnDiskName = "metrics.collect.test.QueryLoadSegmentOnDisk";
    public static final String queryLoadBitmapOffHeapName = "metrics.collect.test.QueryLoadBitmapOffheap";

    //cache hit rate
    public static final String cacheHitBrokerName = "metrics.collect.test.CacheHitBroker";
    public static final String cacheNotHitBrokerName = "metrics.collect.test.CacheNotHitBroker";
    public static final String cacheHitHistoricalName = "metrics.collect.test.CacheHitHistorical";
    public static final String cacheNotHitHistoricalName = "metrics.collect.test.CacheNotHitHistorical";

}


