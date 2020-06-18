package com.yp.core.entity;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.yp.core.log.MyLogger;
import com.yp.core.tools.DateTime;

//import javafx.beans.property.SimpleBooleanProperty;
//import javafx.beans.property.SimpleDoubleProperty;
//import javafx.beans.property.SimpleIntegerProperty;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.beans.property.SimpleStringProperty;

public class DataEntity implements IDataEntity {

	public static final byte INSERTED = 0;
	public static final byte DELETED = 1;
	public static final byte UPDATED = 2;
	public static final byte UNCHANGED = 3;
	public static final byte EMPTY = 4;

	private static final long serialVersionUID = 5340694004093419660L;
	protected Map<String, IElement> fields;
	private Map<String, IElement> primaryKeys;
	protected byte state;
	protected String className;
	private boolean selected;

	public DataEntity() {
		super();
		className = "DataEntity";
		fields = new HashMap<>();
		primaryKeys = new HashMap<>();
		state = INSERTED;
		selected = false;
	}

	@Override
	public Map<String, IElement> getFields() {
		return fields;
	}

	@Override
	public Map<String, IElement> getPrimaryKeys() {
		return primaryKeys;
	}

	@Override
	public void setPrimaryKeys(String... pKeyNames) {
		primaryKeys.clear();
		for (String k : pKeyNames) {
			String key = k.toLowerCase(Locale.US);
			primaryKeys.put(key, new Element());
		}
	}

	protected Integer getInteger(String pFieldName) {
		Object o = get(pFieldName);
		if (o != null && !(o instanceof Integer)) {
			setField(pFieldName, Integer.parseInt(o.toString()), getFields().get(pFieldName).isChanged());
		}
		return (Integer) get(pFieldName);
	}

	@Override
	public byte getState() {
		return state;
	}

	@Override
	public void setState(byte pState) {
		state = pState;
	}

	protected transient int rowNum;

	@Override
	public Integer getRowNum() {
		return rowNum;
	}

	@Override
	public void setRowNum(Integer pRowNum) {
		rowNum = pRowNum;
	}

	@Override
	public void set(String pFieldName, Object pValue) {
		setField(pFieldName, pValue, true);
	}

	@Override
	public void set(String pFieldName, String pValue, int pLength) {
		if (pValue != null && pValue.length() > pLength) {
			pValue = pValue.substring(0, pLength - 1);
		}
		setField(pFieldName, pValue, true);
	}

	@Override
	public void setFieldReadonly(String pFieldName, boolean pReadonly) {
		String key = pFieldName.toLowerCase(Locale.US);
		if (fields.containsKey(key)) {
			fields.get(key).setReadonly(pReadonly);
		}
		if (primaryKeys.containsKey(key)) {
			primaryKeys.get(key).setReadonly(pReadonly);
		}
	}

	@Override
	public void setField(String pFieldName, Object pValue, boolean pChanged) {
		String key = pFieldName.toLowerCase(Locale.US);
		IElement e = fields.computeIfAbsent(key, k -> new Element(pValue));

		e.setValue(pValue, pChanged);
		if (primaryKeys.containsKey(key) && (isNew() || !pChanged)) {
			e = primaryKeys.get(key);
			if (e == null) {
				e = new Element(pValue);
			}
			e.setValue(pValue);
			primaryKeys.put(key, e);
		}
		if (pChanged && UNCHANGED == state)
			state = UPDATED;
	}

	@Override
	public Object get(String pFieldName) {
		String key = pFieldName.toLowerCase(Locale.US);
		if (fields.containsKey(key)) {
			return fields.get(key).getValue();
		}
		return null;
	}

