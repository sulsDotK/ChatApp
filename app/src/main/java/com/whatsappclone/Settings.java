package com.whatsappclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import Database.DbUser;
import HelperClasses.GlobalVariables;

public class Settings extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Toolbar toolbar = findViewById(R.id.Settingstoolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		final LinearLayout profileLinearLayout = findViewById(R.id.profileLinearLayout);

		profileLinearLayout.setOnClickListener(new View.OnClickListener() {
			                                       @Override
			                                       public void onClick(View view) {
				                                       startActivity(new Intent(Settings.this, UserProfile.class));
			                                       }
		                                       }

		);

		final LinearLayout accountLinearLayout = findViewById(R.id.accountLinearLayout);

		accountLinearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(Settings.this, UserAccount.class));
			}
		});

		final LinearLayout contactsLinearLayout = findViewById(R.id.contactsLinearLayout);

		/** Idhar  hona ha invite friends wala kam **/

		contactsLinearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String name = GlobalVariables.getInstance(Settings.this).getCurrentUser().getUserName();

				Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
				intent.putExtra(Intent.EXTRA_TEXT, name + " invites you to use WhatsappClone!");
				intent.setType("text/plain");
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		DbUser currentUser = GlobalVariables.getInstance(this).getCurrentUser();

		ImageView userImage = findViewById(R.id.settingsProfileImage);
		TextView userName = findViewById(R.id.settingsUsername);
		TextView userStatus = findViewById(R.id.settingsUserStatus);

		if (currentUser.getProfilePhotoPath().equals("")) {
			userImage.setImageDrawable(ContextCompat.getDrawable(Settings.this, R.drawable.ic_person_black_36dp));
		} else {
			userImage.setImageURI(Uri.parse(currentUser.getProfilePhotoPath()));
		}
		userName.setText(currentUser.getUserName());
		userStatus.setText(currentUser.getStatus());
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
