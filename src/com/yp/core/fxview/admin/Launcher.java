package com.yp.core.fxview.admin;

import java.util.List;
import java.util.ResourceBundle;

import com.yp.admin.data.Projects;
import com.yp.admin.model.GroupModel;
import com.yp.core.BaseConstants;
import com.yp.core.fxview.ALauncher;
import com.yp.core.user.IUser;

import javafx.stage.Stage;

public class Launcher extends ALauncher {

	private static final ResourceBundle configBundle;

	static {
		configBundle = ResourceBundle.getBundle("fxview.admin.Config");
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	protected List<Projects> findRootMenuList(IUser pUser) {
		return new GroupModel().findGroupList(pUser.getUserId(), getApplicationId());
	}

	public String getFormUrl(String key) {
		return configBundle.getString(key);
	}

	@Override
	public ResourceBundle getBundle() {
		return BaseConstants.BUNDLE_MESSAGE;
	}

	@Override
	public String getStringConstant(String key) {
		return BaseConstants.getString(key);
	}

}
