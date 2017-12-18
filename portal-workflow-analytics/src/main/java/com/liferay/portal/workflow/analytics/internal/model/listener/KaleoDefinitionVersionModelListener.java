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
public class KaleoDefinitionVersionModelListener
	extends BaseModelListener<KaleoDefinitionVersion> {

	@Override
	public void onAfterCreate(KaleoDefinitionVersion kaleoDefinitionVersion)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				kaleoDefinitionVersion);

			Date date = kaleoDefinitionVersion.getCreateDate();

			OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoDefinitionVersion.getUserId()),
					Event.KALEO_DEFINITION_VERSION_CREATE.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterRemove(KaleoDefinitionVersion kaleoDefinitionVersion)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				kaleoDefinitionVersion);

			OffsetDateTime offsetDateTime = OffsetDateTime.now();

			properties.put("date", offsetDateTime.toString());

			AnalyticsEventsMessage analyticsEventsMessage =
				WorkflowAnalyticsHelper.buildMessage(
					String.valueOf(kaleoDefinitionVersion.getUserId()),
					Event.KALEO_DEFINITION_VERSION_REMOVE.name(), properties);

			_analyticsClient.sendAnalytics(analyticsEventsMessage);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	protected Map<String, String> createProperties(
			KaleoDefinitionVersion kaleoDefinitionVersion)
		throws PortalException {

		Map<String, String> properties = new HashMap<>();

		properties.put("description", kaleoDefinitionVersion.getDescription());

		KaleoDefinition kaleoDefinition =
			kaleoDefinitionVersion.getKaleoDefinition();

		properties.put(
			"kaleoDefinitionId",
			String.valueOf(kaleoDefinition.getKaleoDefinitionId()));

		properties.put(
			"kaleoDefinitionVersionId",
			String.valueOf(
				kaleoDefinitionVersion.getKaleoDefinitionVersionId()));
		properties.put("name", kaleoDefinitionVersion.getName());
		properties.put("title", kaleoDefinitionVersion.getTitle());
		properties.put(
			"userId", String.valueOf(kaleoDefinitionVersion.getUserId()));
		properties.put(
			"version", String.valueOf(kaleoDefinitionVersion.getVersion()));

		return properties;
	}

	@Reference
	private AnalyticsClient _analyticsClient;

}