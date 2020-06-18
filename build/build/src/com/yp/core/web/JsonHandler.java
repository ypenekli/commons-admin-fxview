package com.yp.core.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import com.yp.core.BaseConstants;
import com.yp.core.FnParam;
import com.yp.core.IHandler;
import com.yp.core.db.DbCommand;
import com.yp.core.db.DbConninfo;
import com.yp.core.db.ITransfer;
import com.yp.core.db.OnExportListener;
import com.yp.core.entity.DataEntity;
import com.yp.core.entity.IDataEntity;
import com.yp.core.entity.IResult;
import com.yp.core.entity.Result;
import com.yp.core.excel.AXlsAktar;
import com.yp.core.log.MyLogger;

public class JsonHandler<T> implements IHandler<T> {
	private String url, callerClassName;
	private Class<T> callerClass;
	protected static final String CLASS_NAME = "className";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonHandler(String pUrl, Class pCallerClass) {
		super();
		url = pUrl;
		callerClass = pCallerClass;
		callerClassName = callerClass.getSimpleName();
	}

	private HttpURLConnection getConnection(String pFnName) throws IOException {
		String query = url + "/" + callerClassName + "/" + pFnName + "/";
		URL newUrl = new URL(query);

		HttpURLConnection connection;
		connection = (HttpURLConnection) newUrl.openConnection();
		connection.setConnectTimeout(5000);
		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		connection.setRequestProperty("enctype", "multipart/form-data");
		connection.setRequestProperty("Accept-Encoding", "gzip");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");

		return connection;
	}

	private String getJsonString(InputStream is, String contentEncoding) throws IOException {
		String json;
		if ("gzip".equals(contentEncoding)) {
			json = org.apache.commons.io.IOUtils.toString(new GZIPInputStream(is), StandardCharsets.UTF_8);
		} else
			json = org.apache.commons.io.IOUtils.toString(is, StandardCharsets.UTF_8);
//		System.out.println("json :" + json);
		return json;
	}

	public List<T> downloadAnyFromServer(String pFnName, FnParam[] pParams) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();

		FnParam[] params;
		if (pParams != null && pParams.length > 0) {
			params = new FnParam[pParams.length + 1];
			for (int i = 0; i < pParams.length; i++) {
				params[i + 1] = pParams[i];
			}
		} else {
			params = new FnParam[1];
		}

		Class<T> entityType = getTypeParameterClass();
		params[0] = new FnParam(CLASS_NAME, getClassName(entityType));

		String in = gson.toJson(params);
		HttpURLConnection conn = getConnection(pFnName);
		// System.out.println("URL :" + conn.getURL().toString() + "/" + in);
		OutputStream os = conn.getOutputStream();
		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.flush();
		os.close();

		InputStream is = conn.getInputStream();
		// String res = org.apache.commons.io.IOUtils.toString(is,
		// StandardCharsets.UTF_8);

		String res = getJsonString(is, conn.getContentEncoding());

		Type type = TypeToken.getParameterized(ArrayList.class, entityType).getType();

		ArrayList<T> result = gson.fromJson(res, type);

		is.close();
		conn.disconnect();

		if (result != null) {
			int i = 1;
			for (T e : result) {
				IDataEntity vs = (IDataEntity) e;
				vs.checkValues();
				vs.setRowNum(i);
				i += 1;
			}
		}

