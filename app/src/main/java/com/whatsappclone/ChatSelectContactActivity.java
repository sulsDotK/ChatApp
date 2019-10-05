package com.whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import Adapters.ContactAdapter;
import Database.DbContact;
import HelperClasses.GlobalVariables;

public class ChatSelectContactActivity extends AppCompatActivity {

	public ContactAdapter contactAdapter;
	ListView contactsListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_select_contact);

		Toolbar toolbar = findViewById(R.id.toolbar3);
		setSupportActionBar(toolbar);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle("Select Contact");

		final List<DbContact> contacts = GlobalVariables.getInstance(getApplicationContext())
				                                 .getUserContacts();

		contactsListView = findViewById(R.id.chatSelectContactListView);

		contactAdapter = new ContactAdapter(ChatSelectContactActivity.this,
		                                    R.layout.contact_view, contacts);

		contactsListView.setAdapter(contactAdapter);

		contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				DbContact currentContact = (DbContact) adapterView.getItemAtPosition(position);

				TabbedActivity.isGoingInBackground = false;
				Intent intent = new Intent(ChatSelectContactActivity.this, ChatActivity.class);
				intent.putExtra("contact", currentContact);
				startActivity(intent);
				finish();
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

	}

}
