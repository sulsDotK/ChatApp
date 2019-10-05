package com.whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import HelperClasses.GlobalVariables;


public class WelcomeScreen extends AppCompatActivity {
	private static final String TAG = "WelcomeActivity";

	private void initDisplay() {
		setContentView(R.layout.activity_welcome_screen);

		Button continueButton = findViewById(R.id.welcomeScreenContinueButton);

		continueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(WelcomeScreen.this, SignUpActivityUserInfo.class));
			}
		});

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initDisplay();
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

	}

}

