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
import com.liferay.portal.workflow.kaleo.model.KaleoDefinition;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionVersionLocalService;

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
public class KaleoInstanceModelListener
	extends BaseModelListener<KaleoInstance> {

	@Override
	public void onAfterCreate(KaleoInstance kaleoInstance)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(kaleoInstance);

			Date date = kaleoInstance.getCreateDate();

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoInstance.getUserId()),
					Event.KALEO_INSTANCE_CREATE.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterRemove(KaleoInstance kaleoInstance)
		throws ModelListenerException {

		try {
			if (kaleoInstance.isCompleted()) {
				return;
			}

			Map<String, String> properties = createProperties(kaleoInstance);

			OffsetDateTime offsetDateTime = OffsetDateTime.now();

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoInstance.getUserId()),
					Event.KALEO_INSTANCE_REMOVE.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterUpdate(KaleoInstance kaleoInstance)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(kaleoInstance);

			Event event = Event.KALEO_INSTANCE_UPDATE;

			Date date = kaleoInstance.getModifiedDate();

			if (kaleoInstance.isCompleted()) {
				event = Event.KALEO_INSTANCE_COMPLETE;

				Duration duration = Duration.between(
					kaleoInstance.getCreateDate().toInstant(),
					kaleoInstance.getCompletionDate().toInstant());

				properties.put(
					"duration", String.valueOf(duration.getSeconds()));

				date = kaleoInstance.getCompletionDate();
			}

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoInstance.getUserId()), event.name(),
					properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	protected Map<String, String> createProperties(KaleoInstance kaleoInstance)
		throws PortalException {

		Map<String, String> properties = new HashMap<>();

		properties.put("className", kaleoInstance.getClassName());
		properties.put("classPK", String.valueOf(kaleoInstance.getClassPK()));

		long kaleoDefinitionVersionId =
			kaleoInstance.getKaleoDefinitionVersionId();

		KaleoDefinitionVersion kaleoDefinitionVersion =
			_kaleoDefinitionVersionLocalService.getKaleoDefinitionVersion(
				kaleoDefinitionVersionId);

		KaleoDefinition kaleoDefinition =
			kaleoDefinitionVersion.getKaleoDefinition();

		properties.put(
			"kaleoDefinitionId",
			String.valueOf(kaleoDefinition.getKaleoDefinitionId()));

		properties.put(
			"kaleoDefinitionVersionId",
			String.valueOf(kaleoDefinitionVersionId));
		properties.put(
			"kaleoInstanceId",
			String.valueOf(kaleoInstance.getKaleoInstanceId()));
		properties.put("userId", String.valueOf(kaleoInstance.getUserId()));

		return properties;
	}

	@Reference
	private AnalyticsClient _analyticsClient;

	@Reference
	private KaleoDefinitionVersionLocalService
		_kaleoDefinitionVersionLocalService;

}