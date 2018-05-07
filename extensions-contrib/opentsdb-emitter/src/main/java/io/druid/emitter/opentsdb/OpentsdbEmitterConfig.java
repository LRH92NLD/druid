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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;

public class OpentsdbEmitterConfig
{
  private final static long DEFAULT_BUFFER_SIZE = 33554432;
  private final static long DEFAULT_LOG_PERIOD = 5;
  private final static long DEFAULT_METRIC_PERIOD = 10;

  @JsonProperty("server.url")
  private final String url;
  @JsonProperty("buffer.size")
  private final Long bufferSize;
  @JsonProperty("cluster.name")
  private final String cluster;
  @JsonProperty
  private final List<String> alertEmitters;
  @JsonProperty
  private final Long metricPeriod;
  @JsonProperty
  private final Long logPeriod;

  @JsonCreator
  public OpentsdbEmitterConfig(
      @JsonProperty("server.url") String url,
      @JsonProperty("buffer.size") Long bufferSize,
      @JsonProperty("cluster.name") String cluster,
      @JsonProperty("alertEmitters") List<String> alertEmitters,
      @JsonProperty("metricPeriod") Long metricPeriod,
      @JsonProperty("logPeriod") Long logPeriod
  )
  {
    this.url = Preconditions.checkNotNull(url, "server.url can not be null");
    this.bufferSize = bufferSize == null ? DEFAULT_BUFFER_SIZE : bufferSize;
    this.cluster = Preconditions.checkNotNull(cluster, "cluster.name can not be null.");
    if (!this.cluster.matches("([a-zA-Z0-9_./\\-]*)")) {
      throw new NullPointerException("Only the following characters are allowed in cluster.name: " +
                                     "a to z, A to Z, 0 to 9, -, _, . or /");
    }
    this.alertEmitters = alertEmitters == null ? Collections.<String>emptyList() : alertEmitters;
    this.metricPeriod = metricPeriod == null ? DEFAULT_METRIC_PERIOD : metricPeriod;
    this.logPeriod = logPeriod == null ? DEFAULT_LOG_PERIOD : logPeriod;
  }

  @JsonProperty
  public String getUrl()
  {
    return url;
  }

  @JsonProperty
  public Long getBufferSize()
  {
    return bufferSize;
  }

  @JsonProperty
  public String getCluster()
  {
    return cluster;
  }

  @JsonProperty
  public List<String> getAlertEmitters() { return alertEmitters; }

  @JsonProperty
  public Long getMetricPeriod() { return metricPeriod; }

  @JsonProperty
  public Long getLogPeriod() { return logPeriod; }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    OpentsdbEmitterConfig that = (OpentsdbEmitterConfig) o;

    if (!getUrl().equals(that.getUrl())) {
      return false;
    }
    if (!getBufferSize().equals(that.getBufferSize())) {
      return false;
    }
    if (!getMetricPeriod().equals(that.getMetricPeriod())) {
      return false;
    }
    if (!getLogPeriod().equals(that.getLogPeriod())) {
      return false;
    }
    if (!getCluster().equals(that.getCluster())) {
      return false;
    }
    if (getAlertEmitters() != null
        ? !getAlertEmitters().equals(that.getAlertEmitters())
        : that.getAlertEmitters() != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = getUrl().hashCode();
    result = 31 * result + getCluster().hashCode();
    result = 31 * result + getBufferSize().hashCode();
    result = 31 * result + getMetricPeriod().hashCode();
    result = 31 * result + getLogPeriod().hashCode();
    result = 31 * result + (getAlertEmitters() != null ? getAlertEmitters().hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return "OpentsdbEmitterConfig{" +
           "server.url='" + url + '\'' +
           ", buffer.size='" + bufferSize + '\'' +
           ", metricPeriod='" + metricPeriod + '\'' +
           ", logPeriod='" + logPeriod + '\'' +
           ", cluster.name='" + cluster + '\'' +
           ", alertEmitters='" + alertEmitters.toString() + '\'' +
           '}';
  }
}
