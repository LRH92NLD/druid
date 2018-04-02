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

package io.druid.emitter.opentsdb;

import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.Event;
import com.metamx.emitter.service.AlertEvent;
import com.metamx.emitter.service.ServiceMetricEvent;
import io.druid.emitter.opentsdb.MemoryBoundLinkedBlockingQueue.ObjectContainer;
import io.druid.java.util.common.ISE;
import io.druid.java.util.common.StringUtils;
import io.druid.java.util.common.lifecycle.LifecycleStart;
import io.druid.java.util.common.lifecycle.LifecycleStop;
import io.druid.java.util.common.logger.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liangrenhua on 18-2-6.
 */
public class OpentsdbEmitter implements Emitter{
    private static Logger log = new Logger(OpentsdbEmitter.class);
    private final OpentsdbEmitterConfig config;
    private final String url;
    private final String clusterName;
    private final List<Emitter> emitterList;
    private final CloseableHttpClient client;
    private final ScheduledExecutorService scheduler;
    private final MemoryBoundLinkedBlockingQueue<String> metricQueue;
    private final AtomicLong metricLost;
    private final AtomicLong invalidLost;
    private final AtomicBoolean started;
    private final String KEY_METRIC = "metric";
    private final String KEY_TIMESTAMP = "timestamp";
    private final String KEY_VALUE = "value";
    private final String KEY_TAGS = "tags";
    private final String TAG_CLUSTER = "clusterName";

    public OpentsdbEmitter(OpentsdbEmitterConfig config, List<Emitter> emitterList) {
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.client = HttpClientBuilder.create().setConnectionManager(new PoolingHttpClientConnectionManager()).build();
        this.url = config.getUrl();
        this.clusterName = config.getCluster();
        this.emitterList = emitterList;
        long queueMemoryBound = config.getBufferSize();
        this.metricQueue = new MemoryBoundLinkedBlockingQueue<>(queueMemoryBound);
        this.metricLost = new AtomicLong(0L);
        this.invalidLost = new AtomicLong(0L);
        this.started = new AtomicBoolean(false);
    }

    @Override
    @LifecycleStart
    public void start() {
        boolean alreadyStarted = started.getAndSet(true);
        if(!alreadyStarted) {
            scheduler.scheduleWithFixedDelay(()->{
                String putPath = String.format("http://%s/api/put",url);
                ObjectContainer<String> objectToSend;
                try {
                    while (true) {
                        objectToSend = metricQueue.take();
                        HttpPost post = new HttpPost(putPath);
                        post.setEntity(new StringEntity(objectToSend.getData(), ContentType.APPLICATION_JSON));
                        HttpResponse httpResponse = client.execute(post);
                        if (httpResponse.getStatusLine().getStatusCode() == 400) {
                            invalidLost.incrementAndGet();
                            log.error("metric is invalid!\n"+EntityUtils.toString(httpResponse.getEntity()));
                        } else if (httpResponse.getStatusLine().getStatusCode() / 100 != 2) {
                            invalidLost.incrementAndGet();
                            log.error("Failed to send metric to opentsdb!\n"+EntityUtils.toString(httpResponse.getEntity()));
                        }
                        EntityUtils.consume(httpResponse.getEntity());
                    }
                } catch (InterruptedException e) {
                    log.error(e, "Failed to take metric from queue!");
                } catch (IOException e) {
                    log.error(e,"Failed to send metric to opentsdb!");
                }
            }, config.getMetricPeriod(), config.getMetricPeriod(), TimeUnit.SECONDS);
            scheduler.scheduleWithFixedDelay(() -> {
                log.info("Message lost counter: metricLost=[%d], invalidLost=[%d], metricQueue=[%d]",
                        metricLost.get(), invalidLost.get(), metricQueue.size());
            }, config.getLogPeriod(), config.getLogPeriod(), TimeUnit.MINUTES);
            log.info("Starting OpenTSDB Emitter.");
        }
    }

    @Override
    public void emit(Event event) {
        if (!started.get()) {
            throw new ISE("WTF emit was called while service is not started yet");
        }
        if(event != null) {
            if (event instanceof ServiceMetricEvent) {
                String message = convertEventToJsonString((ServiceMetricEvent) event);
                if(message == null){
                    log.error("The metric format is invalid! " +
                            "The metric has to be discarded.");
                    invalidLost.incrementAndGet();
                    return;
                }

                ObjectContainer<String> objectContainer = new ObjectContainer<>(
                        message,
                        StringUtils.toUtf8(message).length
                );

                if (!metricQueue.offer(objectContainer)) {
                    log.error("Failed to insert metric to the queue!");
                    metricLost.incrementAndGet();
                }
            } else if (!emitterList.isEmpty() && event instanceof AlertEvent) {
                for (Emitter emitter : emitterList) {
                    emitter.emit(event);
                }
            } else if (event instanceof AlertEvent) {
                AlertEvent alertEvent = (AlertEvent) event;
                log.error(
                        "The following alert is dropped, description is [%s], severity is [%s]",
                        alertEvent.getDescription(), alertEvent.getSeverity()
                );
            } else {
                log.error("unknown event type [%s]", event.getClass());
            }
        }
    }

    private String convertEventToJsonString(ServiceMetricEvent serviceMetricEvent){
        try {
            Map<String, Object> event = new HashMap<>(serviceMetricEvent.toMap());
            JSONObject object = new JSONObject();
            if (event.containsKey(KEY_TIMESTAMP) && event.containsKey(KEY_METRIC) && event.containsKey(KEY_VALUE)) {
                String regex = "([^a-zA-Z0-9_./\\-])";
                long timestamp = Timestamp.valueOf(event.get(KEY_TIMESTAMP).toString()
                        .replace("T", " ").replace("Z", "")).getTime();
                String metric = event.get(KEY_METRIC).toString().replaceAll(regex, "_");
                double value = Double.valueOf(event.get(KEY_VALUE).toString());
                event.remove(KEY_TIMESTAMP);
                event.remove(KEY_METRIC);
                event.remove(KEY_VALUE);
                event.put(TAG_CLUSTER, clusterName);
                JSONObject tags = new JSONObject();
                for (Map.Entry<String, Object> entry : event.entrySet()) {
                    String tagKey = entry.getKey().replaceAll(regex, "_");
                    String tagValue = entry.getValue().toString().replaceAll(regex, "_");
                    tags.put(tagKey, tagValue);
                }
                object.put(KEY_TIMESTAMP, timestamp);
                object.put(KEY_METRIC, metric);
                object.put(KEY_VALUE, value);
                object.put(KEY_TAGS, tags);
                return object.toJSONString();
            }
        } catch (Exception e){
            log.error(e, e.getMessage());
        }
        return null;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    @LifecycleStop
    public void close() throws IOException {
        boolean wasStarted = this.started.getAndSet(false);
        client.close();
        if (wasStarted) {
            scheduler.shutdownNow();
        }
    }
}
