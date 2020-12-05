package com.yp.core.fxview.admin;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.yp.admin.data.Group;
import com.yp.admin.data.Project;
import com.yp.core.BaseConstants;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class GroupAU extends RootPage {
	@FXML
	private Label txtGroupId;
	@FXML
	private ComboBox<Project> chProjects;
	@FXML
	private TextField txtGroupName;
	
	
	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
	}

	public String getHelpFileName() {
		return null;
	}

	@Override
	public void synchronize(final boolean pToForm, final Object[] pAdditionalParams) {
		if (dataEntity != null) {
			final Group group = (Group) dataEntity;
			if (pToForm) {
				txtGroupId.setText(group.getId().toString());
				txtGroupName.setText(group.getName());
				chProjects.getSelectionModel().select(new Project(group.getProjectId()));
			} else {
				group.setName(txtGroupName.getText());
				if (chProjects.getSelectionModel().getSelectedIndex() > -1) {
					group.setProjectId(chProjects.getValue().getId());
				}
			}
			chProjects.setDisable(!dataEntity.isNew());
		}
	}

	@Override
	public void refresh(final ActionEvent arg0) {
		chProjects.getItems().clear();
		List<Project> projectList = getProjectModel().findProjects(getUser().getId());
		if (!BaseConstants.isEmpty(projectList))
			chProjects.setItems(FXCollections.observableArrayList(projectList));
	}

	@Override
	public void save(final ActionEvent arg0) {
		synchronize(false, null);
		result = this.getGroupModel().save((Group) dataEntity, getUser());
		if (result.isSuccess()) {
			addMessage(BaseConstants.MESSAGE_INFO, result.getMessage());
		} else {
			addMessage(BaseConstants.MESSAGE_WARNING, result.getMessage());
		}
		getAlert().showAndWait();
	}

	@Override
	public void close(ActionEvent arg0) {
		hide(arg0);
	}

	public void add(ActionEvent arg0) {
		dataEntity = new Group(-1);
		synchronize(true, (Object[]) null);
	}
}
