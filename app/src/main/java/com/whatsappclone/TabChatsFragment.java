package com.whatsappclone;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import Adapters.ChatAdapter;
import Database.Chat;
import Database.Chat_;
import Database.DbContact;
import Database.DbContact_;
import HelperClasses.GlobalVariables;
import io.objectbox.Box;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.objectbox.reactive.DataSubscription;

import static com.whatsappclone.TabbedActivity.badgedTabLayout;

/**
 * Created by Saif Ullah on 10/18/2017.
 */

public class TabChatsFragment extends Fragment {

	ChatAdapter chatAdapter;

	DataSubscription subscription;
	Box<Chat> chatBox;
	Query<Chat> query;
	GlobalVariables variables;

	RecyclerView chatsView;
	List<Chat> listOfChats;

	@Override
	public View onCreateView(LayoutInflater inflator, final ViewGroup container,
	                         Bundle savedInstanceState) {
		variables = GlobalVariables.getInstance(getActivity());

		View mChatView = inflator.inflate(R.layout.fragment_chats, container, false);

		chatsView = mChatView.findViewById(R.id._chats);

		FloatingActionButton fab = mChatView.findViewById(R.id._fab2);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), ChatSelectContactActivity.class);
				startActivity(intent);
			}
		});

		chatBox = variables.getBoxStore().boxFor(Chat.class);
		query = chatBox.query().order(Chat_.position).build();

		listOfChats = variables.getBoxStore().boxFor(Chat.class).getAll();

		subscription = query.subscribe().on(AndroidScheduler.mainThread())
				               .observer(new DataObserver<List<Chat>>() {
					               @Override
					               public void onData(List<Chat> data) {
						               listOfChats = data;
						               if (chatAdapter != null) {
							               chatAdapter.updateList(data);
							               chatAdapter.notifyDataSetChanged();
						               }
						               if (badgedTabLayout != null) {
							               int count = 0;

							               for (int i = 0; i < data.size(); i++) {
								               if (data.get(i).unreadKeys.size() > 0) {
									               count++;
								               }
							               }
							               if (count > 0) {
								               //first parameter is the tab index, at which badge should appear
								               badgedTabLayout.setBadgeText(0, String.valueOf(count));
							               } else {
								               //if you want to hide a badge pass null as a second parameter
								               badgedTabLayout.setBadgeText(0, null);
							               }
						               }
					               }
				               });

		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
		chatsView.setLayoutManager(mLayoutManager);
		chatsView.setItemAnimator(new DefaultItemAnimator());

		chatAdapter = new ChatAdapter(listOfChats, getContext());
		chatsView.setAdapter(chatAdapter);

		chatAdapter.setClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = chatsView.indexOfChild(v);

				Query<DbContact> contactQuery = variables.getBoxStore()
						                                .boxFor(DbContact.class).query().equal(DbContact_.phoneNumber, "").build();

				contactQuery.setParameter(DbContact_.phoneNumber, listOfChats.get(pos).getWithWhom());

				DbContact contact = contactQuery.findFirst();

				Intent intent = new Intent(getActivity(), ChatActivity.class);
				intent.putExtra("contact", contact);
				startActivity(intent);
			}
		});

		return mChatView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		subscription.cancel();
	}
}