		return result;
	}

	public List<IDataEntity> downloadAnyFromServer(String pFnName, Type pOutType, FnParam... pParams)
			throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();

		FnParam[] params;
		if (pParams != null && pParams.length > 0) {
			params = new FnParam[pParams.length + 1];
			for (int i = 0; i < pParams.length; i++) {
				params[i + 1] = pParams[i];
			}
		} else
			params = new FnParam[1];

		params[0] = new FnParam(CLASS_NAME, getClassName(pOutType));

		String in = gson.toJson(params);
		HttpURLConnection conn = getConnection(pFnName);
		// System.out.println("URL :" + conn.getURL().toString() + "/" + in);
		OutputStream os = conn.getOutputStream();
		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.flush();
		os.close();

		InputStream is = conn.getInputStream();
		// String res = org.apache.commons.io.IOUtils.toString(is,
		// StandardCharsets.UTF_8);

		String res = getJsonString(is, conn.getContentEncoding());

		Type type = TypeToken.getParameterized(ArrayList.class, pOutType).getType();

		ArrayList<IDataEntity> result = gson.fromJson(res, type);

		is.close();
		conn.disconnect();

		if (result != null) {
			int i = 1;
			for (IDataEntity vs : result) {
				vs.checkValues();
				vs.setRowNum(i);
				i += 1;
			}
		}
		return result;
	}

	public T downloadFromServer(String pFnName, FnParam... pParams) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();

		FnParam[] params;
		if (pParams != null && pParams.length > 0) {
			params = new FnParam[pParams.length + 1];
			for (int i = 0; i < pParams.length; i++) {
				params[i + 1] = pParams[i];
			}
		} else
			params = new FnParam[1];

		Class<T> entityType = getTypeParameterClass();
		params[0] = new FnParam(CLASS_NAME, getClassName(entityType));

		String in = gson.toJson(params);
		HttpURLConnection conn = getConnection(pFnName);
		// System.out.println("URL :" + conn.getURL().toString() + "/" + in);
		OutputStream os = conn.getOutputStream();
		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.flush();
		os.close();

		InputStream is = conn.getInputStream();

		// T result = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8),
		// entityType);

		String res = getJsonString(is, conn.getContentEncoding());
		T result = gson.fromJson(res, entityType);

		is.close();
		conn.disconnect();

		if (result != null)
			((IDataEntity) result).checkValues();

		return result;
	}

	@SuppressWarnings("unchecked")
	public Class<T> getTypeParameterClass() {
		Type type = callerClass.getGenericSuperclass();
		ParameterizedType paramType = (ParameterizedType) type;
		return (Class<T>) paramType.getActualTypeArguments()[0];
	}

	public T downloadFromServer(String pFnName, T pIn) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();
		String in = gson.toJson(pIn);

		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();

		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.close();

		Class<T> entityType = getTypeParameterClass();

		InputStream is = conn.getInputStream();
		// T result = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8),
		// entityType);

		String res = getJsonString(is, conn.getContentEncoding());
		T result = gson.fromJson(res, entityType);

		is.close();
		conn.disconnect();

		if (result != null)
			((IDataEntity) result).checkValues();
		return result;
	}

	public IDataEntity downloadFromServer(String pFnName, Type pOutType, FnParam... pParams) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();

		FnParam[] params;
		if (pParams != null && pParams.length > 0) {
			params = new FnParam[pParams.length + 1];
			for (int i = 0; i < pParams.length; i++) {
				params[i + 1] = pParams[i];
			}
		} else
			params = new FnParam[1];

		params[0] = new FnParam(CLASS_NAME, getClassName(pOutType));

		String in = gson.toJson(params);

		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();
		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.flush();
		os.close();

		InputStream is = conn.getInputStream();

		// IDataEntity result = gson.fromJson(new InputStreamReader(is,
		// StandardCharsets.UTF_8), pOutType);

		String res = getJsonString(is, conn.getContentEncoding());
		IDataEntity result = gson.fromJson(res, pOutType);

		is.close();
		conn.disconnect();

		if (result != null)
			result.checkValues();

		return result;
	}

	public IResult<List<T>> executeAtServer(String pFnName, List<T> pListIn) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();
		String in = gson.toJson(pListIn);

		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();

		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.close();

		Type type = DataEntity.class;
		if (!pListIn.isEmpty()) {
			type = pListIn.get(0).getClass();
		}

		Type entityType = TypeToken
				.getParameterized(Result.class, TypeToken.getParameterized(ArrayList.class, type).getType()).getType();

		InputStream is = conn.getInputStream();
		// String out = org.apache.commons.io.IOUtils.toString(is,
		// StandardCharsets.UTF_8);

		String out = getJsonString(is, conn.getContentEncoding());
		Result<List<T>> result = gson.fromJson(out, entityType);
		is.close();
		conn.disconnect();
		if (result != null && result.getData() != null)
			for (T e : result.getData()) {
				((IDataEntity) e).checkValues();
			}

		return result;
	}

	public IResult<String> executeAtServer(String pFnName, FnParam[] pIn) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();
		String in = gson.toJson(pIn);

		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();

		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.close();

		Type entityType = new TypeToken<Result<String>>() {
		}.getType();

		InputStream is = conn.getInputStream();
		// String out = org.apache.commons.io.IOUtils.toString(is,
		// StandardCharsets.UTF_8);
		String out = getJsonString(is, conn.getContentEncoding());

		IResult<String> result = gson.fromJson(out, entityType);

		is.close();
		conn.disconnect();

		return result;
	}

	public IResult<T> executeAtServer(String pFnName, FnParam[] pIn, Class<T> pOut) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();
		String in = gson.toJson(pIn);

		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();

		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.close();

		Type entityType = TypeToken.getParameterized(Result.class, pOut).getType();

		InputStream is = conn.getInputStream();
		// String out = org.apache.commons.io.IOUtils.toString(is,
		// StandardCharsets.UTF_8);

		String out = getJsonString(is, conn.getContentEncoding());
		IResult<T> result = gson.fromJson(out, entityType);

		is.close();
		conn.disconnect();

		if (result != null && result.getData() != null)
			((IDataEntity) result.getData()).checkValues();
		return result;
	}

	public IResult<T> executeAtServer(String pFnName, T pIn) throws IOException {
		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();
		String in = gson.toJson(pIn);

		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();

		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.close();

		Type entityType = TypeToken.getParameterized(Result.class, pIn.getClass()).getType();

		InputStream is = conn.getInputStream();
		// String out = org.apache.commons.io.IOUtils.toString(is,
		// StandardCharsets.UTF_8);
		String out = getJsonString(is, conn.getContentEncoding());
		IResult<T> result = gson.fromJson(out, entityType);
		is.close();
		conn.disconnect();

		if (result != null && result.getData() != null)
			((IDataEntity) result.getData()).checkValues();

		return result;
	}

	public IResult<IDataEntity> executeAtServer(String pFnName, IDataEntity pIn) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();
		String in = gson.toJson(pIn);

		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();

		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.close();

		Type entityType = TypeToken.getParameterized(Result.class, pIn.getClass()).getType();

		InputStream is = conn.getInputStream();
		// String out = org.apache.commons.io.IOUtils.toString(is,
		// StandardCharsets.UTF_8);

		String out = getJsonString(is, conn.getContentEncoding());
		IResult<IDataEntity> result = gson.fromJson(out, entityType);
		is.close();
		conn.disconnect();

		if (result != null && result.getData() != null)
			result.getData().checkValues();
		return result;
	}

	public List<IDataEntity> uploadToServer(String pFnName, IDataEntity pIn, Type pOut) throws IOException {

		GsonBuilder gb = new GsonBuilder();
		// gb.registerTypeAdapter(IElement.class, new ElementSerializer());
		gb.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

		Gson gson = gb.create();
		String in = gson.toJson(pIn);

		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();

		os.write(in.getBytes(StandardCharsets.UTF_8));
		os.close();

		InputStream is = conn.getInputStream();

		Type entityType = TypeToken.getParameterized(ArrayList.class, pOut).getType();

		ArrayList<IDataEntity> result = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), entityType);

		is.close();
		conn.disconnect();

		return result;
	}

	public String uploadToServer(String pFnName, String pData) throws IOException {
		HttpURLConnection conn = getConnection(pFnName);
		OutputStream os = conn.getOutputStream();

		os.write(pData.getBytes(StandardCharsets.UTF_8));
		os.close();
		InputStream in = conn.getInputStream();

		String result = org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);
		in.close();
		conn.disconnect();

		return result;
	}

	protected String getClassName(Type pOutType) {
		String className = pOutType.toString();
		return className.substring(className.lastIndexOf('.') + 1);
	}

	@Override
	public T find(T pDataEntity) {
		T result = null;
		try {
			result = downloadFromServer("find", pDataEntity);
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}

		return result;
	}

	@Override
	public IDataEntity findOne(DbCommand pQuery, Type pOutType) {
		IDataEntity result = null;
		String dFnName = "findOne@" + pQuery.getName();
		try {
			result = downloadFromServer(dFnName, pOutType, pQuery.getParams());
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}

		return result;
	}

	@Override
	public T findOne(DbCommand pQuery) {
		String dFnName = "findOne@" + pQuery.getName();
		try {
			return downloadFromServer(dFnName, pQuery.getParams());
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public List<IDataEntity> findAny(DbCommand pQuery, Type pOutType) {
		String dFnName = "findBy@" + pQuery.getName();
		List<IDataEntity> list = null;
		try {
			list = downloadAnyFromServer(dFnName, pOutType, pQuery.getParams());
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return list;
	}

	@Override
	public List<T> findAny(DbCommand pQuery) {
		String dFnName = "findBy@" + pQuery.getName();
		List<T> list = null;
		try {
			list = downloadAnyFromServer(dFnName, pQuery.getParams());
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return list;
	}

	@Override
	public IResult<T> save(T pData) {
		try {
			return executeAtServer("save", pData);
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public IResult<IDataEntity> save(IDataEntity pData) {
		try {
			return executeAtServer("save", pData);
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public IResult<List<T>> saveAll(List<T> pData) {

		try {
			return executeAtServer("saveAll", pData);
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public IResult<String> execute(DbCommand pQuery) {
		String dFnName = "execute@" + pQuery.getName();
		try {
			return executeAtServer(dFnName, pQuery.getParams());
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public IResult<String> executeAll(DbCommand... pQueries) {
		ArrayList<FnParam> fnparams = new ArrayList<>();
		if (pQueries != null && pQueries.length > 0)
			for (DbCommand x : pQueries) {
				fnparams.add(new FnParam(x.getName(), x.getParams()));
			}
		try {
			return executeAtServer("executeAll", fnparams.toArray(new FnParam[] {}));
		} catch (IOException e) {
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IResult<String> saveAtomic(Object... pParams) {
		if (pParams != null && pParams.length > 0) {
			ArrayList<FnParam> fnparams = new ArrayList<>();
			for (Object o : pParams) {
				if (o != null) {
					if (ArrayList.class.isAssignableFrom(o.getClass())) {
						ArrayList<IDataEntity> list = (ArrayList) o;
						if (!list.isEmpty())
							fnparams.add(new FnParam("list", list));
					} else if (IDataEntity[].class.isAssignableFrom(o.getClass())) {
						IDataEntity[] list = (IDataEntity[]) o;
						if (list.length > 0)
							for (IDataEntity de : list)
								fnparams.add(new FnParam("data", de));
					} else if (IDataEntity.class.isAssignableFrom(o.getClass())) {
						IDataEntity de = (IDataEntity) o;
						fnparams.add(new FnParam("data", de));
					} else if (DbCommand[].class.isAssignableFrom(o.getClass())) {
						DbCommand[] list = (DbCommand[]) o;
						if (list.length > 0)
							for (DbCommand cmd : list)
								fnparams.add(new FnParam(cmd.getName(), cmd.getParams()));
					} else if (DbCommand.class.isAssignableFrom(o.getClass())) {
						DbCommand cmd = (DbCommand) o;
						fnparams.add(new FnParam(cmd.getName(), cmd.getParams()));
					}
				}

			}

			try {
				return executeAtServer("saveAtomic", fnparams.toArray(new FnParam[] {}));
			} catch (IOException e) {
				Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
			}
		}

		return null;
	}

	@Override
	public IResult<AXlsAktar> transferToXls(DbCommand pQuery, Type pOutType, AXlsAktar pXls) {
		IResult<AXlsAktar> res = new Result<>();
		List<IDataEntity> list = findAny(pQuery, pOutType);
		if (!BaseConstants.isEmpty(list)) {
			res.setSuccess(true);
			pXls.yaz(list);
			res.setData(pXls);
		}
		return res;
	}

	@Override
	public IResult<String> sendMail(FnParam... pParams) {
		IResult<String> res = new Result<>();
		try {
			return executeAtServer("sendMail", pParams);
		} catch (IOException e) {
			res.setSuccess(false);
			res.setMessage(e.getMessage());
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, e.getMessage(), e);
		}
		return res;
	}

	public static boolean isJsonValid(final String json) throws IOException {
		return isJsonValid(new StringReader(json));
	}

	private static boolean isJsonValid(final Reader reader) throws IOException {
		return isJsonValid(new JsonReader(reader));
	}

	private static boolean isJsonValid(final JsonReader jsonReader) throws IOException {
		try {
			JsonToken token;
			loop: while ((token = jsonReader.peek()) != JsonToken.END_DOCUMENT && token != null) {
				switch (token) {
				case BEGIN_ARRAY:
					jsonReader.beginArray();
					break;
				case END_ARRAY:
					jsonReader.endArray();
					break;
				case BEGIN_OBJECT:
					jsonReader.beginObject();
					break;
				case END_OBJECT:
					jsonReader.endObject();
					break;
				case NAME:
					jsonReader.nextName();
					break;
				case STRING:
				case NUMBER:
				case BOOLEAN:
				case NULL:
					jsonReader.skipValue();
					break;
				case END_DOCUMENT:
					break loop;
				default:
					throw new AssertionError(token);
				}
			}
			return true;
		} catch (final MalformedJsonException ignored) {
			return false;
		}
	}

	public List<IDataEntity> findDbTables(String pLibrary, String pSchema) {
		return null;
	}

	@Override
	public IResult<ITransfer> transferDb(DbConninfo pTarget, ITransfer pTransfer, OnExportListener proceedListener) {
		return null;
	}
}
