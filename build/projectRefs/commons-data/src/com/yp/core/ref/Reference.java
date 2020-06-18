package com.yp.core.ref;

import com.yp.core.entity.DataEntity;

public class Reference<T> extends DataEntity implements IReference<T> {

	public static final transient String KEY = "key";
	public static final transient String DEFINITION = "definition";
	private static final String extraFieldName = "extra";

	private static final long serialVersionUID = 7652554245250573812L;
	private String keyFieldName;
	private String definitionFieldName;
	private String toStringFieldName;

	public Reference(String pKeyFieldName, T pKeyFieldValue, String pDefinitionFieldName,
			String pDefinitionFieldValue) {
		super();
		keyFieldName = pKeyFieldName;
		definitionFieldName = pDefinitionFieldName;
		toStringFieldName = definitionFieldName;
		setKey(pKeyFieldValue);
		setDefinition(pDefinitionFieldValue);
	}

	public Reference(T pKeyFieldValue, String pDefinitionFieldValue) {
		this(KEY, pKeyFieldValue, DEFINITION, pDefinitionFieldValue);
	}

	public Reference(T pKeyFieldValue) {
		this(KEY, pKeyFieldValue, DEFINITION, pKeyFieldValue.toString());
	}

	public Reference(T pKeyFieldValue, String pDefinitionFieldValue, String pExtra) {
		this(pKeyFieldValue, pDefinitionFieldValue);
		set(extraFieldName, pExtra);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getKey() {
		return (T) get(keyFieldName);
	}

	@Override
	public void setKey(T pKey) {
		set(keyFieldName, pKey);
	}

	@Override
	public String getDefinition() {
		return (String) get(definitionFieldName);
	}

	@Override
	public void setDefinition(String pAck) {
		set(definitionFieldName, pAck);
	}

	@Override
	public String getExtra() {
		return (String) get(extraFieldName);
	}

	@Override
	public void setExtra(String pExtra) {
		set(extraFieldName, pExtra);
	}

	public void setToStringField(String pToStringField) {
		toStringFieldName = pToStringField;
	}

	@Override
	public String toString() {
		if (!isNull(toStringFieldName))
			return (String) get(toStringFieldName);
		else
			return super.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object pObj) {
		if (pObj instanceof Reference) {
			if (!isNull(keyFieldName))
				return getKey().equals(((Reference<T>) pObj).getKey());
		} else if (pObj != null) {
			return get(keyFieldName).equals(pObj);
		}
		return false;
	}

}
