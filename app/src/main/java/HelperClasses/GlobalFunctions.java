package HelperClasses;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.PhoneNumber;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Database.DbContact;
import Database.DbContact_;
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * Created by Suleman Khalid on 11/27/2017.
 */

public class GlobalFunctions {
	public static void addContactToLocalDb(DbContact contact, Context context) {
		GlobalVariables.getInstance(context).getBoxStore().boxFor(DbContact.class).put(contact);
	}

	public static void initContactsList(Context context) {
		Contacts.initialize(context);

		List<Contact> allContacts = Contacts.getQuery().find();

		List<DbContact> userContacts = GlobalVariables
				                               .getInstance(context).getUserContacts();

		userContacts.clear();

		for (int i = 0, size = allContacts.size(); i < size; i++) {
			if (allContacts.get(i).getDisplayName() != null) {
				List<PhoneNumber> phoneNumbers = allContacts.get(i).getPhoneNumbers();

				String path;
				try {
					Uri uri = Uri.parse(allContacts.get(i).getPhotoUri());
					path = uri.toString();
				} catch (Exception e) {
					e.printStackTrace();
					path = "";
				}

				for (int j = 0; j < phoneNumbers.size(); j++) {
					String normalizedNumber = phoneNumbers.get(j).getNormalizedNumber();
					boolean hasNumber = false;

					if (normalizedNumber != null) {
						hasNumber = true;
					} else if (phoneNumbers.get(j).getNumber() != null) {
						normalizedNumber = phoneNumbers.get(j).getNumber()
								                   .replaceAll("\\s+", "");
						hasNumber = true;
					} else {
						Log.e("Nocontactnumber:", allContacts.get(i).getDisplayName());
					}

					if (hasNumber) {
						String name = allContacts.get(i).getDisplayName();
						String textStatus = "Hi there ! I'm using WhatsApp";

						DbContact newUserContact = new DbContact();
						newUserContact.setPhoneNumber(normalizedNumber);
						newUserContact.setUserName(name);
						newUserContact.setStatus(textStatus);
						newUserContact.setProfilePhotoPath(path);

						if (!userContacts.contains(newUserContact)) {
							userContacts.add(newUserContact);
						}
					}
				}
			} else {
				Log.e("Nocontactnameatindex:", String.valueOf(i));
			}
		}
	}

	public static void getStoreAndUploadContacts(final Context context, DatabaseReference mUsersDatabaseReference,
	                                             String number, final boolean showToast) {

		initContactsList(context);

		final List<DbContact> contacts = GlobalVariables.getInstance(context).getUserContacts();

		final int numberOfContacts = contacts.size();

		final Box<DbContact> contactBox = GlobalVariables.getInstance(context)
				                                  .getBoxStore().boxFor(DbContact.class);

		final Query<DbContact> query = contactBox.query().equal(DbContact_.phoneNumber, "").build();

		final AtomicInteger current = new AtomicInteger(0);

		for (int i = 0; i < contacts.size(); i++) {
			// upload to firebase and check
			final DbContact contact = contacts.get(i);

			if (contact.getPhoneNumber() != null) {
				mUsersDatabaseReference.getRoot().child("UserContacts").child(number).push().setValue(contact
						                                                                                      .getPhoneNumber(), new DatabaseReference.CompletionListener() {
					@Override
					public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
						current.addAndGet(1);
						if (current.get() == numberOfContacts) {
							if (showToast && !((Activity) context).isFinishing()) {
								Toast.makeText(context, "Contacts Refreshed" + current.get(), Toast.LENGTH_SHORT).show();
							}
						}

						//find contact in db with this number
						DbContact exists = query.setParameter(DbContact_.phoneNumber,
						                                      contact.getPhoneNumber()).findFirst();

						if (databaseError == null) {
							// if contact does not exist in database, new contact in phone and exists on firebase
							if (exists == null) {
								addContactToLocalDb(contact, context);
							}
							databaseReference.setValue(null);
						} else {
							//contact doesn't exist on firebase
							contacts.remove(contact);
							if (exists != null) {
								contactBox.remove(exists);
							}
							Log.d("Contacts removed : ", contact.getPhoneNumber());
						}
					}
				});
			} else {
				if (showToast && !((Activity) context).isFinishing()) {
					Toast.makeText(context, "No phone number" + contact.getUserName(),
					               Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public static String getLastSeen(long time) {
		String lastSeen = "";

		if (time == 0) {
			lastSeen = "Online";
		} else {
			// DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date lastSeenDate = new Date(time);

			if (DateUtils.isToday(time)) {
				lastSeen = "last seen today at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(lastSeenDate);
			} else if (DateUtils.isToday(lastSeenDate.getTime() + DateUtils.DAY_IN_MILLIS)) {
				lastSeen = "last seen yesterday at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(lastSeenDate);
			} else {
				lastSeen = "last seen on " + DateFormat.getInstance().format(lastSeenDate);
			}
		}
		return lastSeen;
	}

	public static String getLastMessageTime(String timeStamp) {
		String lastMessageTime = "";

		DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		try {
			Date date = format.parse(timeStamp);

			long time = date.getTime();

			Date lastSeenDate = new Date(time);

			if (DateUtils.isToday(time)) {
				lastMessageTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(lastSeenDate);
			} else if (DateUtils.isToday(lastSeenDate.getTime() + DateUtils.DAY_IN_MILLIS)) {
				lastMessageTime = "Yesterday";
			} else {
				lastMessageTime = DateFormat.getInstance().format(lastSeenDate);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return lastMessageTime;
	}

	public static String getHourMin(String timeStamp) {
		String messageTime = "";

		DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		try {
			Date msgDate = format.parse(timeStamp);
			messageTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(msgDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return messageTime;
	}

}
