<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
WorkflowDefinitionVersion currentWorkflowDefinitionVersion = (WorkflowDefinitionVersion)request.getAttribute(WebKeys.WORKFLOW_DEFINITION_VERSION);
%>

<liferay-ui:search-container
	id="workflowDefinitionVersions"
>
	<liferay-ui:search-container-results
		results="<%= workflowDefinitionDisplayContext.getWorkflowDefinitionVersions(currentWorkflowDefinitionVersion.getName()) %>"
	/>

	<liferay-ui:search-container-row
		className="com.liferay.portal.kernel.workflow.WorkflowDefinitionVersion"
		modelVar="workflowDefinitionVersion"
	>
		<liferay-ui:search-container-column-text
			name="version"
			value="<%= workflowDefinitionDisplayContext.getVersion(workflowDefinitionVersion) %>"
		/>

		<liferay-ui:search-container-column-jsp
			path="/workflow_definition_version_action.jsp"
		/>
	</liferay-ui:search-container-row>

	<liferay-ui:search-iterator displayStyle="list" markupView="lexicon" />
</liferay-ui:search-container>