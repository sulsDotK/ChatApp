package com.whatsappclone;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.intentfilter.androidpermissions.PermissionManager;
import com.snatik.storage.Storage;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import Database.DbContact;
import Database.DbUser;
import HelperClasses.GlobalFunctions;
import HelperClasses.GlobalVariables;
import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.Box;

import static java.util.Collections.singleton;

//import com.vansuita.pickimage.*;

public class SignUpActivityUserInfo extends AppCompatActivity {
	private static final int RC_SIGN_IN = 1;

	static CircleImageView profilePhoto;
	static boolean signUpSuccessful = false;
	EditText signUpUserNameEditText;
	PermissionManager permissionManager;
	Box<DbUser> userBox;
	Box<DbContact> contactsBox;
	private FirebaseDatabase mFirebaseDatabase;
	private FirebaseAuth mFireBaseAuth;
	private FirebaseStorage mFirebaseStorage;
	private DatabaseReference mUsersDatabaseReference;
	private StorageReference mUserStorageReference;
	private DatabaseReference mUserDatabaseReference;
	private StorageReference mUserProfilePic;

	private void addUserToLocalDb(DbUser currentUser) {
		currentUser.setId(0);
		userBox.put(currentUser);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up_user_info);

		// Database
		userBox = GlobalVariables.getInstance(getApplicationContext()).getBoxStore().boxFor(DbUser.class);
		contactsBox = GlobalVariables.getInstance(getApplicationContext()).getBoxStore().boxFor(DbContact.class);

		permissionManager = PermissionManager.getInstance(getApplicationContext());

		signUpUserNameEditText = findViewById(R.id.signUpUserNameEditText);

		profilePhoto = findViewById(R.id.signUpUserImage);

		profilePhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<String> p = new ArrayList<>();
				p.add(android.Manifest.permission.CAMERA);
				p.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

				permissionManager.checkPermissions(p, new PermissionManager.PermissionRequestListener() {
					@Override
					public void onPermissionGranted() {
						PickImageDialog.build(new PickSetup())
								.setOnPickResult(new IPickResult() {
									@Override
									public void onPickResult(PickResult r) {
										if (r.getError() == null) {
											profilePhoto.setImageBitmap(r.getBitmap());

											GlobalVariables vars = GlobalVariables.getInstance(getApplicationContext());

											// get external storage
											Storage storage = GlobalVariables.getInstance(getApplicationContext()).getStorage();

											String path = storage.getExternalStorageDirectory();

											// photos dir
											String photoPath = path + GlobalVariables.USER_PROFILE_PHOTOS_PATH;

											boolean dirExists = storage.isDirectoryExists(photoPath);

											if (!dirExists) {
												storage.createDirectory(photoPath);
											}

											String filePath = SiliCompressor.with(SignUpActivityUserInfo.this)
													                  .compress(r.getUri().toString(), new File(photoPath));

											Uri photoUri = Uri.fromFile(new File(filePath));

											vars.getCurrentUser().setProfilePhotoPath(photoUri.toString());
										} else {
											r.getError().printStackTrace();
										}
									}
								}).show(getSupportFragmentManager());
					}

					@Override
					public void onPermissionDenied() {
						Toast.makeText(getApplicationContext(),
						               "Camera and External Storage Permissions are" +
								               " required to select profile photo", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		Button signUpContinueButton = findViewById(R.id.signUpContinueButton);

		final ProgressDialog progressDialog = new ProgressDialog(SignUpActivityUserInfo.this);

		signUpContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = signUpUserNameEditText.getText().toString();
				final DbUser currentUser = GlobalVariables.getInstance(getApplicationContext()).getCurrentUser();
				currentUser.setUserName(username);
				final String n = mFireBaseAuth.getCurrentUser().getPhoneNumber();

				progressDialog.setIndeterminate(true);
				progressDialog.setCancelable(false);
				progressDialog.setMessage("Creating Account ...");
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setProgress(0);
				progressDialog.setMax(100);
				progressDialog.show();

                /*
                if(profilePhoto has photo)
                {
                    if(profilePhotoPath not empty)
                    {
                        uploadphoto
                            uploaduser
                            nextSActivity
                    }
                }
                uploaduser
                NextActivity
                 */
				if (profilePhoto.getDrawable() != ContextCompat.getDrawable
						                                                (SignUpActivityUserInfo.this, R.drawable.ic_person_black_36dp)) {
					if (!currentUser.getProfilePhotoPath().isEmpty()) {
						Uri uri = Uri.parse(currentUser.getProfilePhotoPath());
						mUserProfilePic.putFile(uri).
								                            addOnSuccessListener(SignUpActivityUserInfo.this,
								                                                 new OnSuccessListener<UploadTask.TaskSnapshot>() {
									                                                 @Override
									                                                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
										                                                 currentUser.setProfilePhotoUrl(taskSnapshot.getStorage().getDownloadUrl().toString());
										                                                 uploadUser(currentUser, n);
										                                                 progressDialog.dismiss();
										                                                 launchNextActivity();
									                                                 }
								                                                 });
						return;
					}
					// unchanged photo
				}
				// no photo selected
				uploadUser(currentUser, n);
				progressDialog.dismiss();
				launchNextActivity();
			}
		});

		// Initialise firebase components
		mFirebaseDatabase = FirebaseDatabase.getInstance();
		mFireBaseAuth = FirebaseAuth.getInstance();
		mFirebaseStorage = FirebaseStorage.getInstance();

		mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");

		AuthUI.IdpConfig phoneConfigWithDefaultNumber = new AuthUI.IdpConfig.PhoneBuilder()
				                                                .setDefaultCountryIso("pk")
				                                                .build();

		GlobalVariables.ACTIVITIES_COUNT.addAndGet(1);

//        Bundle params = new Bundle();
//        params.putString(AuthUI., "pk");

		startActivityForResult(
				AuthUI.getInstance()
						.createSignInIntentBuilder()
						.setIsSmartLockEnabled(false)
						.setAvailableProviders(
								Arrays.asList(
										new AuthUI.IdpConfig.PhoneBuilder()
												.setDefaultCountryIso("pk")
												.build()))
						.build(),
				RC_SIGN_IN);

	}

	private void uploadUser(DbUser currentUser, String number) {
		mUsersDatabaseReference.child(number).setValue(currentUser);
		addUserToLocalDb(currentUser);
	}

	private void launchNextActivity() {
		signUpSuccessful = true;
		Intent intent = new Intent(SignUpActivityUserInfo.this,
		                           TabbedActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		GlobalVariables.decrementActivities();
		if (requestCode == RC_SIGN_IN) {
			IdpResponse response = IdpResponse.fromResultIntent(data);

			if (resultCode == RESULT_OK) {
				syncUser(mFireBaseAuth.getCurrentUser());
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Sign in Cancelled", Toast.LENGTH_SHORT).show();
				finish();
			} else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
				Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
				return;
			} else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
				Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}

	private void syncUser(final FirebaseUser user) {
		final String n = user.getPhoneNumber();

		mUserDatabaseReference = mUsersDatabaseReference.child(n);
		mUserStorageReference = mFirebaseStorage.getReference().child(n);
		mUserProfilePic = mUserStorageReference.child("profilePic");

		mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					// User exists.
					GlobalVariables.getInstance(getApplicationContext())
							.setCurrentUser(dataSnapshot.getValue(DbUser.class));

					final DbUser currentUser = GlobalVariables.getInstance(getApplicationContext())
							                           .getCurrentUser();

					signUpUserNameEditText.setText(currentUser.getUserName());

					String downloadUrl = currentUser.getProfilePhotoUrl();

					if (!currentUser.getProfilePhotoPath().isEmpty()) {
						Uri uri = Uri.parse(currentUser.getProfilePhotoPath());
						try {
							InputStream stream = getContentResolver().openInputStream(uri);
							Drawable d = Drawable.createFromStream(stream, uri.toString());
							profilePhoto.setImageDrawable(d);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							Glide.with(SignUpActivityUserInfo.this)
									.load(currentUser.getProfilePhotoUrl()).into(profilePhoto);
						}
					} else if (!downloadUrl.isEmpty()) {
						Glide.with(SignUpActivityUserInfo.this)
								.load(currentUser.getProfilePhotoUrl()).into(profilePhoto);
					}

					ArrayList<String> permissions = new ArrayList<>();
					permissions.add(android.Manifest.permission.READ_CONTACTS);
					permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
					permissionManager.checkPermissions(permissions,
					                                   new PermissionManager.PermissionRequestListener() {
						                                   @Override
						                                   public void onPermissionGranted() {
							                                   GlobalFunctions.getStoreAndUploadContacts
									                                                   (SignUpActivityUserInfo.this, mUsersDatabaseReference, n, false);

							                                   currentUser.setToken(FirebaseInstanceId.getInstance().getToken());

							                                   mUsersDatabaseReference.child(n).setValue(currentUser);
						                                   }

						                                   @Override
						                                   public void onPermissionDenied() {
							                                   Toast.makeText(getApplicationContext(), "Contacts & Storage Permissions are needed for WhatsApp to function !",
							                                                  Toast.LENGTH_SHORT).show();
							                                   finishAffinity();
						                                   }
					                                   });

				} else {
					// add new user
					permissionManager.checkPermissions(singleton(android.Manifest.permission.READ_CONTACTS),
					                                   new PermissionManager.PermissionRequestListener() {
						                                   @Override
						                                   public void onPermissionGranted() {
							                                   GlobalFunctions.getStoreAndUploadContacts
									                                                   (SignUpActivityUserInfo.this, mUsersDatabaseReference, n, false);

							                                   DbUser currentUser = GlobalVariables.getInstance
									                                                                        (getApplicationContext()).getCurrentUser();

							                                   currentUser.setPhoneNumber(n);
							                                   currentUser.setStatus("Hi there ! I'm using whatsApp");
							                                   currentUser.setToken(FirebaseInstanceId.getInstance().getToken());

							                                   mUsersDatabaseReference.child(n).setValue(currentUser);
							                                   mUsersDatabaseReference.getRoot().child("AllUsers").child(n).setValue(true);
						                                   }

						                                   @Override
						                                   public void onPermissionDenied() {
							                                   Toast.makeText(getApplicationContext(), "Contacts Permission is needed for WhatsApp to function !",
							                                                  Toast.LENGTH_SHORT).show();
							                                   finishAffinity();
						                                   }
					                                   });
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Log.e("Database error", databaseError.getMessage());
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		GlobalVariables.ACTIVITIES_COUNT.addAndGet(1);
	}

	@Override
	protected void onStop() {
		super.onStop();

		GlobalVariables.decrementActivities();

		if (!signUpSuccessful) {
			mFireBaseAuth.signOut();
		}

	}
}
