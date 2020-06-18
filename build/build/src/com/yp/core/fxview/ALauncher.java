package com.yp.core.fxview;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.yp.core.BaseConstants;
import com.yp.core.entity.IResult;
import com.yp.core.entity.Result;
import com.yp.core.log.MyLogger;
import com.yp.core.ref.IReference;
import com.yp.core.ref.Reference;
import com.yp.core.tools.DateTime;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public abstract class ALauncher extends Application {

	protected static final String BACKUPFILENAME = ".backup";
	protected static final String DBFILENAME = "data1";	
	protected static final String DBDIR = "db";	
	protected static final String BACKUPDIR = "backup";	
	protected static boolean isCreateDb;
	protected static String updateUrl;

	protected static final String CREATEDB = "--createdb=";
	protected static final String URL = "--url=";
	protected static final String PATH = "--path=";

	@Override
	public void init() throws Exception {
		Parameters params = getParameters();
		if (params != null) {
			params.getNamed().forEach((k, v) -> System.out.println(k + ": " + v));
			params.getUnnamed().forEach(System.out::println);

			if (params.getNamed().containsKey(PATH))
				BaseConstants.setRootAddress(params.getNamed().get(PATH));
			isCreateDb = params.getNamed().containsKey(CREATEDB);
		}

		createDb();
		restore();
		checkApplicationRelease();
		Logger.getLogger(MyLogger.NAME).log(Level.INFO, "Application is starting");
		super.init();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Button btn = new Button();
		btn.setText("Say 'Hello World");
		btn.setOnAction(event -> {
			System.out.println("Hello World!");
			try {
				String root = BaseConstants.getRootAddress();
				System.out.println(root);

				IReference<String> ref = new Reference<>("A", BaseConstants.getString("1028"));
				System.out.println("ref :" + ref.getDefinition());
			} catch (Exception e) {
				System.err.println(e);
			}
		});
		StackPane root = new StackPane();
		root.getChildren().add(btn);
		Scene scene = new Scene(root, 300, 250);

		primaryStage.setTitle("Hello World");
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	protected void createDb() {
		Logger.getLogger(MyLogger.NAME).log(Level.INFO, "Create database..");
	}

	public void restore() {
		Logger.getLogger(MyLogger.NAME).log(Level.INFO, "Restore database..");
		String source = null;
		File fKaynak = null;
		String path = BaseConstants.getRootAddress();
		File file = new File(Paths.get(path, DBDIR, BACKUPFILENAME).toUri());
		if (file.exists() && file.canRead()) {
			fKaynak = file;
			java.util.List<String> list = null;
			try {
				list = Files.readAllLines(file.toPath());
			} catch (IOException h) {
				Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, h.getMessage(), h);
			}
			if (!BaseConstants.isEmpty(list)) {
				source = list.get(0);
			}
		}

		if (source != null) {
			backup(DBFILENAME, null);
			File target = new File(Paths.get(path, DBDIR, DBFILENAME).toUri());
			File sourceFile = new File(source);
			try {
				if (target.exists()) {
					FileUtils.deleteDirectory(target);
				}
				FileUtils.copyDirectory(sourceFile, target);
				FileUtils.deleteQuietly(fKaynak);
			} catch (IOException h) {
				Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, h.getMessage(), h);
			}

		}

	}

	public IResult<String> backup(String pDbName, String pBackupFileName) {
		Logger.getLogger(MyLogger.NAME).log(Level.INFO, "Backup database..");
		IResult<String> dSnc = new Result<>(true, "");

		if (pDbName == null)
			pDbName = DBFILENAME;
		if (pBackupFileName == null)
			pBackupFileName = pDbName + "-" + DateTime.dbNow().toString();
		String path = BaseConstants.getRootAddress();

		try {
			Path target = Paths.get(path, BACKUPDIR, pBackupFileName);
			Path source = Paths.get(path, DBDIR, pDbName);

			Files.deleteIfExists(target);
			FileUtils.copyDirectory(source.toFile(), target.toFile());

		} catch (IOException h) {
			dSnc.setSuccess(false);
			dSnc.setMessage(BaseConstants.getString("FrmAyar.Yedekleme.Basarisiz"));
			Logger.getLogger(MyLogger.NAME).log(Level.SEVERE, h.getMessage(), h);
		}
		if (dSnc.isSuccess())
			dSnc.setMessage(BaseConstants.getString("FrmAyar.Yedekleme.Basarili") + pBackupFileName);

		return dSnc;
	}

	protected boolean checkApplicationConfig() {
		Logger.getLogger(MyLogger.NAME).log(Level.INFO, "Check application config..");
		return true;
	}

	protected static void checkApplicationRelease() {
		Logger.getLogger(MyLogger.NAME).log(Level.INFO, "Check application release..");
	}
	
	


	public static void main(String[] args) {
		launch(args);
	}	

}
