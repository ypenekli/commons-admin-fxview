package com.yp.core.fxview.admin;

import java.util.List;

import com.yp.admin.Constants;
import com.yp.admin.data.Commons;
import com.yp.admin.model.CommonModel;
import com.yp.admin.model.ProjectFuncModel;
import com.yp.admin.model.ProjectModel;
import com.yp.admin.model.ExportModel;
import com.yp.core.fxview.AForm;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;

public abstract class RootPage extends AForm {
	protected static final String ADD_NEW;
	protected static final String UPDATE;
	protected static final String VIEW;
	protected static final String LISTING;
	protected static final String REPORT = "rpr.do";
	private CommonModel commonModel;
	private ProjectModel projectModel;
	private ProjectFuncModel projectFuncModel;
	private ExportModel exportModel;
	protected List<Commons> cityList;
	protected List<Commons> districtList;

	static {
		ADD_NEW = Constants.getString("Addnew");
		UPDATE = Constants.getString("Update");
		VIEW = Constants.getString("View");
		LISTING = Constants.getString("Listing");
	}

	public CommonModel getCommonModel() {
		if (commonModel == null) {
			commonModel = new CommonModel();
		}
		return commonModel;
	}

	public ProjectModel getProjectModel() {
		if (projectModel == null) {
			projectModel = new ProjectModel();
		}
		return projectModel;
	}

	public ProjectFuncModel getProjectFuncModel() {
		if (projectFuncModel == null) {
			projectFuncModel = new ProjectFuncModel();
		}
		return projectFuncModel;
	}

	public ExportModel getExportModel() {
		if (exportModel == null) {
			exportModel = new ExportModel();
		}
		return exportModel;
	}

	public List<Commons> getCityList() {
		if (cityList == null) {
			cityList = getCommonModel().findByParent(Commons.PARENT_ID_CITY_TR);
		}
		return cityList;
	}

	public void showProgress(@SuppressWarnings("rawtypes") final Worker task) {
		app.getHome().progressBar.visibleProperty().bind(task.runningProperty());
		this.self.disableProperty().bind(task.runningProperty());
	}

	public void showProgress(final boolean show) {
		app.getHome().progressBar.setVisible(show);
		app.getHome().progressBar.setProgress(0.0);
		this.self.setDisable(show);
	}

	public void updateProgress(final long taskCount) {
		if (taskCount > 0) {
			Double progres = 1d / taskCount;
			progres += app.getHome().progressBar.getProgress();
			boolean hide = progres > 0.98;
			if (hide)
				progres = 0.0;

			app.getHome().progressBar.setProgress(progres);
			app.getHome().progressBar.setVisible(!hide);
		} else
			app.getHome().progressBar.setProgress(0.0);

	}

	public void bindProgress(Task<?> progres) {
		app.getHome().progressBar.setProgress(0);
		app.getHome().progressBar.setVisible(true);
		app.getHome().progressBar.progressProperty().unbind();
		app.getHome().progressBar.progressProperty().bind(progres.progressProperty());
	}
}