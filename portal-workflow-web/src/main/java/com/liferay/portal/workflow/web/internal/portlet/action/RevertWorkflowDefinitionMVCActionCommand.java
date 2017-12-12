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

package com.liferay.portal.workflow.web.internal.portlet.action;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionManagerUtil;
import com.liferay.portal.workflow.web.internal.constants.WorkflowPortletKeys;

import java.text.Format;

import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + WorkflowPortletKeys.CONTROL_PANEL_WORKFLOW,
		"mvc.command.name=revertWorkflowDefinition"
	},
	service = MVCActionCommand.class
)
public class RevertWorkflowDefinitionMVCActionCommand
	extends UpdateWorkflowDefinitionMVCActionCommand {

	/**
	 * Adds a success message to the workflow definition reversion action
	 *
	 * @param actionRequest The actionRequest object of the action
	 * @param workflowDefinition The workflow definition that will be reverted back to published.
	 *
	 * @review
	 */
	protected void addSuccessMessage(
		ActionRequest actionRequest, WorkflowDefinition workflowDefinition) {

		Locale locale = actionRequest.getLocale();

		HttpServletRequest request = _portal.getHttpServletRequest(
			actionRequest);

		Format dateFormatTime = null;

		if (!DateUtil.isFormatAmPm(locale)) {
			dateFormatTime = FastDateFormatFactoryUtil.getSimpleDateFormat(
				"MMM d, yyyy, HH:mm", locale);
		}
		else {
			dateFormatTime = FastDateFormatFactoryUtil.getSimpleDateFormat(
				"MMM d, yyyy, hh:mm a", locale);
		}

		String dateTime = dateFormatTime.format(
			workflowDefinition.getModifiedDate());

		String message = LanguageUtil.get(
			request, "restored-to-revision-from-x");

		SessionMessages.add(
			actionRequest, "requestProcessed",
			LanguageUtil.format(
				locale,
				LanguageUtil.get(request, "restored-to-revision-from-x"),
				dateTime));
	}

	/**
	 * Reverts a workflow definition to the published state, creating a new version of it.
	 *
	 * @param actionRequest
	 * @param actionResponse
	 *
	 * @review
	 */
	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		String name = ParamUtil.getString(actionRequest, "name");
		int version = ParamUtil.getInteger(actionRequest, "version");

		WorkflowDefinition workflowDefinition =
			WorkflowDefinitionManagerUtil.getWorkflowDefinition(
				themeDisplay.getCompanyId(), name, version);

		String content = workflowDefinition.getContent();

		workflowDefinitionManager.deployWorkflowDefinition(
			themeDisplay.getCompanyId(), themeDisplay.getUserId(),
			workflowDefinition.getTitle(), content.getBytes());

		addSuccessMessage(actionRequest, workflowDefinition);
	}

	@Reference
	private Portal _portal;

}