	protected String getClob(String pAlanadi) {
		Object r = get(pAlanadi);
		if (r != null) {
			if (r instanceof String)
				return (String) r;
			if (r instanceof Clob) {
				Clob b = (Clob) r;
				try {
					return b.getSubString(1l, (int) b.length());
				} catch (SQLException e) {
					Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
		return "";
	}

	protected byte[] getBlob(String pAlanadi) {
		Object r = get(pAlanadi);
		if (r != null) {
			if (r instanceof byte[])
				return (byte[]) r;
			if (r instanceof Blob) {
				Blob b = (Blob) r;
				try {
					return b.getBytes((long) 1, (int) b.length());
				} catch (SQLException e) {
					Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
		return null;
	}

	@Override
	public void delete() {
		state = DELETED;
	}

	protected static final String ALN_DELETE_PERMITED = "Deletepermited";

	@Override
	public boolean isDeleteDisabled() {
		return "1".equals(get(ALN_DELETE_PERMITED));
	}

	public void disableDeletion() {
		setField(ALN_DELETE_PERMITED, "1", false);
	}

	public void enableDeletion() {
		setField(ALN_DELETE_PERMITED, "0", false);
	}

	@Override
	public void accept() {
		state = UNCHANGED;
		for (IElement v : fields.values()) {
			v.accept();
		}
		for (Map.Entry<String, IElement> entry : primaryKeys.entrySet()) {
			entry.setValue(fields.get(entry.getKey()));
		}

		// fields.forEach((k, v) -> {
		// v.accept();
		// });
		// primaryKeys.forEach((k, v) -> {
		// v = fields.get(k);
		// });
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean pSelected) {
		selected = pSelected;
	}

	public Boolean getNew() {
		return INSERTED == state;
	}

	@Override
	public boolean isNew() {
		return INSERTED == state;
	}

	@Override
	public boolean isUpdated() {
		return UPDATED == state;
	}

	@Override
	public boolean isUnchanged() {
		return UNCHANGED == state;
	}

	@Override
	public boolean isDeleted() {
		return DELETED == state;
	}

	@Override
	public boolean isUpdated(String pFieldName) {
		String key = pFieldName.toLowerCase(Locale.US);
		return fields.containsKey(key) && fields.get(key).isChanged();
	}

	@Override
	public boolean isNull(String pFieldName) {
		String key = pFieldName.toLowerCase(Locale.US);
		return !fields.containsKey(key) || fields.get(key).getValue() == null;
	}

	@Override
	public boolean isPrimaryKey(String pFieldName) {
		String key = pFieldName.toLowerCase(Locale.US);
		return primaryKeys.containsKey(key) || primaryKeys.get(key).getValue() == null;
	}

	@Override
	public void load(String[] pFieldNames, Object[] pValues) {
		int i = 0;
		for (String key : pFieldNames) {
			key = key.toLowerCase(Locale.US);
			setField(key, pValues[i++], false);
		}
	}

	@Override
	public void load(String[] pFieldNames, ResultSet pRs) {
		int i = 1;
		for (String key : pFieldNames) {
			key = key.toLowerCase(Locale.US);
			try {
				setField(key, pRs.getObject(i++), false);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public IDataEntity load(final IDataEntity pDe) {
		fields = pDe.getFields();
		primaryKeys = pDe.getPrimaryKeys();
		state = pDe.getState();
		className = pDe.getClassName();
		return this;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public void checkValues() {
		checkBigDecimal(ALN_Trhzmn);
		checkBigDecimal(ALN_Sontrhzmn);

	}

	public void checkInteger(String pFieldName) {
		Object temp = get(pFieldName);
		if (temp != null && !(temp instanceof Integer))
			setField(pFieldName, (int) Double.parseDouble(temp.toString()), false);
	}

	public void checkBigDecimal(String pFieldName) {
		Object temp = get(pFieldName);
		if (temp != null && !(temp instanceof BigDecimal))
			setField(pFieldName, new BigDecimal(temp.toString()), false);
	}

	public void checkDouble(String pFieldName) {
		Object temp = get(pFieldName);
		if (temp != null && !(temp instanceof Double))
			setField(pFieldName, Double.parseDouble(temp.toString()), false);
	}

	public void checkString(String pFieldName, Format pFormat) {
		Object temp = get(pFieldName);
		if (temp != null && !(temp instanceof String))
			setField(pFieldName, pFormat.format(temp), false);
	}

	@Override
	public String getSchemaName() {
		return "";
	}

	@Override
	public String getTableName() {
		return "";
	}

	@Override
	public void setUserInfo(IDataEntity pDataEntity) {
		setKln((DataEntity) pDataEntity);
	}

	@Override
	public void setLastUserInfo(IDataEntity pDataEntity) {
		setSonkln((DataEntity) pDataEntity);
	}

	@Override
	public void setUserInfo(String pUser, String pClientIP, Date pDate) {
		setKln(pUser, pClientIP, pDate);
	}

	@Override
	public void setLastUserInfo(String pUser, String pClientIP, Date pDate) {
		setSonkln(pUser, pClientIP, pDate);

	}

	private static final String ALN_Kln = "Kln";

	public String getUser() {
		return (String) get(ALN_Kln);
	}

	public void setKln(String pKln) {
		set(ALN_Kln, pKln);
		setTrhzmn(new Date());
	}

	public void setKln(String pKln, Date pTrhZmn) {
		set(ALN_Kln, pKln);
		setTrhzmn(pTrhZmn);
	}

	public void setKln(String pKln, String pUzkAdr, Date pTrhZmn) {
		set(ALN_Uzkadr, pUzkAdr);
		setKln(pKln, pTrhZmn);
	}

	public void setKln(String pKln, String pUzkAdr) {
		set(ALN_Uzkadr, pUzkAdr);
		setKln(pKln);
	}

	public void setKln(DataEntity pKln) {
		setKln(pKln.getUser(), pKln.getUzkadr(), pKln.getTrhzmn());
	}

	public Boolean isKlnNull() {
		return isNull(ALN_Kln);
	}

	private static final String ALN_Uzkadr = "Uzkadr";

	public String getUzkadr() {
		return (String) get(ALN_Uzkadr);
	}

	public void setUzkadr(String pUzkadr) {
		set(ALN_Uzkadr, pUzkadr);
	}

	public Boolean isUzkadrNull() {
		return isNull(ALN_Uzkadr);
	}

	private static final String ALN_Trhzmn = "Trhzmn";

	public Date getTrhzmn() {
		return DateTime.asDate((BigDecimal) get(ALN_Trhzmn));
	}

	public void setTrhzmn(Date pTrhzmn) {
		set(ALN_Trhzmn, DateTime.asDbDate(pTrhzmn));
	}

	public Boolean isTrhzmnNull() {
		return isNull(ALN_Trhzmn);
	}

	private static final String ALN_Sonkln = "Sonkln";

	public String getSonkln() {
		return (String) get(ALN_Sonkln);
	}

	// public void setSonkln(String pSonkln) {
	// set(ALN_Sonkln, pSonkln);
	// setSontrhzmn(new Date());
	// if (isYeni()) {
	// setKln(pSonkln, getSontrhzmn());
	// }
	// }

	public void setSonkln(String pSonkln, Date pTrhZmn) {
		setSonkln(pSonkln, "yerel", pTrhZmn);
		if (isNew()) {
			setKln(pSonkln, getUzkadr(), pTrhZmn);
		}
	}

	public void setSonkln(String pSonkln, String pUzkAdr, Date pTrhZmn) {
		set(ALN_Sonkln, pSonkln);
		set(ALN_Sonuzkadr, pUzkAdr);
		setSontrhzmn(pTrhZmn);
		if (isNew()) {
			setKln(pSonkln, pUzkAdr, pTrhZmn);
		}
	}

	public void setSonkln(String pSonkln, String pUzkAdr) {
		setSonkln(pSonkln, pUzkAdr, new Date());
		if (isNew()) {
			setKln(pSonkln, pUzkAdr, getSontrhzmn());
		}
	}

	public void setSonkln(DataEntity pKln) {
		setSonkln(pKln.getSonkln(), pKln.getSonuzkadr(), pKln.getSontrhzmn());
	}

	public Boolean isSonklnNull() {
		return isNull(ALN_Sonkln);
	}

	private static final String ALN_Sonuzkadr = "Sonuzkadr";

	public String getSonuzkadr() {
		return (String) get(ALN_Sonuzkadr);
	}

	public void setSonuzkadr(String pSonuzkadr) {
		set(ALN_Sonuzkadr, pSonuzkadr);
	}

	public Boolean isSonuzkadrNull() {
		return isNull(ALN_Sonuzkadr);
	}

	private static final String ALN_Sontrhzmn = "Sontrhzmn";

	public Date getSontrhzmn() {
		return DateTime.asDate((BigDecimal) get(ALN_Sontrhzmn));
	}

	public void setSontrhzmn(Date pSontrhzmn) {
		set(ALN_Sontrhzmn, DateTime.asDbDate(pSontrhzmn));
	}

	public Boolean isSontrhzmnNull() {
		return isNull(ALN_Sontrhzmn);
	}

	public Object isNull(Object pValue, Object pNullValue) {
		if (pValue == null)
			return pNullValue;
		return pValue;
	}

//	public class BooleanProperty extends SimpleBooleanProperty {
//
//		private String dAlanAdi;
//		private Object dTrueDeger = Boolean.TRUE;
//		private Object dFalseDeger = Boolean.FALSE;
//
//		public BooleanProperty(String pAlanAdi, Boolean pBoolean) {
//			super(pBoolean);
//			dAlanAdi = pAlanAdi;
//		}
//
//		public BooleanProperty(String pAlanAdi, Boolean pBoolean, final Object pTrueDeger, final Object pFalseDeger) {
//			super(pBoolean);
//			dAlanAdi = pAlanAdi;
//			dTrueDeger = pTrueDeger;
//			dFalseDeger = pFalseDeger;
//		}
//
//		@Override
//		public void set(boolean pBoolean) {
//			super.set(pBoolean);
//			Object dDeger = pBoolean ? dTrueDeger : dFalseDeger;
//			// if (!dDeger.equals(get(dAlanAdi)))
//			DataEntity.this.set(dAlanAdi, dDeger);
//		}
//
//		public Object getTrueDeger() {
//			return dTrueDeger;
//		}
//
//		public Object getFalseDeger() {
//			return dFalseDeger;
//		}
//
//	}
//
//	public class StringProperty extends SimpleStringProperty {
//
//		private String dAlanAdi;
//		private boolean dDegisti;
//		private CheckString checkString;
//
//		public StringProperty(String pAlanAdi, String pDeger) {
//			super(pDeger);
//			dAlanAdi = pAlanAdi;
//			dDegisti = true;
//		}
//
//		public StringProperty(String pAlanAdi, String pDeger, CheckString pCheckString) {
//			this(pAlanAdi, pDeger);
//			checkString = pCheckString;
//		}
//
//		public StringProperty(String pAlanAdi, String pDeger, boolean pDegisti) {
//			this(pAlanAdi, pDeger);
//			dDegisti = pDegisti;
//		}
//
//		@Override
//		public void set(String pValue) {
//			String value = checkString != null ? checkString.getChecked(pValue) : pValue;
//			super.set(value);
//			setField(dAlanAdi, value, dDegisti);
//		}
//
//	}
//
//	public interface CheckString {
//		String getChecked(String pValue);
//	}
//
//	public class DoubleProperty extends SimpleDoubleProperty {
//
//		private String dAlanAdi;
//		private Double dBosDeger;
//		private boolean dChanged;
//
//		public DoubleProperty(String pAlanAdi, Double pSayi, Double pBosDeger, boolean pChanged) {
//			super();
//			dAlanAdi = pAlanAdi;
//			dBosDeger = pBosDeger;
//			dChanged = pChanged;
//			setValue(pSayi);
//		}
//
//		public DoubleProperty(String pAlanAdi, Double pSayi, Double pBosDeger) {
//			this(pAlanAdi, pSayi, pBosDeger, true);
//		}
//
//		@Override
//		public void setValue(Number pSayi) {
//			if (pSayi == null)
//				pSayi = dBosDeger;
//			set(pSayi.doubleValue());
//		}
//
//		@Override
//		public void set(double pSayi) {
//			super.set(pSayi);
//			setField(dAlanAdi, pSayi, dChanged);
//		}
//	}
//
//	public class BigDecimalProperty extends SimpleDoubleProperty {
//
//		private String dAlanAdi;
//		private BigDecimal dBosDeger;
//
//		public BigDecimalProperty(String pAlanAdi, BigDecimal pSayi, BigDecimal pBosDeger) {
//			super();
//			dAlanAdi = pAlanAdi;
//			dBosDeger = pBosDeger;
//			setValue(pSayi);
//		}
//
//		@Override
//		public void setValue(Number pSayi) {
//			if (pSayi == null)
//				pSayi = dBosDeger;
//			set(pSayi.doubleValue());
//		}
//
//		@Override
//		public void set(double pSayi) {
//			super.set(pSayi);
//			setField(dAlanAdi, new BigDecimal(pSayi), true);
//		}
//	}
//
//	public class IntegerProperty extends SimpleIntegerProperty {
//
//		private String dAlanAdi;
//		private boolean dDegisti;
//		private Integer dBosDeger;
//
//		public IntegerProperty(String pAlanAdi, Integer pSayi, Integer pBosDeger, boolean pDegisti) {
//			super();
//			dAlanAdi = pAlanAdi;
//			dDegisti = pDegisti;
//			dBosDeger = pBosDeger;
//			setValue(pSayi);
//		}
//
//		public IntegerProperty(String pAlanAdi, Integer pSayi, Integer pBosDeger) {
//			this(pAlanAdi, pSayi, pBosDeger, true);
//		}
//
//		@Override
//		public void setValue(Number pSayi) {
//			if (pSayi == null)
//				pSayi = dBosDeger;
//			set(pSayi.intValue());
//		}
//
//		@Override
//		public void set(int pSayi) {
//			super.set(pSayi);
//			setField(dAlanAdi, pSayi, dDegisti);
//		}
//
//	}
//
//	public class TarihProperty extends SimpleObjectProperty<LocalDate> {
//
//		private String dAlanAdi;
//
//		public TarihProperty(String pAlanAdi, LocalDate pTarih) {
//			super();
//			dAlanAdi = pAlanAdi;
//			set(pTarih);
//		}
//
//		@Override
//		public void setValue(LocalDate pTarih) {
//			if (pTarih != null) {
//				super.set(pTarih);
//				setField(dAlanAdi, DateTime.asDbDate(pTarih), true);
//			} else {
//				super.set(null);
//				setField(dAlanAdi, BigDecimal.ZERO, true);
//			}
//		}
//	}	
//
//	public class KodProperty<T> extends SimpleObjectProperty<IReference<T>> {
//		private String dAlanAdi;
//		private IReference<T> dBosDeger;
//
//		public KodProperty(String pAlanAdi, IReference<T> pKod, IReference<T> pBosDeger) {
//			super();
//			dAlanAdi = pAlanAdi;
//			dBosDeger = pBosDeger;
//			setValue(pKod);
//		}
//
//		@Override
//		public void set(IReference<T> pKod) {
//			super.set(pKod);
//			setField(dAlanAdi, pKod.getKey(), true);
//		}
//
//		@Override
//		public void setValue(IReference<T> pKod) {
//			if (pKod == null)
//				pKod = dBosDeger;
//			set(pKod);
//		}
//	}

}
