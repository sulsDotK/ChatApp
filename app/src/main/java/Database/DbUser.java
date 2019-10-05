package Database;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Suleman Khalid on 11/29/2017.
 */
@IgnoreExtraProperties
@Entity
public class DbUser {
	@Id
	long id;
	private String profilePhotoUrl;
	private String profilePhotoPath;
	private String userName;
	private String phoneNumber;
	private String status;
	private String statusDate;
	private long lastSeen;
	private String token;

	public DbUser() {
		phoneNumber = "";
		profilePhotoUrl = "";
		profilePhotoPath = "";
		userName = "";
		status = "Hi there ! I'm using WhatApp";
		token = "";

		lastSeen = new Date().getTime() / 1000;

		//lastSeen = DateFormat.getDateTimeInstance().format(new Date());

		statusDate = DateFormat.getDateTimeInstance().format(new Date());
		;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getProfilePhotoUrl() {
		return profilePhotoUrl;
	}

	public void setProfilePhotoUrl(String profilePhotoUrl) {
		this.profilePhotoUrl = profilePhotoUrl;
	}

	public String getProfilePhotoPath() {
		return profilePhotoPath;
	}

	public void setProfilePhotoPath(String profilePhotoPath) {
		this.profilePhotoPath = profilePhotoPath;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}

	public long getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getLastSeenString() {
		if (lastSeen == 0) {
			return "Online";
		}

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		Date d = new java.util.Date(lastSeen);
		String reportDate = df.format(d);

		return reportDate;
	}

//    public void setLastSeenString(String lastSeen)
//    {
//        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//
//        Date ls;
//        try
//        {
//            ls = df.parse(lastSeen);
//            this.lastSeen = ls.getTime()/1000;
//        }
//        catch (ParseException e)
//        {
//            e.printStackTrace();
//        }
//    }
}
