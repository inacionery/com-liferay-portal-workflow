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

package com.liferay.portal.workflow.kaleo.runtime.integration.impl.internal;

import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionVersion;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionVersionManager;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.workflow.kaleo.KaleoWorkflowModelConverter;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.runtime.util.comparator.KaleoDefinitionVersionOrderByComparator;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionVersionLocalService;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true, property = {"proxy.bean=false"},
	service = WorkflowDefinitionVersionManager.class
)
public class WorkflowDefinitionVersionManagerImpl
	implements WorkflowDefinitionVersionManager {

	@Override
	public WorkflowDefinitionVersion getWorkflowDefinitionVersion(
			long companyId, String name, String version)
		throws WorkflowException {

		try {
			KaleoDefinitionVersion kaleoDefinitionVersion =
				_kaleoDefinitionVersionLocalService.getKaleoDefinitionVersion(
					companyId, name, version);

			return _kaleoWorkflowModelConverter.toWorkflowDefinitionVersion(
				kaleoDefinitionVersion);
		}
		catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	@Override
	public int getWorkflowDefinitionVersionCount(long companyId, String name)
		throws WorkflowException {

		try {
			return _kaleoDefinitionVersionLocalService.
				getKaleoDefinitionVersionsCount(companyId, name);
		}
		catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	@Override
	public List<WorkflowDefinitionVersion> getWorkflowDefinitionVersions(
			long companyId, int start, int end,
			OrderByComparator<WorkflowDefinitionVersion> orderByComparator)
		throws WorkflowException {

		try {
			List<KaleoDefinitionVersion> kaleoDefinitionVersions =
				_kaleoDefinitionVersionLocalService.getKaleoDefinitionVersions(
					companyId, start, end,
					KaleoDefinitionVersionOrderByComparator.
						getOrderByComparator(
							orderByComparator, _kaleoWorkflowModelConverter));

			return toWorkflowDefinitionVersions(kaleoDefinitionVersions);
		}
		catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	@Override
	public List<WorkflowDefinitionVersion> getWorkflowDefinitionVersions(
			long companyId, String name, int start, int end,
			OrderByComparator<WorkflowDefinitionVersion> orderByComparator)
		throws WorkflowException {

		try {
			List<KaleoDefinitionVersion> kaleoDefinitionVersions =
				_kaleoDefinitionVersionLocalService.getKaleoDefinitionVersions(
					companyId, name, start, end,
					KaleoDefinitionVersionOrderByComparator.
						getOrderByComparator(
							orderByComparator, _kaleoWorkflowModelConverter));

			return toWorkflowDefinitionVersions(kaleoDefinitionVersions);
		}
		catch (Exception e) {
			throw new WorkflowException(e);
		}
	}

	protected List<WorkflowDefinitionVersion> toWorkflowDefinitionVersions(
		List<KaleoDefinitionVersion> kaleoDefinitionVersions) {

		List<WorkflowDefinitionVersion> workflowDefinitionVersions =
			new ArrayList<>(kaleoDefinitionVersions.size());

		for (KaleoDefinitionVersion kaleoDefinitionVersion :
				kaleoDefinitionVersions) {

			WorkflowDefinitionVersion workflowDefinitionVersion =
				_kaleoWorkflowModelConverter.toWorkflowDefinitionVersion(
					kaleoDefinitionVersion);

			workflowDefinitionVersions.add(workflowDefinitionVersion);
		}

		return workflowDefinitionVersions;
	}

	@Reference
	private KaleoDefinitionVersionLocalService
		_kaleoDefinitionVersionLocalService;

	@Reference
	private KaleoWorkflowModelConverter _kaleoWorkflowModelConverter;

}