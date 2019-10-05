package com.whatsappclone;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.intentfilter.androidpermissions.PermissionManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.rahimlis.badgedtablayout.BadgedTabLayout;
import com.snatik.storage.Storage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Database.Chat;
import Database.ChatMessage;
import Database.Chat_;
import Database.DbContact;
import Database.DbUser;
import Database.PushKey;
import HelperClasses.FirebaseMessage;
import HelperClasses.GlobalFunctions;
import HelperClasses.GlobalVariables;
import io.objectbox.Box;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;

import static java.util.Collections.singleton;


public class TabbedActivity extends AppCompatActivity {
	private static final String TAG = "TabbedActivity";
	public static DatabaseReference lastSeenRef;
	static boolean isGoingInBackground = true;
	static BadgedTabLayout badgedTabLayout;
	PermissionManager permissionManager;
	ChildEventListener newMessageListener;
	DatabaseReference mMessagesReference;
	DatabaseReference mMyMessagesReference;
	DatabaseReference mSenderMessagesReference;
	private FirebaseDatabase mFirebaseDatabase;
	private ChildEventListener mChildEventListener;
	private FirebaseAuth mFireBaseAuth;
	private FirebaseAuth.AuthStateListener mAuthStateListener;
	private FirebaseStorage mFirebaseStorage;
	private StorageReference mUsersContactsStorageReference;
	private DatabaseReference mUsersDatabaseReference;

