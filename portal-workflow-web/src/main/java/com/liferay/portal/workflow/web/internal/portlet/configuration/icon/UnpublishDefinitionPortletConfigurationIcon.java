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

package com.liferay.portal.workflow.web.internal.portlet.configuration.icon;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.configuration.icon.BasePortletConfigurationIcon;
import com.liferay.portal.kernel.portlet.configuration.icon.PortletConfigurationIcon;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.workflow.web.internal.constants.WorkflowPortletKeys;

import java.util.ResourceBundle;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

/**
 * Configuration icon to allow the deactivation of a workflow definition.
 * @review
 * @author Jeyvison Nascimento
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + WorkflowPortletKeys.CONTROL_PANEL_WORKFLOW,
		"path=/definition/edit_workflow_definition.jsp"
	},
	service = PortletConfigurationIcon.class
)
public class UnpublishDefinitionPortletConfigurationIcon
	extends BasePortletConfigurationIcon {

	@Override
	public String getMessage(PortletRequest portletRequest) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", getLocale(portletRequest), getClass());

		return LanguageUtil.get(resourceBundle, "unpublish");
	}

	/**
	 * Creates and returns an action URL passing the workflow
	 * definition name and version as parameters.
	 * @review
	 */
	@Override
	public String getURL(
		PortletRequest portletRequest, PortletResponse portletResponse) {

		PortletURL actionPortlet =
			((RenderResponse)portletResponse).createActionURL();

		actionPortlet.setParameter(
			"javax.portlet.action", "deactivateWorkflowDefinition");
		actionPortlet.setParameter("name", portletRequest.getParameter("name"));
		actionPortlet.setParameter(
			"version", portletRequest.getParameter("version"));

		return actionPortlet.toString();
	}

	@Override
	public boolean isShow(PortletRequest portletRequest) {
		return true;
	}

}