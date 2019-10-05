package com.whatsappclone;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import HelperClasses.GlobalVariables;

public class AfterSignUpLoadingScreen extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_after_sign_up_loading_screen);

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
