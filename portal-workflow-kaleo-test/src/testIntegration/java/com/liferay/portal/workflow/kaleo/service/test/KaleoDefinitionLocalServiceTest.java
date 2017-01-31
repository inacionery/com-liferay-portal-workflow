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

package com.liferay.portal.workflow.kaleo.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinition;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionVersionLocalServiceUtil;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Inácio Nery
 */
@RunWith(Arquillian.class)
@Sync
public class KaleoDefinitionLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws PortalException {
		setUpServiceContext();
	}

	@Test
	public void testDeactivateKaleoDefinitionFirstVersion() throws Exception {
		addKaleoDefinition();

		updateKaleoDefinition();

		deactivateKaleoDefinition(1);

		KaleoDefinitionVersion kaleoDefinitionVersion =
			KaleoDefinitionVersionLocalServiceUtil.getKaleoDefinitionVersion(
				_kaleoDefinition.getKaleoDefinitionId(), "1.0");

		Assert.assertEquals(false, kaleoDefinitionVersion.getActive());

		kaleoDefinitionVersion =
			KaleoDefinitionVersionLocalServiceUtil.getKaleoDefinitionVersion(
				_kaleoDefinition.getKaleoDefinitionId(), "2.0");

		Assert.assertEquals(true, kaleoDefinitionVersion.getActive());
	}

	@Test
	public void testDeactivateKaleoDefinitionSecondVersion() throws Exception {
		addKaleoDefinition();

		updateKaleoDefinition();

		deactivateKaleoDefinition(2);

		KaleoDefinitionVersion kaleoDefinitionVersion =
			KaleoDefinitionVersionLocalServiceUtil.getKaleoDefinitionVersion(
				_kaleoDefinition.getKaleoDefinitionId(), "1.0");

		Assert.assertEquals(true, kaleoDefinitionVersion.getActive());

		kaleoDefinitionVersion =
			KaleoDefinitionVersionLocalServiceUtil.getKaleoDefinitionVersion(
				_kaleoDefinition.getKaleoDefinitionId(), "2.0");

		Assert.assertEquals(false, kaleoDefinitionVersion.getActive());
	}

	@Test
	public void testDeleteKaleoDefinition() throws Exception {
		addKaleoDefinition();

		updateKaleoDefinition();

		updateKaleoDefinition();

		deactivateKaleoDefinition(2);

		deleteKaleoDefinition(2);

		updateKaleoDefinition();

		Assert.assertEquals(4, _kaleoDefinition.getVersion());

		KaleoDefinitionVersion kaleoDefinitionVersion =
			KaleoDefinitionVersionLocalServiceUtil.getKaleoDefinitionVersion(
				_kaleoDefinition.getKaleoDefinitionId(), "4.0");

		Assert.assertEquals("4.0", kaleoDefinitionVersion.getVersion());
	}

	@Test
	public void testKaleoDefinitionVersionWithoutChanges() throws Exception {
		addKaleoDefinition();

		KaleoDefinitionVersion kaleoDefinitionVersion =
			KaleoDefinitionVersionLocalServiceUtil.
				getLatestKaleoDefinitionVersion(
					_kaleoDefinition.getKaleoDefinitionId());

		Assert.assertEquals("1.0", kaleoDefinitionVersion.getVersion());
	}

	@Test
	public void testUpdateKaleoDefinition() throws Exception {
		addKaleoDefinition();

		updateKaleoDefinition();

		KaleoDefinitionVersion kaleoDefinitionVersion =
			KaleoDefinitionVersionLocalServiceUtil.
				getLatestKaleoDefinitionVersion(
					_kaleoDefinition.getKaleoDefinitionId());

		Assert.assertEquals("2.0", kaleoDefinitionVersion.getVersion());
	}

	protected void addKaleoDefinition() throws IOException, PortalException {
		_kaleoDefinition = KaleoDefinitionLocalServiceUtil.addKaleoDefinition(
			StringUtil.randomString(), StringUtil.randomString(),
			StringUtil.randomString(), read("single-approver-definition.xml"),
			1, _serviceContext);

		KaleoDefinitionLocalServiceUtil.activateKaleoDefinition(
			_kaleoDefinition.getKaleoDefinitionId(), _serviceContext);
	}

	protected void deactivateKaleoDefinition(int version)
		throws PortalException {

		KaleoDefinitionLocalServiceUtil.deactivateKaleoDefinition(
			_kaleoDefinition.getName(), version, _serviceContext);
	}

	protected void deleteKaleoDefinition(int version) throws PortalException {
		KaleoDefinitionLocalServiceUtil.deleteKaleoDefinition(
			_kaleoDefinition.getName(), version, _serviceContext);
	}

	protected String read(String name) throws IOException {
		ClassLoader classLoader =
			KaleoDefinitionLocalServiceTest.class.getClassLoader();

		try (InputStream inputStream = classLoader.getResourceAsStream(
				"com/liferay/portal/workflow/kaleo/dependencies/" + name)) {

			return StringUtil.read(inputStream);
		}
	}

	protected void setUpServiceContext() throws PortalException {
		_serviceContext = new ServiceContext();

		_serviceContext.setCompanyId(TestPropsValues.getCompanyId());
		_serviceContext.setUserId(TestPropsValues.getUserId());
	}

	protected void updateKaleoDefinition() throws IOException, PortalException {
		_kaleoDefinition =
			KaleoDefinitionLocalServiceUtil.updateKaleoDefinition(
				_kaleoDefinition.getName(), StringUtil.randomString(),
				StringUtil.randomString(),
				read("single-approver-definition.xml"), _serviceContext,
				_kaleoDefinition);

		KaleoDefinitionLocalServiceUtil.activateKaleoDefinition(
			_kaleoDefinition.getKaleoDefinitionId(), _serviceContext);
	}

	@DeleteAfterTestRun
	private KaleoDefinition _kaleoDefinition;

	private ServiceContext _serviceContext;

}