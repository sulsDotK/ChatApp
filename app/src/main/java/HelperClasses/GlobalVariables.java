package HelperClasses;

import android.app.Application;
import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.snatik.storage.Storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Database.DbContact;
import Database.DbUser;
import Database.MyObjectBox;
import io.objectbox.BoxStore;

import static com.whatsappclone.TabbedActivity.lastSeenRef;

/**
 * Created by Suleman Khalid on 11/27/2017.
 */

public class GlobalVariables extends Application {
	public final static String PROFILE_PHOTOS_PATH = "/WhatsAppClone/Media/Profile Photos/";
	public final static String USER_PROFILE_PHOTOS_PATH = "/WhatsAppClone/Media/User Profile Photos/";
	public final static String USER_SENT_IMAGES_PATH = "/WhatsAppClone/Media/Images/Sent/";
	public final static String USER_RECEIVED_IMAGES_PATH = "/WhatsAppClone/Media/Images/";
	public final static String USER_SEND_AUDIO_PATH = "/WhatsAppClone/Media/Audio/Sent/";
	public final static String USER_RECEIVED_AUDIO_PATH = "/WhatsAppClone/Media/Audio/";
	public final static String USER_RECIEVED_VOICE_NOTES_PATH = "/WhatsAppClone/Media/Voice Notes/";
	public final static String USER_SENT_VOICE_NOTES_PATH = "/WhatsAppClone/Media/Voice Notes/Sent/";
	public final static String USER_RECIEVED_VIDEOS_PATH = "/WhatsAppClone/WhatsAppClone Movies/Received/";
	public final static String USER_SENT_VIDEOS_PATH = "/WhatsAppClone/WhatsAppClone Movies/";
	public static AtomicInteger ACTIVITIES_COUNT = new AtomicInteger(0);
	private static GlobalVariables instance;
	private Storage storage;
	private DbUser currentUser;
	private List<DbContact> userContacts;
	private BoxStore boxStore;

	private GlobalVariables(Context context) {
		currentUser = new DbUser();
		userContacts = new ArrayList<>();
		boxStore = MyObjectBox.builder().androidContext(context).build();
		storage = new Storage(context);
		FirebaseDatabase.getInstance().setPersistenceEnabled(true);
	}

	public static GlobalVariables getInstance(Context ctx) {
		if (instance == null) {
			instance = new GlobalVariables(ctx);
			instance.attachBaseContext(ctx);
		}
		return instance;
	}

	public static void decrementActivities() {
		if (ACTIVITIES_COUNT.decrementAndGet() == 0) {
			if (lastSeenRef != null) {
				lastSeenRef.setValue(new Date().getTime());
			}
		}
	}

	public Storage getStorage() {
		return storage;
	}

	public BoxStore getBoxStore() {
		return boxStore;
	}

	public List<DbContact> getUserContacts() {
		return userContacts;
	}

	public void setCurrentUserContacts(List<DbContact> currentUserContacts) {
		this.userContacts = currentUserContacts;
	}

	public DbUser getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(DbUser currentUser) {
		this.currentUser = currentUser;
	}
}
