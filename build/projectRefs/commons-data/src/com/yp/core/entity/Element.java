package com.yp.core.entity;

public class Element implements IElement {

	private static final long serialVersionUID = 4398406209912604390L;
	private Object value;
	private boolean changed;
	private String typeName;
	private boolean readonly;

	public Element() {
		super();
		value = null;
		changed = false;
		typeName = "";
		readonly = false;
	}

	public Element(Object pValue) {
		this();
		value = pValue;
		changed = false;
		typeName = "";
		readonly = false;
	}

	public Element(String pValue, Boolean pChanged) {
		this(pValue);
		value = pValue;
		changed = pChanged;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object pValue) {
		value = pValue;
		changed = true;
	}

	@Override
	public void setValue(Object pValue, boolean pChanged) {
		value = pValue;
		changed = pChanged;
	}

	@Override
	public boolean isChanged() {
		return changed;
	}

	@Override
	public void setChanged(boolean pChanged) {
		changed = pChanged;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public void setTypeName(String pTypeName) {
		typeName = pTypeName;
	}

	@Override
	public boolean isReadonly() {
		return readonly;
	}

	@Override
	public void setReadonly(boolean pReadonly) {
		readonly = pReadonly;
	}

	@Override
	public void accept() {
		changed = false;
	}

}
