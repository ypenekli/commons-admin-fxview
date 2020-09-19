package com.yp.core.fxview.login;

import java.net.URL;
import java.util.ResourceBundle;

import com.yp.admin.data.Users;
import com.yp.core.fxview.AForm;
import com.yp.core.tools.StringTool;
import com.yp.core.user.IUser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ChangePassword extends AForm {
	@FXML
	private Label lbUserName;
	@FXML
	private PasswordField txtPassword, txtNewPassword, txtNewPasswordConfirmed;
	@FXML
	private Label lbMessage;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

	@FXML
	protected void changePassword(ActionEvent event) {
		lbMessage.setText("");
		if (!StringTool.isNull(txtPassword.getText()) && !StringTool.isNull(txtNewPassword.getText())
				&& !StringTool.isNull(txtNewPasswordConfirmed.getText())) {

			result = getUserModel().changePassword(getUser().getEmail(), txtPassword.getText(),
					txtNewPassword.getText(), txtNewPasswordConfirmed.getText(), getUser(), getClientIP());

			if (result != null) {
				lbMessage.setText(result.getMessage());
			}
		}
	}

	@Override
	public IUser getUser() {
		if (dataEntity == null)
			dataEntity = new Users();
		return (IUser) dataEntity;
	}

	@Override
	public String getHelpFileName() {
		return null;
	}

	@Override
	public void synchronize(boolean pToForm, Object[] pAdditionalParams) {
		if (pToForm)
			lbUserName.setText(getUser().getEmail());

	}

}