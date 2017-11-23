/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.analytics.internal.util;

import com.liferay.analytics.model.AnalyticsEventsMessage;
import com.liferay.analytics.model.AnalyticsEventsMessage.Builder;
import com.liferay.analytics.model.AnalyticsEventsMessage.Event;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;

import java.util.Map;

/**
 * @author Inácio Nery
 */
public class WorkflowAnalyticsHelper {

	public static AnalyticsEventsMessage buildMessage(
			String userId, String eventId, Map<String, String> properties)
		throws Exception {

		AnalyticsEventsMessage.Event.Builder eventBuilder =
			AnalyticsEventsMessage.Event.builder(_APPLICATION_ID, eventId);

		eventBuilder.properties(properties);

		Event build = eventBuilder.build();

		Builder builder = AnalyticsEventsMessage.builder(
			_ANALYTICS_KEY, userId).event(build);

		return builder.build();
	}

	private static final String _ANALYTICS_KEY = System.getProperty(
		"analytics.key", CompanyThreadLocal.getCompanyId().toString());

	private static final String _APPLICATION_ID =
		"com.liferay.portal.workflow.analytics:1.0.0";

}