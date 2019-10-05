package com.whatsappclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.snatik.storage.Storage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import Adapters.ContactAdapter;
import Database.DbContact;
import Database.DbContact_;
import Database.DbUser;
import HelperClasses.GlobalVariables;
import io.objectbox.Box;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.objectbox.reactive.DataSubscription;

public class TabContactsFragment extends Fragment {
	public static ContactAdapter contactAdapter;
	DataSubscription subscription;
	private FirebaseDatabase mFirebaseDatabase;
	private ValueEventListener mValueEventListener;
	private DatabaseReference mUsersDatabaseReference;
	private DatabaseReference mUserDatabaseReference;

	@Override
	public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
		final List<DbContact> contacts = GlobalVariables.getInstance(getContext())
				                                 .getUserContacts();

		Query<DbContact> query = GlobalVariables.getInstance(getContext()).getBoxStore()
				                         .boxFor(DbContact.class).query().greater(DbContact_.id, -1).build();

		subscription = query.subscribe().on(AndroidScheduler.mainThread()).observer(new DataObserver<List<DbContact>>() {
			@Override
			public void onData(List<DbContact> data) {
				if (contactAdapter != null) {
					contactAdapter.updateAdapter(data);
				}
			}
		});

		mFirebaseDatabase = FirebaseDatabase.getInstance();
		mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");

		for (int i = 0; i < contacts.size(); i++) {
			DatabaseReference userContactReference = mUsersDatabaseReference.child(contacts.get(i)
					                                                                       .getPhoneNumber());

			attachUserDatabaseReadListener(contacts.get(i), userContactReference);
		}

		contactAdapter = new ContactAdapter(getActivity(), R.layout.contact_view, contacts);

		View mContactView = inflator.inflate(R.layout.fragment_contacts, container, false);
		ListView contactView = mContactView.findViewById(R.id._contacts);
		contactView.setAdapter(contactAdapter);

		FloatingActionButton fab = mContactView.findViewById(R.id._fab1);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
				intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
				startActivity(intent);
			}
		});

		contactView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				DbContact currentContact = (DbContact) adapterView.getItemAtPosition(position);

				Intent intent = new Intent(getActivity(), ChatInfoScreen.class);
				intent.putExtra("contact", currentContact);
				startActivity(intent);
			}
		});

		return mContactView;
	}

	private void attachUserDatabaseReadListener(final DbContact contactInList, final DatabaseReference mUserDbReference) {
		if (mUserDbReference != null) {
			final Box<DbContact> contactsBox = GlobalVariables.getInstance(getContext())
					                                   .getBoxStore().boxFor(DbContact.class);

			mValueEventListener = new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					//User Contact on firebase
					DbUser contactOnFirebase = dataSnapshot.getValue(DbUser.class);

					if (contactOnFirebase == null) {
						if (mUserDbReference != null) {
							if (mValueEventListener != null) {
								mUserDbReference.removeEventListener(mValueEventListener);
								contactInList.setDeleted(true);
								contactsBox.put(contactInList);
								return;
							}
						}
					}

					contactInList.setStatus(contactOnFirebase.getStatus());
					contactInList.setLastSeen(contactOnFirebase.getLastSeen());
					contactInList.setStatusDate(contactOnFirebase.getStatusDate());

					contactsBox.put(contactInList);

					if (!contactInList.getProfilePhotoUrl().equals(contactOnFirebase.getProfilePhotoUrl())) {
						contactInList.setProfilePhotoUrl(contactOnFirebase.getProfilePhotoUrl());

						if (!contactInList.getProfilePhotoUrl().isEmpty()) {
							ImageLoader imageLoader = ImageLoader.getInstance();
							String imageUri = contactInList.getProfilePhotoUrl();
							imageLoader.loadImage(imageUri, new SimpleImageLoadingListener() {
								@Override
								public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
									saveToStorage(contactInList, loadedImage);
									contactsBox.put(contactInList);
								}
							});
						} else {
							// profile pic empty, remove local reference
							contactInList.setProfilePhotoPath("");
							// delete local photo (optional)
						}
						contactsBox.put(contactInList);
					}
					Log.d("Contact data : ", dataSnapshot.toString());
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					Log.d("Error getting contact :", databaseError.getMessage());
					// remove contact from user list
					contactsBox.remove(contactInList);
					GlobalVariables.getInstance(getActivity()).getUserContacts().remove(contactInList);
				}
			};

			mUserDbReference.addValueEventListener(mValueEventListener);
		}
	}

	private void saveToStorage(DbContact contact, Bitmap loadedImage) {
		// get external storage
		Storage storage = GlobalVariables.getInstance(getContext()).getStorage();

		String path = GlobalVariables.getInstance(getContext())
				              .getStorage().getExternalStorageDirectory();

		// photos dir
		String photoPath = path + GlobalVariables.PROFILE_PHOTOS_PATH;

		boolean dirExists = storage.isDirectoryExists(photoPath);

		if (!dirExists) {
			storage.createDirectory(photoPath);
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				                   .format(Calendar.getInstance().getTime());

		String fileName = contact.getUserName() + " " + timeStamp;

		photoPath = photoPath + fileName + ".jpg";

		storage.createFile(photoPath, loadedImage);

		Uri photoUri = Uri.fromFile(new File(photoPath));

		contact.setProfilePhotoPath(photoUri.toString());
	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		if (!subscription.isCanceled()) {
			subscription.cancel();
		}
		//detachDatabaseListener();
	}

	public void searchStart() {
		getActivity().closeContextMenu();
		getActivity().findViewById(R.id.mainSearchBar).setVisibility(View.VISIBLE);
		getActivity().findViewById(R.id.tabs).setVisibility(View.GONE);

		((EditText) getActivity().findViewById(R.id.searchText)).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				contactAdapter.getFilter().filter(charSequence.toString());
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
	}

	public void searchEnd() {
		getActivity().findViewById(R.id.tabs).setVisibility(View.VISIBLE);
		getActivity().findViewById(R.id.mainSearchBar).setVisibility(View.GONE);
		getActivity().invalidateOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.mainMenuSearchItem) {
			getActivity().closeContextMenu();
			getActivity().findViewById(R.id.mainSearchBar).setVisibility(View.VISIBLE);
			getActivity().findViewById(R.id.tabs).setVisibility(View.GONE);

			((EditText) getActivity().findViewById(R.id.searchText)).addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
					contactAdapter.getFilter().filter(charSequence.toString());
				}

				@Override
				public void afterTextChanged(Editable editable) {

				}
			});
		} else if (id == R.id.searchClose) {
			getActivity().findViewById(R.id.tabs).setVisibility(View.VISIBLE);
			getActivity().findViewById(R.id.mainSearchBar).setVisibility(View.GONE);
			getActivity().invalidateOptionsMenu();
		}

		return super.onOptionsItemSelected(item);
	}
}
