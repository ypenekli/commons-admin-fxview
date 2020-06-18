package com.yp.core.ref;

import com.yp.core.entity.IDataEntity;

public interface IReference<T> extends IDataEntity{

	T getKey();

	void setKey(T pKey);

	String getDefinition();

	void setDefinition(String pDefinition);

	String getExtra();

	void setExtra(String pExtra);

}
