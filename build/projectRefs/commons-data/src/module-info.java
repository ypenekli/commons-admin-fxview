module commons.data {
	exports com.yp.core.html;
	exports com.yp.core;
	exports com.yp.core.user;
	exports com.yp.core.db;
	exports com.yp.core.ref;
	exports com.yp.core.log;
	exports com.yp.core.web;
	exports com.yp.core.entity;
	exports com.yp.core.sec;
	exports com.yp.core.excel;
	exports com.yp.core.mail;
	exports com.yp.core.tools;

	requires gson;
	requires java.desktop;
	requires java.logging;
	requires java.mail;
	requires java.naming;
	requires java.sql;
	requires org.apache.commons.io;
	requires poi;
}