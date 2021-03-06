/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.rabbitmqClient;

import java.util.Collections;
import java.util.List;
import java.util.Collection;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

abstract class AbstractRabbitMetricsGenerator extends AbstractMetricsGenerator {
	public static final String RABBIT_COUNT_SUFFIX = ":type=counter";

	private final String   rabbitMetricKey;

	AbstractRabbitMetricsGenerator(RabbitPluginOperationType rabbitOpType) {
		super(rabbitOpType.getOperationType());
		rabbitMetricKey = rabbitOpType.getOperationType().getName() + RABBIT_COUNT_SUFFIX;
	}

	@Override
    protected Collection<MetricsBag> addExtraEndPointMetrics(Trace trace, ResourceKey resourceKey, Collection<Frame> externalFrames) {
        if ((externalFrames == null) || externalFrames.isEmpty()) {
            return Collections.emptyList();
        }

        MetricsBag    mb=MetricsBag.create(resourceKey, trace.getRange());
        addCounterMetricToBag(trace, mb, createMetricKey(), externalFrames.size());
        return Collections.singletonList(mb);
	}

	@Override
	protected void addExtraExternalResourceMetrics(Trace trace,  Frame opTypeFrame, MetricsBag mb) {
		addCounterMetricToBag(trace, mb, createMetricKey(), 1);
	}

	@Override
	protected List<Frame> getExternalFramesForMetricGeneration(Trace trace) {
		return trace.getLastFramesOfType(opType);
	}

	final String createMetricKey() {
		return rabbitMetricKey;
	}
}
