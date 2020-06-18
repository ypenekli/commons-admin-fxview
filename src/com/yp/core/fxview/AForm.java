package com.yp.core.fxview;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import com.yp.Root;
import com.yp.admin.model.GroupModel;
import com.yp.admin.model.UserModel;
import com.yp.core.BaseConstants;
import com.yp.core.entity.IDataEntity;
import com.yp.core.entity.IResult;
import com.yp.core.fxview.web.Browser;
import com.yp.core.tools.DateTime;
import com.yp.core.tools.StringTool;
import com.yp.core.user.IUser;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class AForm implements Initializable {

	@FXML
	protected String id;
	@FXML
	protected Pane self;

	protected static ALauncher app;
	protected IResult<?> result;
	protected IDataEntity dataEntity;
	protected List<IDataEntity> list;
	public static NumberFormat FORMAT_CURRENCY;
	protected String message;
	private Alert alert;
	private Alert confirm;
	private UserModel userModel;
	private GroupModel groupModel;
	private Date date;
	private Calendar c;
	private Property<String> dIslemack;
	private BooleanProperty dGosterMeKapat;
	private BooleanProperty dGosterMeRefresh;
	private BooleanProperty dGosterMeEkle;
	private BooleanProperty dGosterMeKaydet;
	private BooleanProperty dGosterMeRaporla;
	private StringProperty dEtiketKaydet;
	protected OnFindAllCompletedListener<?> onFindAllCompletedListener;
	protected OnFindOneCompletedListener<?> onFindOneCompletedListener;
	protected OnSaveCompletedListener<?> onSaveCompletedListener;

	static {
		AForm.FORMAT_CURRENCY = NumberFormat.getCurrencyInstance();
	}

	public AForm() {
		this.dIslemack = new SimpleStringProperty();
		this.dGosterMeKapat = new SimpleBooleanProperty();
		this.dGosterMeRefresh = new SimpleBooleanProperty();
		this.dGosterMeEkle = new SimpleBooleanProperty();
		this.dGosterMeKaydet = new SimpleBooleanProperty();
		this.dGosterMeRaporla = new SimpleBooleanProperty();
		this.dEtiketKaydet = new SimpleStringProperty(BaseConstants.getString("Kaydet"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void refresh(final TableView<? extends IDataEntity> pTablo, final List pList) {
		pTablo.setItems(null);
		if (!BaseConstants.isEmpty(pList)) {
			pTablo.setItems(FXCollections.observableList(pList));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IDataEntity refresh(final TableView pTablo, final List pList, final IDataEntity pSelection) {
		IDataEntity selection = pSelection;
		if (selection == null)
			selection = (IDataEntity) pTablo.getSelectionModel().getSelectedItem();

		pTablo.setItems(null);
		if (!BaseConstants.isEmpty(pList)) {
			IDataEntity dVs = (IDataEntity) pList.get(pList.size() - 1);
			if (dVs.isNew())
				pList.remove(pList.size() - 1);

			pTablo.layout();
			pTablo.setItems(FXCollections.observableList(pList));
			if (selection != null)
				pTablo.getSelectionModel().select(selection);
			else {
				pTablo.getSelectionModel().clearAndSelect(0);
				selection = (IDataEntity) pTablo.getSelectionModel().getSelectedItem();
			}
		}
		return selection;
	}

	public abstract String getHelpFileName();

	public abstract void synchronize(boolean pToForm, Object[] pAdditionalParams);

	public String getMessage() {
		return message;
	}

	protected Alert getAlert() {
		if (alert == null) {
			ButtonType ok = new ButtonType(BaseConstants.getString("Tamam"), ButtonBar.ButtonData.OK_DONE);
			alert = new Alert(AlertType.INFORMATION, "", ok);
			alert.setResizable(true);
			alert.setTitle(app.getApplicationName());
			alert.setHeaderText("");
		}
		return alert;
	}

	public static final ButtonType CONFIRM_OK = new ButtonType(BaseConstants.getString("Tamam"),
			ButtonType.OK.getButtonData());
	public static final ButtonType CONFIRM_CANCEL = new ButtonType(BaseConstants.getString("Iptal"),
			ButtonType.CANCEL.getButtonData());

	protected Alert getConfirm() {
		if (confirm == null) {
			confirm = new Alert(AlertType.CONFIRMATION, "", CONFIRM_OK, CONFIRM_CANCEL);
			confirm.setResizable(true);
			confirm.setTitle(app.getApplicationName());
			confirm.setHeaderText("");
		}
		return confirm;
	}

	public void addMessage(String summary, String detail) {
		getAlert().setHeaderText(summary);
		if (summary.equals(BaseConstants.MESSAGE_WARNING))
			alert.setAlertType(AlertType.ERROR);
		else
			alert.setAlertType(AlertType.INFORMATION);

		alert.contentTextProperty().set(detail);
	}

	protected byte[] getImage(String fileName) {
		InputStream is = null;
		byte[] b = null;
		try {
			is = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream(fileName));
			b = new byte[is.available()];
			is.read(b);
		} catch (IOException e) {
			app.logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
		return b;
	}

	public IUser getUser() {
		return app.user;
	}

	public UserModel getUserModel() {
		if (userModel == null) {
			userModel = new UserModel();
		}
		return userModel;
	}

	public GroupModel getGroupModel() {
		if (groupModel == null) {
			groupModel = new GroupModel();
		}
		return groupModel;
	}

	public IDataEntity getDataEntity() {
		return dataEntity;
	}

	public void show(String pIslvkod, String pUrl, String pBaslik, IDataEntity pDataEntity, ResourceBundle pResources) {
		String fnId = pIslvkod + pUrl;
		app.logger.log(Level.INFO, "Sub function id {0}.", fnId);
		URL dUrl = null;
		Pane dForm = app.getForm(fnId);
		if (dForm == null && pUrl != null) {
			try {
				String dSayfa = app.getConfig().getString(pUrl);
				dUrl = Root.class.getResource(dSayfa);
				FXMLLoader fxmlLoader = new FXMLLoader(dUrl, pResources == null ? app.getBundle() : pResources);
				dForm = fxmlLoader.load();
				AForm aForm = fxmlLoader.<AForm>getController();
				aForm.id = pIslvkod;
				dForm.setUserData(aForm);
				dForm.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
					if (evt.getCode() == KeyCode.F1) {
						showHelp("YARDIM", null, aForm);
					}
				});
				app.loadForm(fnId, dForm);
			} catch (Exception e) {
				app.logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		if (dForm != null) {
			if (!StringTool.isNull(pBaslik))
				app.getPrimaryStage().setTitle(app.getApplicationName() + "-> " + pBaslik);

			BorderPane.setAlignment(dForm, Pos.TOP_LEFT);
			app.getHome().root.setCenter(dForm);

			AForm aForm = (AForm) dForm.getUserData();
			aForm.dataEntity = pDataEntity;
			aForm.refresh(null);
			aForm.synchronize(true, null);
		}
	}

	protected AForm showModal(String pIslvkod, String pUrl, String pTitle, IDataEntity pDataEntity,
			ArrayList<IDataEntity> pList, boolean pResizable) {
		String fnId = pIslvkod + pUrl;
		app.logger.log(Level.INFO, "Sub function id {0}.", fnId);
		URL dUrl = null;
		Pane dForm = app.getForm(fnId);
		if (dForm == null && pUrl != null) {
			try {
				String dSayfa = app.getConfig().getString(pUrl);
				dUrl = Root.class.getResource(dSayfa);
				FXMLLoader fxmlLoader = new FXMLLoader(dUrl, app.getBundle());
				dForm = fxmlLoader.load();
				AForm aForm1 = fxmlLoader.<AForm>getController();
				aForm1.id = pIslvkod;
				dForm.setUserData(aForm1);
				dForm.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
					if (evt.getCode() == KeyCode.F1) {
						showHelp("YARDIM", null, aForm1);
					}
				});

				aForm1.setOnFindAllCompletedListener(onFindAllCompletedListener);
				aForm1.setOnFindOneCompletedListener(onFindOneCompletedListener);
				aForm1.setOnSaveCompletedListener(onSaveCompletedListener);

				app.loadForm(fnId, dForm);
			} catch (Exception e) {
				app.logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		if (dForm != null) {
			Scene scene = dForm.getScene();
			if (scene == null)
				scene = new Scene(dForm);
			Stage stage = new Stage();
			stage.setResizable(pResizable);
			stage.setScene(scene);
			stage.setTitle(pTitle);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(app.getPrimaryStage().getScene().getWindow());
			stage.centerOnScreen();
			final AForm sncForm = (AForm) dForm.getUserData();
			stage.setOnCloseRequest(handle -> sncForm.close((ActionEvent) null));

			sncForm.dataEntity = pDataEntity;
			sncForm.list = pList;
			sncForm.refresh(null);
			sncForm.synchronize(true, null);
			stage.showAndWait();
			return sncForm;
		}
		return null;
	}

	public AForm showDialog(String pUrl, String pTitle, IDataEntity pDataEntity, List<IDataEntity> pList,
			boolean pResizable) {
		Stage stage = new Stage();
		Parent root = null;
		AForm aForm = null;
		try {
			String dSayfa = app.getConfig().getString(pUrl);
			URL dUrl = Root.class.getResource(dSayfa);
			FXMLLoader fxmlLoader = new FXMLLoader(dUrl, app.getBundle());
			root = fxmlLoader.load();
			aForm = fxmlLoader.<AForm>getController();
			aForm.setOnFindAllCompletedListener(onFindAllCompletedListener);
			aForm.setOnFindOneCompletedListener(onFindOneCompletedListener);
			aForm.setOnSaveCompletedListener(onSaveCompletedListener);
		} catch (IOException e) {
			app.logger.log(Level.SEVERE, e.getMessage(), e);
		}
		if (root != null) {
			Scene scene = new Scene(root);
			stage.setResizable(pResizable);
			stage.setScene(scene);
			stage.setTitle(pTitle);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.centerOnScreen();
			if (aForm != null) {
				aForm.dataEntity = pDataEntity;
				aForm.list = pList;
				aForm.refresh(null);
				aForm.synchronize(true, null);
			}
			stage.showAndWait();
		}
		return aForm;
	}

	public AForm showModelessDialog(String pUrl, String pTitle, IDataEntity pDataEntity, List<IDataEntity> pList,
			boolean pResizable) {
		Stage stage = new Stage();
		Parent root = null;
		AForm aForm = null;
		try {
			String dSayfa = app.getConfig().getString(pUrl);
			URL dUrl = Root.class.getResource(dSayfa);
			FXMLLoader fxmlLoader = new FXMLLoader(dUrl, app.getBundle());
			root = fxmlLoader.load();
			aForm = fxmlLoader.<AForm>getController();
			aForm.setOnFindAllCompletedListener(onFindAllCompletedListener);
			aForm.setOnFindOneCompletedListener(onFindOneCompletedListener);
			aForm.setOnSaveCompletedListener(onSaveCompletedListener);
		} catch (IOException e) {
			app.logger.log(Level.SEVERE, e.getMessage(), e);
		}
		if (root != null) {
			Scene scene = new Scene(root);
			stage.setResizable(pResizable);
			stage.setScene(scene);
			stage.setTitle(pTitle);
			stage.initOwner(app.getPrimaryStage().getScene().getWindow());
			stage.centerOnScreen();
			if (aForm != null) {
				aForm.dataEntity = pDataEntity;
				aForm.list = pList;
				aForm.refresh(null);
				aForm.synchronize(true, null);
			}
			stage.show();
		}
		return aForm;
	}

	public void showWebDialog(String pUrl, String pTitle, String pMimeType, IDataEntity pDataEntity) {
		if (app.WEB == null && pUrl != null) {
			Stage stage = new Stage();
			stage.setTitle(pTitle);
			Browser bw = new Browser();
			Scene scene = new Scene(bw, 750, 500, Color.web("#666970"));
			stage.setScene(scene);
			stage.setUserData(pDataEntity);
			app.WEB = stage;
			stage.setOnCloseRequest(event -> System.out.println("Stage is closing"));
		}
		((Browser) app.WEB.getScene().getRoot()).load(pUrl, pTitle, pMimeType);
		app.WEB.showAndWait();
	}

	public void showWebDialog(String pContent, String pTitle, String pMimeType) {
		if (app.WEB == null && pContent != null) {
			Stage stage = new Stage();
			stage.setTitle(pTitle);
			Browser bw = new Browser();
			Scene scene = new Scene(bw, 750, 500, Color.web("#666970"));
			stage.setScene(scene);
			// stage.setUserData(pDataEntity);
			app.WEB = stage;
			stage.setOnCloseRequest(event -> System.out.println("Stage is closing"));
		}
		((Browser) app.WEB.getScene().getRoot()).loadContent(pContent, pMimeType);
		app.WEB.showAndWait();
	}

	public void showHelp(String pBaslik, String pMimeTipi, AForm pForm) {
		String url = pForm == null ? getHelpFileName() : pForm.getHelpFileName();
		if (!StringTool.isNull(url)) {
			if (app.getHelpUrl().startsWith("http")) {
				url = app.getHelpUrl() + url;
			} else {
				url = "file:///" + BaseConstants.getRootAddress() + app.getHelpUrl() + url;
			}
			showWebDialog(url, pBaslik, pMimeTipi, null);
		}
	}

	public void close(AForm pForm) {
		// Pane dP = (Pane) session.getPrimaryStage().getScene().getRoot();
		// dP.getChildren().removeAll(session.getForm(pForm.id));
		app.getHome().root.setCenter(null);
	}

	protected String getClientIP() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException h) {
			app.logger.log(Level.SEVERE, h.getMessage(), h);
		}
		return "1.1.1.1";
	}

	protected void setLastUserInfo(IDataEntity pDataEntity) {
		String user = "Admin@yp.com";
		if (app.getUser() != null) {
			user = app.getUser().getEmail();
		}
		pDataEntity.setLastUserInfo(user, getClientIP(), getDate());
	}

	protected void setUserInfo(IDataEntity pDataEntity) {
		String user = "Admin@yp.com";
		if (app.getUser() != null) {
			user = app.getUser().getEmail();
		}
		pDataEntity.setUserInfo(user, getClientIP(), getDate());
	}

	public Date getDate() {
		if (date == null) {
			date = new Date();
		}
		return date;
	}

	public Calendar today() {
		if (c == null) {
			c = Calendar.getInstance();
		}
		return c;
	}

	public String getDateTR(BigDecimal pDate) {
		return DateTime.asDateTR(pDate);
	}

	public void setDataEntity(IDataEntity pDataEntity) {
		dataEntity = pDataEntity;
		if (dataEntity != null) {
			setIslemack(dataEntity.isNew() ? BaseConstants.NEW : BaseConstants.UPDATE);
		}
	}

	public Property<String> islemackProperty() {
		return dIslemack;
	}

	public String getIslemack() {
		return dIslemack.getValue();
	}

	public void setIslemack(String pIslemack) {
		dIslemack.setValue(pIslemack);
	}

	public BooleanProperty gosterMeKapatProperty() {
		return dGosterMeKapat;
	}

	public BooleanProperty gosterMeRefreshProperty() {
		return dGosterMeRefresh;
	}

	public BooleanProperty gosterMeEkleProperty() {
		return dGosterMeEkle;
	}

	public BooleanProperty gosterMeKaydetProperty() {
		return dGosterMeKaydet;
	}

	public BooleanProperty gosterMeRaporlaProperty() {
		return dGosterMeRaporla;
	}

	public Boolean getGosterMeKapat() {
		return dGosterMeKapat.getValue();
	}

	public void setGosterMeKapat(Boolean pGosterMe) {
		dGosterMeKapat.setValue(pGosterMe);
	}

	public Boolean getGosterMerefresh() {
		return dGosterMeRefresh.getValue();
	}

	public void setGosterMerefresh(Boolean pGosterMe) {
		dGosterMeRefresh.setValue(pGosterMe);
	}

	public Boolean getGosterMeEkle() {
		return dGosterMeEkle.getValue();
	}

	public void setGosterMeEkle(Boolean pGosterMe) {
		dGosterMeEkle.setValue(pGosterMe);
	}

	public Boolean getGosterMeKaydet() {
		return dGosterMeKaydet.getValue();
	}

	public void setGosterMeKaydet(Boolean pGosterMe) {
		dGosterMeKaydet.setValue(pGosterMe);
	}

	public Boolean getGosterMeRaporla() {
		return dGosterMeRaporla.getValue();
	}

	public void setGosterMeRaporla(Boolean pGosterMe) {
		dGosterMeRaporla.setValue(pGosterMe);
	}

	public StringProperty etiketKaydetProperty() {
		return dEtiketKaydet;
	}

	public String getEtiketKaydet() {
		return dEtiketKaydet.getValue();
	}

	public void setEtiketKaydet(String pEtiket) {
		dEtiketKaydet.setValue(pEtiket);
	}

	@FXML
	public void close(ActionEvent arg0) {
		show(id, ".Bos", null, dataEntity, null);
	}

	@FXML
	public void cancel(ActionEvent arg0) {
		show(id, ".Bos", null, dataEntity, null);
	}

	@FXML
	public void refresh(ActionEvent arg0) {
		System.out.println("refresh");
	}

	@FXML
	public void add(ActionEvent arg0) {
		System.out.println("ekle");
	}

	@FXML
	public void save(ActionEvent arg0) {
		System.out.println("kaydet");
	}

	@FXML
	public void report(ActionEvent arg0) {
		System.out.println("raporla");
	}

	@SuppressWarnings("rawtypes")
	@FXML
	public IResult getResult() {
		return result;
	}

	public void customResize(TableView<?> view) {

		AtomicLong width = new AtomicLong();
		view.getColumns().forEach(col -> {
			width.addAndGet((long) col.getWidth());
		});
		double tableWidth = view.getWidth();

		if (tableWidth > width.get()) {
			view.getColumns().forEach(col -> {
				col.setPrefWidth(col.getWidth() + ((tableWidth - width.get()) / view.getColumns().size()));
			});
		}
	}

	public List<IDataEntity> getList() {
		return list;
	}

	public void setListe(List<IDataEntity> pList) {
		list = pList;
	}

	public void setOnFindAllCompletedListener(OnFindAllCompletedListener<?> listener) {
		onFindAllCompletedListener = listener;
	}

	public void setOnFindOneCompletedListener(OnFindOneCompletedListener<?> listener) {
		onFindOneCompletedListener = listener;
	}

	public void setOnSaveCompletedListener(OnSaveCompletedListener<?> listener) {
		onSaveCompletedListener = listener;
	}

	public interface OnFindAllCompletedListener<T extends IDataEntity> {
		void onFindCompleted(List<T> list);
	}

	public interface OnFindOneCompletedListener<T extends IDataEntity> {
		void onFindCompleted(T vs);
	}

	public interface OnSaveCompletedListener<T> {
		void onSaveCompleted(IResult<T> result);
	}
}
