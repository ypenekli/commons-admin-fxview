package com.yp.core.fxview.login;

import com.yp.admin.data.Commons;
import com.yp.admin.data.Users;
import com.yp.core.fxview.data.PropertyWraper;
import com.yp.core.tools.DateTime;

import javafx.beans.property.SimpleStringProperty;

public class Account extends PropertyWraper<Users> {
	
	private SimpleStringProperty password2 = new SimpleStringProperty("");

	public Account(Users pUser) {
		super(pUser);
	}

	public Account() {
		super(new Users(-1));
	}
	
	private StringProperty email;

	public StringProperty emailProperty() {
		if (email == null)
			email = new StringProperty(Users.EMAIL, data.getEmail());
		return email;
	}

	public String getEmail() {
		return email.get();
	}

	private StringProperty name;

	public StringProperty nameProperty() {
		if (name == null)
			name = new StringProperty(Users.NAME, data.getName());
		return name;
	}

	public String getName() {
		return name.get();
	}

	private StringProperty surname;

	public StringProperty surnameProperty() {
		if (surname == null)
			surname = new StringProperty(Users.SURNAME, data.getSurname());
		return surname;
	}

	public String getSurname() {
		return surname.get();
	}

	private RefProperty<Integer> homeCity;

	public RefProperty<Integer> homeCityProperty() {
		if (homeCity == null)
			homeCity = new RefProperty<>(Users.HOME_CITY, data.getHomeCityRef(), new Commons(-1));
		return homeCity;
	}

	public String getHomeCity() {
		return homeCity.getValue().getValue();
	}

	private StringProperty phone;

	public StringProperty phoneProperty() {
		if (phone == null)
			phone = new StringProperty(Users.PHONE_NU2, data.getMobilePhoneNu());
		return phone;
	}

	public String getPhone() {
		return phone.get();
	}

	private StringProperty password1;

	public StringProperty password1Property() {
		if (password1 == null)
			password1 = new StringProperty(Users.PASSWORD, data.getPassword());
		return password1;
	}

	public String getPassword() {
		return password1.get();
	}

	public SimpleStringProperty password2Property() {
		return password2;
	}

	public String getPassword2() {
		return password2.get();
	}

	public void setPassword2(String pPassword) {
		password2.set(pPassword);
	}

	public boolean isPasswordsDiffer() {
		return !data.getPassword().equals(getPassword2());
	}

	private DateProperty birthday;

	public DateProperty birthdayProperty() {
		if (birthday == null) {
			birthday = new DateProperty(Users.BIRTH_DATE, DateTime.asLocalDate(data.getBirthDate()));
		}
		return birthday;
	}

	public String getBirthday() {
		return DateTime.asDateTR(birthday.get());
	}

	private StringProperty address;

	public StringProperty addressProperty() {
		if (address == null)
			address = new StringProperty(Users.HOME_ADDRESS, data.getHomeAddress());
		return address;
	}

	public String getAddress() {
		return address.get();
	}

}
