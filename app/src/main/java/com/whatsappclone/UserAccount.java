package com.whatsappclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import Database.Chat;
import Database.ChatMessage;
import Database.ChatMessage_;
import Database.Chat_;
import Database.DbUser;
import HelperClasses.GlobalVariables;
import io.objectbox.Box;
import io.objectbox.query.Query;

public class UserAccount extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_account);

		Toolbar toolbar = findViewById(R.id.userAccountToolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		LinearLayout deleteAccount = findViewById(R.id.accountSettingDeleteAccount);
		/*Delete account*/

		deleteAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new AlertDialog.Builder(UserAccount.this)
						.setTitle("Confirm Deletion")
						.setMessage("Are you sure you want to delete you account?\n\nNOTE: This action cannot be undone!")
						.setIcon(R.drawable.alert_octagon)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {

								final DbUser currentUser = GlobalVariables.getInstance(UserAccount.this).getCurrentUser();

								FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getPhoneNumber()).setValue(null)
										.addOnSuccessListener(new OnSuccessListener<Void>() {
											@Override
											public void onSuccess(Void aVoid) {
												FirebaseDatabase.getInstance().getReference().child("AllUsers").child(currentUser.getPhoneNumber()).setValue(null);
												FirebaseStorage.getInstance().getReference().child(currentUser.getPhoneNumber()).delete();

												Box<Chat> chatBox = GlobalVariables.getInstance(UserAccount.this).getBoxStore().boxFor(Chat.class);
												Box<ChatMessage> messageBox = GlobalVariables.getInstance(UserAccount.this).getBoxStore().boxFor(ChatMessage.class);

												Query<Chat> chatQuery = chatBox.query().greater(Chat_.__ID_PROPERTY, -1).build();
												Query<ChatMessage> messageQuery = messageBox.query().greater(ChatMessage_.__ID_PROPERTY, -1).build();

												messageQuery.setParameter(ChatMessage_.__ID_PROPERTY, -1);
												chatQuery.setParameter(Chat_.__ID_PROPERTY, -1);

												Toast.makeText(UserAccount.this, "User Deleted Successfully", Toast.LENGTH_SHORT).show();
												FirebaseAuth.getInstance().signOut();

												Intent intent = new Intent(UserAccount.this, WelcomeScreen.class);
												intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
												startActivity(intent);
											}
										}).addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										Toast.makeText(UserAccount.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
									}
								});
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {

							}
						})
						.show();
			}
		});

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
