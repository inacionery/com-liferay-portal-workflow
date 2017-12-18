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
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.workflow.analytics.internal.metrics.Event;
import com.liferay.portal.workflow.analytics.internal.util.WorkflowAnalyticsHelper;
import com.liferay.portal.workflow.kaleo.model.KaleoTask;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskLocalService;

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
public class KaleoTaskInstanceTokenModelListener
	extends BaseModelListener<KaleoTaskInstanceToken> {

	@Override
	public void onAfterCreate(KaleoTaskInstanceToken kaleoTaskInstanceToken)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				kaleoTaskInstanceToken);

			Date date = kaleoTaskInstanceToken.getCreateDate();

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoTaskInstanceToken.getUserId()),
					Event.KALEO_TASK_INSTANCE_TOKEN_CREATE.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterRemove(KaleoTaskInstanceToken kaleoTaskInstanceToken)
		throws ModelListenerException {

		try {
			if (kaleoTaskInstanceToken.isCompleted()) {
				return;
			}

			Map<String, String> properties = createProperties(
				kaleoTaskInstanceToken);

			OffsetDateTime offsetDateTime = OffsetDateTime.now();

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoTaskInstanceToken.getUserId()),
					Event.KALEO_TASK_INSTANCE_TOKEN_REMOVE.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterUpdate(KaleoTaskInstanceToken kaleoTaskInstanceToken)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				kaleoTaskInstanceToken);

			Event event = Event.KALEO_TASK_INSTANCE_TOKEN_UPDATE;

			Date date = kaleoTaskInstanceToken.getModifiedDate();

			if (kaleoTaskInstanceToken.isCompleted()) {
				event = Event.KALEO_TASK_INSTANCE_TOKEN_COMPLETE;

				Duration duration = Duration.between(
					kaleoTaskInstanceToken.getCreateDate().toInstant(),
					kaleoTaskInstanceToken.getCompletionDate().toInstant());

				properties.put(
					"duration", String.valueOf(duration.getSeconds()));

				date = kaleoTaskInstanceToken.getCompletionDate();
			}

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoTaskInstanceToken.getUserId()),
					event.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	protected Map<String, String> createProperties(
			KaleoTaskInstanceToken kaleoTaskInstanceToken)
		throws PortalException {

		Map<String, String> properties = new HashMap<>();

		properties.put("className", kaleoTaskInstanceToken.getClassName());
		properties.put(
			"classPK", String.valueOf(kaleoTaskInstanceToken.getClassPK()));
		properties.put(
			"kaleoDefinitionVersionId",
			String.valueOf(
				kaleoTaskInstanceToken.getKaleoDefinitionVersionId()));
		properties.put(
			"kaleoInstanceId",
			String.valueOf(kaleoTaskInstanceToken.getKaleoInstanceId()));
		properties.put(
			"kaleoInstanceTokenId",
			String.valueOf(kaleoTaskInstanceToken.getKaleoInstanceTokenId()));

		long kaleoTaskId = kaleoTaskInstanceToken.getKaleoTaskId();

		properties.put("kaleoTaskId", String.valueOf(kaleoTaskId));

		KaleoTask kaleoTask = _kaleoTaskLocalService.getKaleoTask(kaleoTaskId);

		properties.put("name", kaleoTask.getName());

		properties.put(
			"userId", String.valueOf(kaleoTaskInstanceToken.getUserId()));

		return properties;
	}

	@Reference
	private AnalyticsClient _analyticsClient;

	@Reference
	private KaleoTaskLocalService _kaleoTaskLocalService;

}