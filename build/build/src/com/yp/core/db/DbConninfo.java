package com.yp.core.db;

public class DbConninfo {
	
	private String key;
	private String name;
	private String dbDriver;
	private String dbPassword;
	private String dbSeperator;
	private String dbUrl;
	private String dbUser;
	
	public DbConninfo(String pKey, String pLabel) {
		super();
		key = pKey;
		name = pLabel;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String pKey) {
		key = pKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String pName) {
		name = pName;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String pDbDriver) {
		dbDriver = pDbDriver;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String pDbPassword) {
		dbPassword = pDbPassword;
	}

	public String getDbSeperator() {
		return dbSeperator;
	}

	public void setDbSeperator(String pDbSeperator) {
		dbSeperator = pDbSeperator;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String pDbUrl) {
		dbUrl = pDbUrl;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String pDbUser) {
		dbUser = pDbUser;
	}
	

}
