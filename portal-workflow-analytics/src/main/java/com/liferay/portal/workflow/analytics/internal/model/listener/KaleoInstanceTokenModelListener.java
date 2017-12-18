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

package com.liferay.portal.workflow.analytics.internal.model.listener;

import com.liferay.analytics.client.AnalyticsClient;
import com.liferay.analytics.model.AnalyticsEventsMessage;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.workflow.analytics.internal.metrics.Event;
import com.liferay.portal.workflow.analytics.internal.util.WorkflowAnalyticsHelper;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = ModelListener.class)
public class KaleoInstanceTokenModelListener
	extends BaseModelListener<KaleoInstanceToken> {

	@Override
	public void onAfterCreate(KaleoInstanceToken kaleoInstanceToken)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				kaleoInstanceToken);

			Date date = kaleoInstanceToken.getCreateDate();

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoInstanceToken.getUserId()),
					Event.KALEO_INSTANCE_TOKEN_CREATE.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterRemove(KaleoInstanceToken kaleoInstanceToken)
		throws ModelListenerException {

		try {
			if (kaleoInstanceToken.isCompleted()) {
				return;
			}

			Map<String, String> properties = createProperties(
				kaleoInstanceToken);

			OffsetDateTime offsetDateTime = OffsetDateTime.now();

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoInstanceToken.getUserId()),
					Event.KALEO_INSTANCE_TOKEN_REMOVE.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterUpdate(KaleoInstanceToken kaleoInstanceToken)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				kaleoInstanceToken);

			Event event = Event.KALEO_INSTANCE_TOKEN_UPDATE;

			Date date = kaleoInstanceToken.getModifiedDate();

			if (kaleoInstanceToken.isCompleted()) {
				event = Event.KALEO_INSTANCE_TOKEN_COMPLETE;

				Duration duration = Duration.between(
					kaleoInstanceToken.getCreateDate().toInstant(),
					kaleoInstanceToken.getCompletionDate().toInstant());

				properties.put(
					"duration", String.valueOf(duration.getSeconds()));

				date = kaleoInstanceToken.getCompletionDate();
			}

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoInstanceToken.getUserId()),
					event.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	protected Map<String, String> createProperties(
		KaleoInstanceToken kaleoInstanceToken) {

		Map<String, String> properties = new HashMap<>();

		properties.put("className", kaleoInstanceToken.getClassName());
		properties.put(
			"classPK", String.valueOf(kaleoInstanceToken.getClassPK()));
		properties.put(
			"currentKaleoNodeId",
			String.valueOf(kaleoInstanceToken.getCurrentKaleoNodeId()));
		properties.put(
			"currentKaleoNodeName",
			kaleoInstanceToken.getCurrentKaleoNodeName());
		properties.put(
			"kaleoInstanceId",
			String.valueOf(kaleoInstanceToken.getKaleoInstanceId()));
		properties.put(
			"kaleoInstanceTokenId",
			String.valueOf(kaleoInstanceToken.getKaleoInstanceTokenId()));
		properties.put(
			WorkflowConstants.CONTEXT_COMPANY_ID,
			String.valueOf(kaleoInstanceToken.getCompanyId()));
		properties.put(
			WorkflowConstants.CONTEXT_GROUP_ID,
			String.valueOf(kaleoInstanceToken.getGroupId()));
		properties.put(
			"userId", String.valueOf(kaleoInstanceToken.getUserId()));

		return properties;
	}

	@Reference
	private AnalyticsClient _analyticsClient;

}