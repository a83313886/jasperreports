/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2013 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.data.xls;

import java.io.IOException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.data.AbstractXlsDataSource;
import net.sf.jasperreports.engine.data.JRXlsDataSource;

/**
 * @deprecated To be removed.
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: XlsDataAdapterService.java 6972 2014-03-12 11:41:51Z shertage $
 */
public class JxlDataAdapterService extends AbstractXlsDataAdapterService 
{
	public static final String PROPERTY_DATA_ADAPTER_USE_LEGACY_JEXCELAPI = JRPropertiesUtil.PROPERTY_PREFIX + ".data.adapter.xls.use.legacy.jexcelapi";
	
	/**
	 * 
	 */
	public JxlDataAdapterService(JasperReportsContext jasperReportsContext, XlsDataAdapter xlsDataAdapter)
	{
		super(jasperReportsContext, xlsDataAdapter);
	}
	
	@Override
	protected AbstractXlsDataSource getXlsDataSource() throws JRException
	{
		XlsDataAdapter xlsDataAdapter = getXlsDataAdapter();
		
		AbstractXlsDataSource dataSource = null; 
		try
		{
			dataSource = new JRXlsDataSource(getJasperReportsContext(), xlsDataAdapter.getFileName());
		}
		catch (IOException e)
		{
			throw new JRException(e);
		}
		return dataSource;
	}
	
}