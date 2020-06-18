package com.yp.core.db;

import com.yp.core.FnParam;

public class DbCommand {
	private String name;
	private String query;
	private FnParam[] params;

	public DbCommand(final String pName, final FnParam... pParams) {
		this.name = pName;
		this.params = pParams;
	}

	public DbCommand(final String pQuery) {
		query = pQuery;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String pName) {
		this.name = pName;
	}

	public FnParam[] getParams() {
		return this.params;
	}

	public void setParams(final FnParam... pParams) {
		this.params = pParams;
	}

	public String getQuery() {
		return this.query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}
}