package com.whatsappclone;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.ArrayList;

import Database.DbUser;
import HelperClasses.GlobalVariables;
import io.objectbox.Box;

public class UserProfile extends AppCompatActivity {

	private boolean isImageRemoved;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		isImageRemoved = false;

		Toolbar toolbar = findViewById(R.id.userProfileToolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		DbUser currentUser = GlobalVariables.getInstance(this).getCurrentUser();

		final Button saveButton = findViewById(R.id.userProfileSaveButton);
		final ImageView userImage = findViewById(R.id.userProfileImage);
		final EditText userName = findViewById(R.id.usernameTextView);
		final EditText userStatus = findViewById(R.id.statusTextView);
		TextView userNumber = findViewById(R.id.userContactTextView);

		ImageButton changeUsernameButton = findViewById(R.id.changeUsernameIcon);
		ImageButton changeStatusButton = findViewById(R.id.changeStatusIcon);

		userNumber.setText(currentUser.getPhoneNumber());

		changeUsernameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				userName.setFocusableInTouchMode(true);
			}
		});

		changeStatusButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				userStatus.setFocusableInTouchMode(true);
			}
		});

		userName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				saveButton.setVisibility(View.VISIBLE);
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		userStatus.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				saveButton.setVisibility(View.VISIBLE);
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		final PermissionManager permissionManager = PermissionManager.getInstance(getApplicationContext());
		final FloatingActionButton profilePhoto = findViewById(R.id.userProfileEditImageButton);

		profilePhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<String> p = new ArrayList<>();
				p.add(android.Manifest.permission.CAMERA);
				p.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

				permissionManager.checkPermissions(p, new PermissionManager.PermissionRequestListener() {
					@Override
					public void onPermissionGranted() {
						PickImageDialog.build(new PickSetup())
								.setOnPickResult(new IPickResult() {
									@Override
									public void onPickResult(PickResult r) {
										if (r.getError() == null) {
											((ImageView) findViewById(R.id.userProfileImage)).setImageBitmap(r.getBitmap());

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

											String filePath = SiliCompressor.with(UserProfile.this)
													                  .compress(r.getUri().toString(), new File(photoPath));

											Uri photoUri = Uri.fromFile(new File(filePath));

											vars.getCurrentUser().setProfilePhotoPath(photoUri.toString());

											saveButton.setVisibility(View.VISIBLE);
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

		findViewById(R.id.userProfileRemoveImageButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DbUser currentUser = GlobalVariables.getInstance(UserProfile.this).getCurrentUser();

				isImageRemoved = true;

				userImage.setImageDrawable(ContextCompat.getDrawable(UserProfile.this, R.drawable.ic_person_black_36dp));
				saveButton.setVisibility(View.VISIBLE);
			}
		});

		findViewById(R.id.userProfileSaveButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				updateUserInfo();
				Toast.makeText(UserProfile.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		DbUser currentUser = GlobalVariables.getInstance(this).getCurrentUser();
		ImageView userImage = findViewById(R.id.userProfileImage);
		EditText userName = findViewById(R.id.usernameTextView);
		EditText userStatus = findViewById(R.id.statusTextView);

		if (currentUser.getProfilePhotoPath().equals("")) {
			userImage.setImageDrawable(ContextCompat.getDrawable(UserProfile.this, R.drawable.ic_person_black_36dp));
		} else {
			userImage.setImageURI(Uri.parse(currentUser.getProfilePhotoPath()));
		}
		userName.setText(currentUser.getUserName());
		userStatus.setText(currentUser.getStatus());
		findViewById(R.id.userProfileSaveButton).setVisibility(View.INVISIBLE);
	}

	private void updateUserInfo() {
		final ProgressDialog progressDialog = new ProgressDialog(UserProfile.this);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.setMessage("Updating info ...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setProgress(0);
		progressDialog.setMax(100);
		progressDialog.show();

		final DbUser currentUser = GlobalVariables.getInstance(this).getCurrentUser();
		String username = ((TextView) findViewById(R.id.usernameTextView)).getText().toString();
		String status = ((TextView) findViewById(R.id.statusTextView)).getText().toString();
		final FirebaseDatabase mFireRef = FirebaseDatabase.getInstance();
		StorageReference mUserProfilePic = FirebaseStorage.getInstance().getReference().child(currentUser.getPhoneNumber()).child("profilePic");

		final Box<DbUser> userBox = GlobalVariables.getInstance(this).getBoxStore().boxFor(DbUser.class);

		if (!username.equals("")) {
			currentUser.setUserName(username);
		}

		if (!status.equals("")) {
			currentUser.setStatus(status);
		}

		if (isImageRemoved) {
			currentUser.setProfilePhotoPath("");
			currentUser.setProfilePhotoUrl("");
		}

		// get picture
		FloatingActionButton profilePhoto = findViewById(R.id.userProfileEditImageButton);
		if (!currentUser.getProfilePhotoPath().isEmpty()) {
			if (profilePhoto.getDrawable() != ContextCompat.getDrawable
					                                                (UserProfile.this, R.drawable.ic_person_black_36dp)) {
				Uri uri = Uri.parse(currentUser.getProfilePhotoPath());
				mUserProfilePic.putFile(uri).
						                            addOnSuccessListener(UserProfile.this,
						                                                 new OnSuccessListener<UploadTask.TaskSnapshot>() {
							                                                 @Override
							                                                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
								                                                 currentUser.setProfilePhotoUrl(taskSnapshot.getStorage().getDownloadUrl().toString());
								                                                 mFireRef.getReference().child("Users").child(currentUser.getPhoneNumber()).setValue(currentUser);

								                                                 // updating local DB
								                                                 userBox.put(currentUser);
								                                                 progressDialog.dismiss();
							                                                 }
						                                                 });
				return;
				// unchanged photo
			}
		}

		// Update firebase
		mFireRef.getReference().child("Users").child(currentUser.getPhoneNumber()).setValue(currentUser);

		// updating local DB
		userBox.put(currentUser);

		progressDialog.dismiss();

		(findViewById(R.id.usernameTextView)).setFocusable(false);
		(findViewById(R.id.statusTextView)).setFocusable(false);
		findViewById(R.id.userProfileSaveButton).setVisibility(View.GONE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			onBackPressed();
		}

		return super.onOptionsItemSelected(item);
	}
}
