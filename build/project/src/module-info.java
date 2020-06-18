module commons.admin.fxview {
	exports com.yp.core.fxview;
	exports com.yp.core.fxview.admin;

	requires commons.data;
	requires java.logging;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires org.apache.commons.io;
	requires gson;
	
	
	opens com.yp.core.fxview.admin;
}