	//    private boolean
	private StorageReference mUserContactsStorageReference;
	private DatabaseReference mUserDatabaseReference;
	private DatabaseReference connectedRef;
	private ValueEventListener connectedRefListener;
	private Menu myMenu;
	/**
	 * The {@link PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;

	private void initDisplay() {
		setContentView(R.layout.activity_main);

		// Create global configuration and initialize ImageLoader with this config
		ImageLoaderConfiguration config = new ImageLoaderConfiguration
				                                      .Builder(this).build();

		ImageLoader.getInstance().init(config);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		badgedTabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		findViewById(R.id.mainSearchBar).setVisibility(View.GONE);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initDisplay();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.

		// Initialise firebase components
		mFirebaseDatabase = FirebaseDatabase.getInstance();
		mFireBaseAuth = FirebaseAuth.getInstance();
		mFirebaseStorage = FirebaseStorage.getInstance();

		// set persistence enabled
		GlobalVariables.getInstance(getApplicationContext());

		permissionManager = PermissionManager.getInstance(TabbedActivity.this);
		connectedRef = mFirebaseDatabase.getReference(".info/connected");
		connectedRefListener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				boolean connected = snapshot.getValue(Boolean.class);
				if (connected) {
					// when I disconnect, update the last time I was seen online
					lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
					lastSeenRef.setValue(0);
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				System.err.println("Listener was cancelled at .info/connected");
			}
		};

		mAuthStateListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				final FirebaseUser user = mFireBaseAuth.getCurrentUser();

				if (user != null) {
					//initDisplay();
					onSignedInInitialise(user);
					connectedRef.addValueEventListener(connectedRefListener);
				} else {
					Intent intent = new Intent(TabbedActivity.this, WelcomeScreen.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}
		};
	}

	private void onSignedInInitialise(FirebaseUser user) {
		ArrayList<String> permissions = new ArrayList<>();
		permissions.add(android.Manifest.permission.READ_CONTACTS);
		permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		permissionManager.checkPermissions(permissions,
		                                   new PermissionManager.PermissionRequestListener() {
			                                   @Override
			                                   public void onPermissionGranted() {

			                                   }

			                                   @Override
			                                   public void onPermissionDenied() {
				                                   Toast.makeText(getApplicationContext(), "Contacts & Storage Permissions are needed for WhatsApp to function !",
				                                                  Toast.LENGTH_SHORT).show();
				                                   finishAffinity();
			                                   }
		                                   });

		final GlobalVariables variables = GlobalVariables.getInstance(getApplicationContext());

		Box<DbUser> dbUserBox = variables.getBoxStore().boxFor(DbUser.class);

		Box<DbContact> dbContactBox = variables.getBoxStore().boxFor(DbContact.class);

		List<DbContact> dbContactList = dbContactBox.getAll();
		variables.setCurrentUserContacts(dbContactList);

		QueryBuilder<DbUser> builder = dbUserBox.query();
		Query<DbUser> query = builder.build();

		variables.setCurrentUser(query.findFirst());

		mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");
		mUsersContactsStorageReference = mFirebaseStorage
				                                 .getReference().child("Users_Contacts_Photos");
		mUserDatabaseReference = mUsersDatabaseReference.child(GlobalVariables.
				                                                                      getInstance(getApplicationContext()).getCurrentUser().getPhoneNumber());
		lastSeenRef = mUserDatabaseReference.child("lastSeen");

		mUserDatabaseReference = mUsersDatabaseReference.child(user.getPhoneNumber());

		mMessagesReference = mFirebaseDatabase.getReference().child("Messages");
		mMyMessagesReference = mMessagesReference.child(variables.getCurrentUser().getPhoneNumber());

		mSenderMessagesReference = null;

		//  get all Contacts
		final List<DbContact> contactsList = variables.getUserContacts();
		for (int i = 0; i < contactsList.size(); i++) {
			final DbContact contact = contactsList.get(i);
			final String sender = contactsList.get(i).getPhoneNumber();
			mSenderMessagesReference = mMyMessagesReference.child(sender);

			mSenderMessagesReference.addChildEventListener(new ChildEventListener() {
				@Override
				public void onChildAdded(DataSnapshot dataSnapshot, String s) {
					FirebaseMessage newMessage = dataSnapshot.getValue(FirebaseMessage.class);

					Query<Chat> query = variables.getBoxStore().boxFor(Chat.class)
							                    .query().equal(Chat_.withWhom, "").build();

					Chat chatObject = query.setParameter(Chat_.withWhom,
					                                     sender).findFirst();

					final PushKey key = new PushKey(dataSnapshot.getKey());

					if (newMessage.getStatus() != null && !newMessage.getStatus().equals("sent")) {
						return;
					} else {
						if (chatObject == null) {
							chatObject = new Chat(sender, 0);
						}

						if (chatObject.unreadKeys.contains(key)) {
							return;
						}

						mSenderMessagesReference.child(dataSnapshot.getKey())
								.child("status").setValue("received");

						newMessage.setStatus("received");

						DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						Date d = new Date();
						newMessage.setTimeStamp(df.format(d));
					}

					final ChatMessage message = new ChatMessage(chatObject, newMessage);

//                    if(!message.getMediaUrl().isEmpty())
//                    {
//                        ImageLoader imageLoader = ImageLoader.getInstance();
//
//                        String imageUri = message.getMediaUrl();
//
//                        final Chat finalChatObject = chatObject;
//
//                        imageLoader.loadImage(imageUri, new SimpleImageLoadingListener()
//                        {
//                            @Override
//                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
//                            {
//                                saveToStorage(message, loadedImage);
//                                finalChatObject.messages.add(message);
//                                finalChatObject.unreadKeys.add(key);
//                                variables.getBoxStore().boxFor(Chat.class).put(finalChatObject);
//                            }
//                        });
//                    }
//                    else
					{
						chatObject.messages.add(message);

						chatObject.unreadKeys.add(key);

						variables.getBoxStore().boxFor(Chat.class).put(chatObject);
					}

					sendNotification(message, contact);
				}

				@Override
				public void onChildChanged(DataSnapshot dataSnapshot, String s) {

				}

				@Override
				public void onChildRemoved(DataSnapshot dataSnapshot) {

				}

				@Override
				public void onChildMoved(DataSnapshot dataSnapshot, String s) {

				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});

		}
	}


	@Override
	protected void onStart() {
		super.onStart();

		GlobalVariables.ACTIVITIES_COUNT.addAndGet(1);

		//    if(isGoingInBackground)
		{
			mFireBaseAuth.addAuthStateListener(mAuthStateListener);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();

		isGoingInBackground = true;
//        if(connectedRefListener != null)
//        {
//         //   connectedRef.addValueEventListener(connectedRefListener);
//        }
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onStop() {
		super.onStop();

		GlobalVariables.decrementActivities();
		// if(isGoingInBackground)
		{
			// if(lastSeenRef != null)
			{
				//    lastSeenRef.setValue(new Date().getTime());
			}
			if (connectedRefListener != null) {
				connectedRef.removeEventListener(connectedRefListener);
			}
			if (mAuthStateListener != null) {
				mFireBaseAuth.removeAuthStateListener(mAuthStateListener);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		myMenu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();


		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, Settings.class));
		} else if (id == R.id._camera) {
			permissionManager.checkPermissions(singleton(Manifest.permission.CAMERA), new PermissionManager.PermissionRequestListener() {
				@Override
				public void onPermissionGranted() {
					Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
					startActivity(intent);
				}

				@Override
				public void onPermissionDenied() {
					Toast.makeText(getApplicationContext(),
					               "Camera Permission ia" +
							               " required to open camera", Toast.LENGTH_SHORT).show();
				}
			});
		} else if (id == R.id.refresh_contacts) {
			GlobalFunctions.getStoreAndUploadContacts
					                (TabbedActivity.this, mUsersDatabaseReference,
					                 mFireBaseAuth.getCurrentUser().getPhoneNumber(), true);

		} else if (id == R.id.mainMenuSearchItem) {
			myMenu.setGroupVisible(R.id.mainMenuSearchItem, false);
//            myMenu.getItem(R.id.mainMenuSearchItem).setVisible(false);

			findViewById(R.id.mainSearchBar).setVisibility(View.VISIBLE);
			findViewById(R.id.tabs).setVisibility(View.GONE);

			((EditText) findViewById(R.id.searchText)).addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
					TabContactsFragment.contactAdapter.getFilter().filter(charSequence.toString());
				}

				@Override
				public void afterTextChanged(Editable s) {

				}
			});

			findViewById(R.id.searchClose).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					TabContactsFragment.contactAdapter.getFilter().filter("");
					findViewById(R.id.tabs).setVisibility(View.VISIBLE);
					findViewById(R.id.mainSearchBar).setVisibility(View.GONE);
				}
			});
		} else if (id == R.id.searchClose) {

		}

		return super.onOptionsItemSelected(item);
	}


	/**
	 * A placeholder fragment containing a simple view.
	 */

