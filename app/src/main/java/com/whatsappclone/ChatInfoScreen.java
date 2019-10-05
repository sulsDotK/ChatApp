package com.whatsappclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

import java.util.List;

import Database.DbContact;
import Database.DbContact_;
import HelperClasses.GlobalFunctions;
import HelperClasses.GlobalVariables;
import HelperClasses.HeaderView;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.objectbox.reactive.DataSubscription;

public class ChatInfoScreen extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
	DataSubscription subscription;
	/* @BindView(R.id.toolbarHeaderView)*/ HeaderView toolbarHeaderView;
	/* @BindView(R.id.floatHeaderView)*/ HeaderView floatHeaderView;
	/* @BindView(R.id.chatInfoAppBar)*/ AppBarLayout appBarLayout;
	/*@BindView(R.id.chatInfoToolBar)*/ Toolbar toolbar;
	/*@BindView(R.id.chatInfoCardStatus)*/ TextView userStatus;
	/*@BindView(R.id.chatInfoCardContactNumber)*/ TextView userNumber;
	TextView headerChatInfoChatName;
	TextView headerChatInfoLastSeen;
	TextView toolbarChatInfoChatName;
	TextView toolbarChatInfoLastSeen;
	private boolean isHideToolbarView = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_info_screen);

		toolbarHeaderView = findViewById(R.id.toolbarHeaderView);
		floatHeaderView = findViewById(R.id.floatHeaderView);
		appBarLayout = findViewById(R.id.chatInfoAppBar);
		toolbar = findViewById(R.id.chatInfoToolBar);

		toolbarHeaderView.setVariables();
		floatHeaderView.setVariables();

		userStatus = findViewById(R.id.chatInfoCardStatus);
		userNumber = findViewById(R.id.chatInfoCardContactNumber);

		headerChatInfoChatName = floatHeaderView.findViewById(R.id.chatInfoChatName);

		headerChatInfoLastSeen = floatHeaderView.findViewById(R.id.chatInfoLastSeen);

		toolbarChatInfoChatName = toolbarHeaderView.findViewById(R.id.chatInfoChatName);

		toolbarChatInfoLastSeen = toolbarHeaderView.findViewById(R.id.chatInfoLastSeen);

		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();

		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		initUI();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void initUI() {
		appBarLayout.addOnOffsetChangedListener(this);

		// set the views to the information of the current user!
		Intent intent = getIntent();
		final DbContact currentContact = (DbContact) intent.getSerializableExtra("contact");

		headerChatInfoChatName.setText(currentContact.getUserName());
		headerChatInfoLastSeen.setText(currentContact.getLastSeenString());

		toolbarChatInfoChatName.setText(currentContact.getUserName());
		toolbarChatInfoLastSeen.setText(currentContact.getLastSeenString());

		ImageView chatImage = findViewById(R.id.chatInfoChatImage);

		if (!currentContact.getProfilePhotoPath().equals("")) {
			chatImage.setImageURI(Uri.parse(currentContact.getProfilePhotoPath()));
		} else {
			chatImage.setImageResource(R.drawable.ic_person_black_36dp);
		}

		userStatus.setText(currentContact.getStatus());
		userNumber.setText(currentContact.getPhoneNumber());

		Query<DbContact> query = GlobalVariables.getInstance(getApplicationContext())
				                         .getBoxStore().boxFor(DbContact.class).query()
				                         .equal(DbContact_.phoneNumber, currentContact.getPhoneNumber()).build();

		subscription = query.subscribe().on(AndroidScheduler.mainThread())
				               .observer(new DataObserver<List<DbContact>>() {
					               @Override
					               public void onData(List<DbContact> data) {
						               int index = data.indexOf(currentContact);
						               if (index != -1) {
							               DbContact c = data.get(index);

							               String lastSeen = GlobalFunctions.getLastSeen(c.getLastSeen());
//                            DateFormat onlyDateFormat = new SimpleDateFormat("dd/MM/yyyy");
//                            DateFormat onlyTimeFormat = new SimpleDateFormat("HH:mm");
//
//                            String lastSeen = "";
//
//                            if(c.getLastSeen() == 0)
//                            {
//                                lastSeen = c.getLastSeenString();
//                            }
//                            else
//                            {
//                                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//
//                                try
//                                {
//                                    Date lastSeenDate = format.parse(c.getLastSeenString());
//
//                                    if(DateUtils.isToday(c.getLastSeen()))
//                                    {
//                                        lastSeen = "last seen today at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(lastSeenDate);
//                                    }
//                                    else if(DateUtils.isToday(lastSeenDate.getTime() + DateUtils.DAY_IN_MILLIS))
//                                    {
//                                        lastSeen = "last seen yesterday at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(lastSeenDate);
//                                    }
//                                    else
//                                    {
//                                        lastSeen = "last seen on " + DateFormat.getInstance().format(lastSeenDate);
//                                    }
//                                }
//                                catch (ParseException e)
//                                {
//                                    e.printStackTrace();
//                                }
//                            }

							               headerChatInfoChatName.setText(c.getUserName());
							               headerChatInfoLastSeen.setText(lastSeen);

							               toolbarChatInfoChatName.setText(c.getUserName());
							               toolbarChatInfoLastSeen.setText(lastSeen);
						               }
					               }
				               });
	}

	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
		int maxScroll = appBarLayout.getTotalScrollRange();
		float percentage = (float) Math.abs(offset) / (float) maxScroll;

		if (percentage == 1f && isHideToolbarView) {
			toolbarHeaderView.setVisibility(View.VISIBLE);
			isHideToolbarView = !isHideToolbarView;

		} else if (percentage < 1f && !isHideToolbarView) {
			toolbarHeaderView.setVisibility(View.GONE);
			isHideToolbarView = !isHideToolbarView;
		}
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

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (!subscription.isCanceled()) {
			subscription.cancel();
		}
	}
}

