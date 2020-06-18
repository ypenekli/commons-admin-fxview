package com.yp.core.db;

import java.math.BigDecimal;

public interface ITransfer {
	String getTransferId();
	
	String getSourceSchema();
	String getSourceTable();
	Integer getSourceCount();
	void setSourceCount(Integer pCount);
	
	String getTargetSchema();
	String getTargetTable();
	Integer getTargetCount();
	void setTargetCount(Integer pCount);
	
	void setStartTime(BigDecimal pTime);
	void setEndTime(BigDecimal pTime);
	
	String getQuery();	
	String getMessages();
	void setMessages(String pMessage);
	
	boolean isDeleteTargetTableRows();

}