	public void sendNotification(ChatMessage message, DbContact contact) {

		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.mipmap.whatsapp1)
						.setContentTitle(contact.getUserName())
						.setContentText(message.getContent())
						.setAutoCancel(true)
						.setOnlyAlertOnce(true)
						.setSound(uri);

		Intent resultIntent = new Intent(this, ChatActivity.class);
		resultIntent.putExtra("contact", contact);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(TabbedActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent
				                                                 (0, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(resultPendingIntent);


		int mNotificationId = 001;
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr =
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		// Builds the notification and issues it.
		mNotifyMgr.notify(mNotificationId, notification);
	}

	private void saveToStorage(ChatMessage message, Bitmap loadedImage) {
		// get external storage
		Storage storage = GlobalVariables.getInstance(getApplicationContext()).getStorage();

		String path = GlobalVariables.getInstance(getApplicationContext())
				              .getStorage().getExternalStorageDirectory();

		// photos dir
		String photoPath = path + GlobalVariables.USER_RECEIVED_IMAGES_PATH;

		boolean dirExists = storage.isDirectoryExists(photoPath);

		if (!dirExists) {
			storage.createDirectory(photoPath);
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				                   .format(Calendar.getInstance().getTime());

		String fileName = "IMG - " + timeStamp;

		photoPath = photoPath + fileName + ".jpg";

		storage.createFile(photoPath, loadedImage);

		Uri photoUri = Uri.fromFile(new File(photoPath));

		message.setMediaPath(photoUri.toString());
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					TabChatsFragment tab1 = new TabChatsFragment();
//                    ((MenuItem)findViewById(R.id.mainMenuSearchItem)).setVisible(false);
					return tab1;
				case 1:
					TabContactsFragment tab2 = new TabContactsFragment();
//                    ((MenuItem)findViewById(R.id.mainMenuSearchItem)).setVisible(true);
					return tab2;
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "Chats";
				case 1:
					return "Contacts";
			}
			return null;
		}

	}

}
