package com.yp.core.user;

import com.yp.core.entity.IDataEntity;

public interface IUser extends IDataEntity {
	String getName();
	String getSurname();
	String getEmail();
	String getMobilePhoneNum();
	String getFullName();
	Integer getUserId();
	void setUserId(Integer userId);

}
