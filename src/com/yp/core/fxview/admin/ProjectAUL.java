package com.yp.core.fxview.admin;

import java.net.URL;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import com.yp.admin.Constants;
import com.yp.admin.data.ProjectFunc;
import com.yp.admin.data.Project;
import com.yp.admin.model.ProjectFuncModel;
import com.yp.core.BaseConstants;
import com.yp.core.entity.IDataEntity;
import com.yp.core.fxview.AForm;
import com.yp.core.ref.IReference;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ProjectAUL extends RootPage {
	@FXML
	private TableView<Project> tProjects;
	@FXML
	private TableView<ProjectFunc> tSubitems;
	@FXML
	private Label txtId;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtUrl;
	@FXML
	private ComboBox<IReference<String>> scmTarget;
	@FXML
	private Button btnAddFunc;
	@FXML
	private Button btnSave;
	@FXML
	private Button btnUp;
	private ProjectFunc selectedProjectFuncs;
	private List<Project> projectList;
	private List<ProjectFunc> subitems;
	private Deque<ProjectFunc> projectNode = new LinkedList<>();

	public void initialize(final URL location, final ResourceBundle resources) {
		scmTarget.setItems(FXCollections.observableArrayList(getProjectFuncModel().getTargetList()));
		buildProjectsTable();
		buildItemsTable();
	}

	public String getHelpFileName() {
		return null;
	}

	public void synchronize(final boolean pToForm, final Object[] pAdditionalParams) {
		if (selectedProjectFuncs != null) {
			if (pToForm) {
				txtId.setText(selectedProjectFuncs.getId());
				txtName.setText(selectedProjectFuncs.getName());
				txtUrl.setText(selectedProjectFuncs.getUrl());
				final IReference<String> target = ProjectFuncModel.TARGET.get(selectedProjectFuncs.getTarget());
				if (target != null) {
					scmTarget.getSelectionModel().select(target);
				} else {
					scmTarget.getSelectionModel().clearSelection();
				}
				if (!selectedProjectFuncs.isLeaf()) {
					subitems = getProjectFuncModel().findProjectFuncs(selectedProjectFuncs.getId());
					refresh(tSubitems, subitems, (IDataEntity) null);
				}
				checkFormItems();
			} else {
				selectedProjectFuncs.setName(txtName.getText());
				selectedProjectFuncs.setUrl(txtUrl.getText());
				if (scmTarget.getSelectionModel().getSelectedIndex() > -1) {
					selectedProjectFuncs.setTarget(scmTarget.getValue().getKey());
				}
			}
		}
	}

	@Override
	public void refresh(final ActionEvent arg0) {
		projectList = getProjectModel().findProjects(getUser().getId());
		refresh(tProjects, projectList, (IDataEntity) null);
	}

	private void showDialog(final Project pProject) {
		AForm form = showModal(this.id, ".ProjectAU", Constants.getString("FrmProjectAUL.Header"), pProject, null,
				false);
		if (form.getResult() != null && form.getResult().isSuccess())
			refresh(null);
	}

	private void updateProject(final ActionEvent arg0) {
		dataEntity = tProjects.getSelectionModel().getSelectedItem();
		showDialog((Project) dataEntity);
	}

	private void buildProjectsTable() {
		final ContextMenu contextMenu = new ContextMenu();
		final MenuItem addMenuItem = new MenuItem(BaseConstants.getString("FrmProjectAUL.Add.Project"));
		final MenuItem updateMenuItem = new MenuItem(BaseConstants.getString("Guncelle"));

		addMenuItem.setOnAction(this::add);
		updateMenuItem.setOnAction(this::updateProject);

		tProjects.setRowFactory(tv -> {
			final TableRow<Project> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getClickCount() == 2) {
					updateProject(null);
				}
			});
			return row;
		});

		contextMenu.getItems().addAll(addMenuItem, new SeparatorMenuItem(), updateMenuItem, new SeparatorMenuItem());
		tProjects.contextMenuProperty().set(contextMenu);
		tProjects.getSelectionModel().selectedIndexProperty().addListener((sec, si1, si2) -> {
			sellectProject(tProjects.getSelectionModel().getSelectedItem());
			synchronize(true, null);
		});
	}

	private void updateFunc(final ActionEvent event) {
		sellectProjectFuncs(tSubitems.getSelectionModel().getSelectedItem());
		if (selectedProjectFuncs != null) {
			synchronize(true, null);
		}
	}

	private void buildItemsTable() {
		final ContextMenu contextMenu = new ContextMenu();
		final MenuItem addLeafMenuItem = new MenuItem(BaseConstants.getString("FrmProjectAUL.Add.Leaf"));
		final MenuItem updateMenuItem = new MenuItem(BaseConstants.getString("Guncelle"));
		addLeafMenuItem.setOnAction(this::addFunc);
		updateMenuItem.setOnAction(this::updateFunc);
		tSubitems.setRowFactory(tv -> {
			final TableRow<ProjectFunc> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty() && event.getClickCount() == 2) {
					sellectProjectFuncs(row.getItem());
					synchronize(true, null);
				}
			});
			return row;
		});
		contextMenu.getItems().addAll(updateMenuItem, new SeparatorMenuItem(), addLeafMenuItem,
				new SeparatorMenuItem());
		tSubitems.contextMenuProperty().set(contextMenu);
	}

	private void sellectProject(final Project de) {
		if (de != null) {
			dataEntity = de;
			projectNode.clear();
			String projectId = de.getId();
			selectedProjectFuncs = new ProjectFunc(projectId);
			selectedProjectFuncs.setProjectId(projectId);
			selectedProjectFuncs.setParentId(projectId);
			selectedProjectFuncs.setName(de.getName());
			selectedProjectFuncs.setUrl(de.getUrl());
			selectedProjectFuncs.setLevel(1);
			selectedProjectFuncs.accept();
		}
	}

	private void sellectProjectFuncs(final ProjectFunc de) {
		projectNode.add(selectedProjectFuncs);
		selectedProjectFuncs = de;
	}

	public void save(final ActionEvent arg0) {
		synchronize(false, null);
		result = getProjectFuncModel().save(selectedProjectFuncs, ((Project) dataEntity).getGroupId(), getUser());
		if (result.isSuccess()) {
			addMessage(BaseConstants.MESSAGE_INFO, result.getMessage());
			if (selectedProjectFuncs.getLevel() > 1) {
				goUp(arg0);
			} else {
				refresh(arg0);
			}
		} else {
			addMessage(BaseConstants.MESSAGE_WARNING, this.result.getMessage());
		}
		this.getAlert().showAndWait();
	}

	@Override
	public void add(final ActionEvent arg0) {
		int idx = 0;
		if (!BaseConstants.isEmpty(projectList))
			idx = projectList.size();
		idx += 1;
		Project newProject = new Project(String.format("0.%s", idx));
		newProject.setAutor(getUser().getFullName());
		showModal(this.id, ".ProjectAU", Constants.getString("FrmProjectAUL.Header"), newProject, null, false);
	}

	public void addFunc(final ActionEvent arg0) {
		if (selectedProjectFuncs != null) {
			sellectProjectFuncs(selectedProjectFuncs.addSubitem(subitems.size(), true));
			synchronize(true, null);
		}
	}

	public void goUp(final ActionEvent arg0) {
		if (!projectNode.isEmpty()) {
			selectedProjectFuncs = projectNode.removeLast();
			synchronize(true, null);
		}
	}

	private void checkFormItems() {
		btnAddFunc.setDisable(selectedProjectFuncs == null || selectedProjectFuncs.isNew());
		btnSave.setDisable(selectedProjectFuncs.getId().equals(selectedProjectFuncs.getParentId()));
	}
}
