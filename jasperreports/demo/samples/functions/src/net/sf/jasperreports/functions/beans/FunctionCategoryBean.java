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
package net.sf.jasperreports.functions.beans;

import java.io.Serializable;
import java.util.List;

import net.sf.jasperreports.engine.JRConstants;

/**
 * @author sanda zaharia (shertage@users.sourceforge.net)
 * @version $Id$
 */
public class FunctionCategoryBean implements Comparable<FunctionCategoryBean>, Serializable {

	private static final long serialVersionUID = JRConstants.SERIAL_VERSION_UID;
	/**
	 * 
	 */
	private List<FunctionBean> functions;
	private String id;
	private String name;
	private String description;
	
	/**
	 * 
	 */
	public FunctionCategoryBean(
			String id,	
			String name,	
			String description,	
			List<FunctionBean> functions) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.functions = functions;
	}

	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public List<FunctionBean> getFunctions() {
		return functions;
	}

	public void setFunctions(List<FunctionBean> functions) {
		this.functions = functions;
	}
	
	@Override
	public int compareTo(FunctionCategoryBean bean) {
		return this.name.compareTo(bean.name);
	}
}