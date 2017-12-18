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
import com.liferay.portal.workflow.analytics.internal.metrics.Event;
import com.liferay.portal.workflow.analytics.internal.util.WorkflowAnalyticsHelper;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;

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
public class KaleoTaskAssignmentInstanceModelListener
	extends BaseModelListener<KaleoTaskAssignmentInstance> {

	@Override
	public void onAfterCreate(
			KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				kaleoTaskAssignmentInstance);

			Date date = kaleoTaskAssignmentInstance.getCreateDate();

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoTaskAssignmentInstance.getUserId()),
					Event.KALEO_TASK_ASSIGNMENT_INSTANCE_CREATE.name(),
					properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterRemove(
			KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance)
		throws ModelListenerException {

		try {
			if (kaleoTaskAssignmentInstance.isCompleted()) {
				return;
			}

			Map<String, String> properties = createProperties(
				kaleoTaskAssignmentInstance);

			OffsetDateTime offsetDateTime = OffsetDateTime.now();

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoTaskAssignmentInstance.getUserId()),
					Event.KALEO_TASK_ASSIGNMENT_INSTANCE_DELETE.name(),
					properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterUpdate(
			KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				kaleoTaskAssignmentInstance);

			Event event = Event.KALEO_TASK_ASSIGNMENT_INSTANCE_UPDATE;

			Date date = kaleoTaskAssignmentInstance.getModifiedDate();

			if (kaleoTaskAssignmentInstance.isCompleted()) {
				event = Event.KALEO_TASK_ASSIGNMENT_INSTANCE_COMPLETE;

				Duration duration = Duration.between(
					kaleoTaskAssignmentInstance.getCreateDate().toInstant(),
					kaleoTaskAssignmentInstance.getCompletionDate().
						toInstant());

				properties.put(
					"duration", String.valueOf(duration.getSeconds()));

				date = kaleoTaskAssignmentInstance.getCompletionDate();
			}

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoTaskAssignmentInstance.getUserId()),
					event.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	protected Map<String, String> createProperties(
		KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance) {

		Map<String, String> properties = new HashMap<>();

		properties.put(
			"className", kaleoTaskAssignmentInstance.getAssigneeClassName());
		properties.put(
			"classPK",
			String.valueOf(kaleoTaskAssignmentInstance.getAssigneeClassPK()));
		properties.put(
			"kaleoDefinitionVersionId",
			String.valueOf(
				kaleoTaskAssignmentInstance.getKaleoDefinitionVersionId()));
		properties.put(
			"kaleoTaskAssignmentInstanceId",
			String.valueOf(
				kaleoTaskAssignmentInstance.
					getKaleoTaskAssignmentInstanceId()));
		properties.put(
			"kaleoTaskId",
			String.valueOf(kaleoTaskAssignmentInstance.getKaleoTaskId()));
		properties.put(
			"userId", String.valueOf(kaleoTaskAssignmentInstance.getUserId()));

		return properties;
	}

	@Reference
	private AnalyticsClient _analyticsClient;

}