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
import org.avaje.metric.CounterMetric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;

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
                                        .setHostname(System.getenv("OPENTSDB_HOSTNAME"))
                                        .setPort(Integer.parseInt(System.getenv("OPENTSDB_PORT")))
                                        .build()));

        EnnMetricsThread metricsTread = injector.getInstance(EnnMetricsThread.class);
        Thread thread = new Thread(metricsTread);
        thread.setDaemon(true);
        thread.start();
    }
    //time for query
    public static final String queryTimeName = "metrics.collect.test.QueryTime";
    public static final String queryTimeSequenceName = "metrics.collect.test.SequenceQueryTime";
    public static final String queryTimeToYielderName = "metrics.collect.test.ToYielderQueryTime";
    public static final String queryFromCacheBrokerName = "metrics.collect.test.BrokerCacheQueryTime";//should delete
    public static final String queryFromServerBrokerName = "metrics.collect.test.AllServersQueryTime";//should delete
    public static final String queryFromSingleServerBrokerName = "metrics.collect.test.SingleServerQueryTime";//should delete
    public static final String queryNodeTtfbName = "metrics.collect.test.QueryNodeTtfb";//post http request and wait first byte back
    public static final String queryNodeTimeName = "metrics.collect.test.QueryNodeTime";//historical results transfer time
    public static final String querySegmentTimeName = "metrics.collect.test.SingleSegmentQueryTime";
    public static final String queryNodeMergeResultsName = "metrics.collect.test.MergeResultsOnSingleNode"; //all query type call this method

    //time for scan and compute
    public static final String querySegmentTimeseriesAggregateName = "metrics.collect.test.TimeseriesAggregateOnSingleSegment";
    public static final String querySegmentTopNAggregateName = "metrics.collect.test.TopNAggregateOnSingleSegment";//include aggregate and sort
    public static final String querySegmentGroupByMergeName = "metrics.collect.test.GroupByMergeOnSingleSegment";//include get single segment result and merge to single server result
    public static final String subqueryNodeGroupByAggregateName = "metrics.collect.test.GroupBySubqueryOnNode";//include aggregate and sort
    public static final String querySegmentSearchComputeName = "metrics.collect.test.SearchComputeOnSingleSegment";//include scan and compute
    public static final String querySegmentSearchCursorExecutorName = "metrics.collect.test.SearchExecuteBaseCursor";//unsupport index dimensions include make cursor
    public static final String querySegmentSearchIndexExecutorName = "metrics.collect.test.SearchExecuteBaseIndex";//support index dimensions
    public static final String querySegmentSelectComputeName = "metrics.collect.test.SelectComputeOnSingleSegment";//scan and compute

    //time for io
    public static final String querySegmentBitmapConstructionName = "metrics.collect.test.BitmapConstructionOnSegment";//prefilters not null construct bitmap
    public static final String querySegmentMakeCursorName = "metrics.collect.test.SegmentMakeCursor";//queryableindex cursor create after postfilters
    public static final String queryIncrementalMakeCursorName = "metrics.collect.test.IncrementalMakeCursor";//realtime cursor create
    public static final String queryLoadIndexOnDiskName = "metrics.collect.test.LoadIndexOnDisk";//index files unzip for loading segment in disk
    public static final String queryLoadSegmentOnDiskName = "metrics.collect.test.LoadSegmentOnDisk";
    public static final String queryLoadBitmapOffHeapName = "metrics.collect.test.LoadBitmapOffheap";
    public static final String queryLoadBitmapOthersName = "metrics.collect.test.LoadBitmapOthers";

    //cache hit rate
    public static final String cacheHitBrokerName = "metrics.collect.test.CacheHitBroker";
    public static final String cacheNotHitBrokerName = "metrics.collect.test.CacheNotHitBroker";
    public static final String cacheHitHistoricalName = "metrics.collect.test.CacheHitHistorical";
    public static final String cacheNotHitHistoricalName = "metrics.collect.test.CacheNotHitHistorical";

    public static TimedMetric queryTime = MetricManager.getTimedMetric(CollectMetrics.queryTimeName);
    public static TimedMetric queryTimeSequence = MetricManager.getTimedMetric(CollectMetrics.queryTimeSequenceName);
    public static TimedMetric queryTimeToYielder = MetricManager.getTimedMetric(CollectMetrics.queryTimeToYielderName);
    public static TimedMetric queryFromCacheBroker = MetricManager.getTimedMetric(CollectMetrics.queryFromCacheBrokerName);//should delete
    public static TimedMetric queryFromServerBroker = MetricManager.getTimedMetric(CollectMetrics.queryFromServerBrokerName);//should delete
    public static ValueMetric cacheHitBroker = MetricManager.getValueMetric(CollectMetrics.cacheHitBrokerName);
    public static ValueMetric cacheNotHitBroker = MetricManager.getValueMetric(CollectMetrics.cacheNotHitBrokerName);
    public static TimedMetric queryFromSingleServerBroker = MetricManager.getTimedMetric(CollectMetrics.queryFromSingleServerBrokerName);//should delete
    public static TimedMetric queryNodeTtfb = MetricManager.getTimedMetric(CollectMetrics.queryNodeTtfbName);
    public static TimedMetric queryNodeTime = MetricManager.getTimedMetric(CollectMetrics.queryNodeTimeName);
    public static TimedMetric querySegmentTime = MetricManager.getTimedMetric(CollectMetrics.querySegmentTimeName);
    public static TimedMetric queryNodeMergeResults = MetricManager.getTimedMetric(CollectMetrics.queryNodeMergeResultsName);
    public static TimedMetric querySegmentTimeseriesAggregate = MetricManager.getTimedMetric(CollectMetrics.querySegmentTimeseriesAggregateName);
    public static TimedMetric querySegmentGroupByMerge = MetricManager.getTimedMetric(CollectMetrics.querySegmentGroupByMergeName);
    public static TimedMetric subqueryNodeGroupByAggregate = MetricManager.getTimedMetric(CollectMetrics.subqueryNodeGroupByAggregateName);
    public static TimedMetric querySegmentSearchCompute = MetricManager.getTimedMetric(CollectMetrics.querySegmentSearchComputeName);
    public static TimedMetric querySegmentSearchCursorExecutor = MetricManager.getTimedMetric(CollectMetrics.querySegmentSearchCursorExecutorName);
    public static TimedMetric querySegmentSearchIndexExecutor = MetricManager.getTimedMetric(CollectMetrics.querySegmentSearchIndexExecutorName);
    public static TimedMetric querySegmentTopNAggregate = MetricManager.getTimedMetric(CollectMetrics.querySegmentTopNAggregateName);
    public static TimedMetric querySegmentSelectCompute = MetricManager.getTimedMetric(CollectMetrics.querySegmentSelectComputeName);
    public static TimedMetric querySegmentBitmapConstruction = MetricManager.getTimedMetric(CollectMetrics.querySegmentBitmapConstructionName);
    public static TimedMetric querySegmentMakeCursor = MetricManager.getTimedMetric(CollectMetrics.querySegmentMakeCursorName);
    public static TimedMetric queryIncrementalMakeCursor = MetricManager.getTimedMetric(CollectMetrics.queryIncrementalMakeCursorName);
    public static TimedMetric queryLoadIndexOnDisk = MetricManager.getTimedMetric(CollectMetrics.queryLoadIndexOnDiskName);
    public static TimedMetric queryLoadSegmentOnDisk = MetricManager.getTimedMetric(CollectMetrics.queryLoadSegmentOnDiskName);
    public static TimedMetric queryLoadBitmapOffHeap = MetricManager.getTimedMetric(CollectMetrics.queryLoadBitmapOffHeapName);
    public static TimedMetric queryLoadBitmapOthers = MetricManager.getTimedMetric(CollectMetrics.queryLoadBitmapOthersName);
    public static CounterMetric cacheHitHistorical = MetricManager.getCounterMetric(CollectMetrics.cacheHitHistoricalName);
    public static CounterMetric cacheNotHitHistorical = MetricManager.getCounterMetric(CollectMetrics.cacheNotHitHistoricalName);

}


