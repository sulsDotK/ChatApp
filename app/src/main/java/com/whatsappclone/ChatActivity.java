package com.whatsappclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.intentfilter.androidpermissions.PermissionManager;
import com.kbeanie.multipicker.api.AudioPicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.AudioPickerCallback;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenAudio;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.kcode.bottomlib.BottomDialog;
import com.snatik.storage.Storage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Adapters.ChatMessageAdapter;
import Database.Chat;
import Database.ChatMessage;
import Database.Chat_;
import Database.DbContact;
import Database.DbContact_;
import Database.DbUser;
import Database.PushKey;
import Database.PushKey_;
import HelperClasses.FirebaseMessage;
import HelperClasses.GlobalFunctions;
import HelperClasses.GlobalVariables;
import HelperClasses.RecyclerItemClickListener;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import io.objectbox.Box;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.objectbox.reactive.DataSubscription;

public class ChatActivity extends AppCompatActivity {

	static final int VOICE_MESSAGE_REQ_CODE = 5;
	ChatMessageAdapter mMessageAdapter;
	Button sendButton;
	EditText messageEditText;
	FirebaseDatabase mFirebaseDatabase;
	DatabaseReference mMessagesReference;
	DatabaseReference mRecieverReference;
	DatabaseReference mMySentMessagesReference;
	FirebaseStorage mFirebaseStorage;
	StorageReference chatPhotosReference;
	StorageReference chatVideosReference;
	StorageReference chatDocsReference;
	StorageReference chatAudiosReference;
	StorageReference chatVoiceReference;
	DataSubscription newMessagesSubscription;
	DataSubscription subscription;
	Box<Chat> chatBox;
	Query<Chat> chatQuery;
	Box<ChatMessage> chatMessageBox;
	Query<ChatMessage> chatMessageQuery;
	Chat chatObject;
	List<ChatMessage> listOfMesages;
	List<ChatMessage> messagesList;
	List<ChatMessage> selectedMessagesList;
	RecyclerView messagesView;
	DbContact currentContact;
	Button buttonChatboxAttach;
	VideoPicker videoPicker;
	AudioPicker audioPicker;
	VideoPickerCallback videoPickerCallback;
	AudioPickerCallback audioPickerCallback;
	String voiceNoteFilePath;

	Toolbar toolbar;
	Menu myMenu;
	Box<PushKey> keysBox;
	List<PushKey> keys;
	private boolean isMultiSelect = false;
	private ActionMode mActionMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		keysBox = GlobalVariables.getInstance(getApplicationContext())
				          .getBoxStore().boxFor(PushKey.class);

		Query<PushKey> pushKeyQuery = keysBox.query().equal(PushKey_.byMe, true).build();

		mFirebaseDatabase = FirebaseDatabase.getInstance();
		mMessagesReference = mFirebaseDatabase.getReference().child("Messages");
		mFirebaseStorage = FirebaseStorage.getInstance();

		chatPhotosReference = mFirebaseStorage.getReference().child("Images");
		chatVideosReference = mFirebaseStorage.getReference().child("Videos");
		chatDocsReference = mFirebaseStorage.getReference().child("Documents");
		chatAudiosReference = mFirebaseStorage.getReference().child("Audios");
		chatVoiceReference = mFirebaseStorage.getReference().child("Voice Messages");

		sendButton = findViewById(R.id.buttonChatboxSend);
		buttonChatboxAttach = findViewById(R.id.buttonChatboxAttach);
		messageEditText = findViewById(R.id.editTextChatbox);
		messagesView = findViewById(R.id.recyclerViewMessageList);

		toolbar = findViewById(R.id.messageToolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		//mLayoutManager.setReverseLayout(true);
		mLayoutManager.setStackFromEnd(true);
		messagesView.setLayoutManager(mLayoutManager);
		messagesView.setItemAnimator(new DefaultItemAnimator());

		chatBox = GlobalVariables.getInstance(this).getBoxStore().boxFor(Chat.class);
		chatQuery = chatBox.query().equal(Chat_.withWhom, "").build();

		chatMessageBox = GlobalVariables.getInstance(this).getBoxStore().boxFor(ChatMessage.class);
		//chatMessageQuery = chatMessageBox.query().equal(ChatMessage_.chatToOneId, "").build();

		if (chatObject != null) {
			messagesList = chatObject.messages;
		} else {
			Intent intent = getIntent();
			currentContact = (DbContact) intent.getSerializableExtra("contact");

			if (currentContact != null) {
				chatObject = chatQuery.setParameter(Chat_.withWhom, currentContact.getPhoneNumber()).findFirst();

				{
					messagesList = new ArrayList<>();
				}
			} else {
				finish();
			}

			mMessageAdapter = new ChatMessageAdapter(messagesList, this);
			messagesView.setAdapter(mMessageAdapter);

			mRecieverReference = mMessagesReference.child(currentContact.getPhoneNumber());
			mMySentMessagesReference = mRecieverReference.child
					                                              (GlobalVariables.getInstance(getApplicationContext())
							                                               .getCurrentUser().getPhoneNumber());

			keys = pushKeyQuery.setParameter(PushKey_.byMe, true).find();

			//            for(int i = 0; i < keys.size(); i++)
			//            {
			//                mMySentMessagesReference.child(keys.get(i).getKey())
			//                        .addValueEventListener(new ValueEventListener() {
			//                            @Override
			//                            public void onDataChange(DataSnapshot dataSnapshot)
			//                            {
			//
			//                            }
			//
			//                            @Override
			//                            public void onCancelled(DatabaseError databaseError)
			//                            {
			//
			//                            }
			//                        });
			//            }

		}

		messagesView.addOnItemTouchListener(new RecyclerItemClickListener(this, messagesView,
		                                                                  new RecyclerItemClickListener.OnItemClickListener() {
			                                                                  @Override
			                                                                  public void onItemClick(View view, int position) {
				                                                                  if (isMultiSelect) {
//                        multi_select(position);
					                                                                  if (messagesList.get(position).isSelected()) {
						                                                                  selectedMessagesList.remove(messagesList.get(position));
					                                                                  } else {
						                                                                  selectedMessagesList.add(messagesList.get(position));
					                                                                  }
				                                                                  }
			                                                                  }


			                                                                  @Override
			                                                                  public void onItemLongClick(View view, int position) {
				                                                                  if (!isMultiSelect) {
					                                                                  isMultiSelect = true;

					                                                                  if (messagesList.get(position).isSelected()) {
						                                                                  selectedMessagesList.remove(messagesList.get(position));

						                                                                  if (selectedMessagesList.size() == 0) {
							                                                                  isMultiSelect = false;

							                                                                  getMenuInflater().inflate(R.menu.message_bar_menu, myMenu);
							                                                                  return;
						                                                                  }
					                                                                  } else {
						                                                                  selectedMessagesList.add(messagesList.get(position));
					                                                                  }

					                                                                  getMenuInflater().inflate(R.menu.contextual_message_menu, myMenu);
				                                                                  }
			                                                                  }
		                                                                  }));

		Query<Chat> query = GlobalVariables.getInstance(getApplicationContext())
				                    .getBoxStore().boxFor(Chat.class).query()
				                    .equal(Chat_.withWhom, currentContact.getPhoneNumber()).build();

		newMessagesSubscription = query.subscribe().on(AndroidScheduler.mainThread())
				                          .observer(new DataObserver<List<Chat>>() {
					                          @Override
					                          public void onData(List<Chat> data) {
						                          for (int i = 0; i < data.size(); i++) {
							                          Chat unreadChat = data.get(i);

							                          if (unreadChat.getWithWhom().equals(currentContact.getPhoneNumber())) {
								                          DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

								                          DbUser user = GlobalVariables.getInstance(getApplicationContext())
										                                        .getCurrentUser();

								                          List<PushKey> keys = unreadChat.unreadKeys;

								                          for (int j = 0; j < keys.size(); j++) {
									                          PushKey key = keys.get(j);

									                          reference.child("Messages").child(user.getPhoneNumber())
											                          .child(currentContact.getPhoneNumber()).child(key.getKey())
											                          .child("status").setValue("seen");

									                          unreadChat.unreadKeys.remove(j);

									                          GlobalVariables.getInstance(getApplicationContext()).getBoxStore()
											                          .boxFor(PushKey.class).remove(key);

									                          GlobalVariables.getInstance(getApplicationContext())
											                          .getBoxStore().boxFor(Chat.class).put(unreadChat);
								                          }
								                          //chatObject = unreadChat;
								                          if (chatObject.messages.size() != unreadChat.messages.size()) {
									                          chatObject.messages = unreadChat.messages;
									                          mMessageAdapter.messages = chatObject.messages;
									                          mMessageAdapter.notifyDataSetChanged();
									                          messagesView.scrollToPosition(mMessageAdapter.getItemCount() - 1);
								                          }
								                          return;
							                          }
						                          }
					                          }
				                          });

		Query<DbContact> queryTwo = GlobalVariables.getInstance(getApplicationContext())
				                            .getBoxStore().boxFor(DbContact.class).query()
				                            .equal(DbContact_.phoneNumber, currentContact.getPhoneNumber()).build();

		subscription = queryTwo.subscribe().on(AndroidScheduler.mainThread())
				               .observer(new DataObserver<List<DbContact>>() {
					               @Override
					               public void onData(List<DbContact> data) {
						               int index = data.indexOf(currentContact);
						               if (index != -1) {
							               DbContact c = data.get(index);

							               String lastSeen = GlobalFunctions.getLastSeen(c.getLastSeen());
							               ((TextView) findViewById(R.id.messageToolbarChatInfo)).setText(lastSeen);
						               }
					               }
				               });

		(findViewById(R.id.chatActivityChatInfo)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent1 = new Intent(ChatActivity.this, ChatInfoScreen.class);
				intent1.putExtra("contact", currentContact);
				startActivity(intent1);
			}
		});

		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (messageEditText.getText().toString().isEmpty()) {
					return;
				}
				sendMessage();
			}
		});

		buttonChatboxAttach.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BottomDialog dialog = BottomDialog.newInstance
						                                   ("Attachment", new String[]{"Photo", "Video", "Document", "Audio", "Voice Message"});

				//add item click listener
				dialog.setListener(new BottomDialog.OnClickListener() {
					@Override
					public void click(int position) {
						if (position == 0) {
							pickImage();
						} else if (position == 1) {
							pickVideo();
						} else if (position == 2) {
							pickFile();
						} else if (position == 3) {
							pickAudio();
						} else if (position == 4) {
							// voice message
							newVoiceMessage();
						}
					}
				});

				dialog.show(getSupportFragmentManager(), "dialog");
			}
		});
	}

	private void pickImage() {
		ArrayList<String> p = new ArrayList<>();
		p.add(android.Manifest.permission.CAMERA);
		p.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

		PermissionManager permissionManager = PermissionManager.getInstance(getApplicationContext());
		permissionManager.checkPermissions(p, new PermissionManager.PermissionRequestListener() {
			@Override
			public void onPermissionGranted() {
				FilePickerBuilder.getInstance().setMaxCount(1)
						.setActivityTheme(R.style.AppTheme)
						.pickPhoto(ChatActivity.this);

//                    PickImageDialog.build(new PickSetup())
//                            .setOnPickResult(new IPickResult() {
//                                @Override
//                                public void onPickResult(PickResult r)
//                                {
//                                    if (r.getError() == null) {
//                                        GlobalVariables vars = GlobalVariables.getInstance(getApplicationContext());
//
//                                        // get external storage
//                                        Storage storage = GlobalVariables.getInstance
//                                                (getApplicationContext()).getStorage();
//
//                                        String path = storage.getExternalStorageDirectory();
//
//                                        // photos dir
//                                        String photoPath = path + GlobalVariables.USER_SENT_IMAGES_PATH;
//
//                                        boolean dirExists = storage.isDirectoryExists(photoPath);
//
//                                        if (!dirExists) {
//                                            storage.createDirectory(photoPath);
//                                        }
//
//                                        String filePath = SiliCompressor.with(ChatActivity.this)
//                                                .compress(r.getUri().toString(), new File(photoPath));
//
//                                        Uri photoUri = Uri.fromFile(new File(filePath));
//
//                                        // add photo to message and upload
//                                        sendMediaMessage(photoUri);
//
//                                    } else {
//                                        r.getError().printStackTrace();
//                                    }
//                                }
//                            }).show(getSupportFragmentManager());
			}

			@Override
			public void onPermissionDenied() {
				Toast.makeText(getApplicationContext(),
				               "Camera and External Storage Permissions are" +
						               " required to select photo", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void pickFile() {
		FilePickerBuilder.getInstance().setMaxCount(1)
				.setActivityTheme(R.style.AppTheme)
				.pickFile(this);
	}

	private void pickVideo() {
		videoPicker = new VideoPicker(ChatActivity.this);

		videoPickerCallback = new VideoPickerCallback() {
			@Override
			public void onVideosChosen(List<ChosenVideo> list) {
				// handle video uploading
				ChosenVideo chosenVideo = list.get(0);
				Uri uri = Uri.fromFile(new File(chosenVideo.getOriginalPath()));

				String path = uri.toString();

//                Storage storage = GlobalVariables.getInstance(getApplicationContext()).getStorage();
//                storage.deleteDirectory(storage.getExternalStorageDirectory() + "/WhatsAppClone/WhatsAppClone Movies/");
//
//                GlobalVariables vars = GlobalVariables.getInstance(getApplicationContext());
//
//                String path = storage.getExternalStorageDirectory();
//
//                String videoPath = path + GlobalVariables.USER_SENT_VIDEOS_PATH;
//
//                boolean dirExists = storage.isDirectoryExists(videoPath);
//
//                if(!dirExists)
//                {
//                    storage.createDirectory(videoPath);
//                }
//
//                String filePath = null;
//                try {
//                    filePath = SiliCompressor.with(ChatActivity.this)
//                            .compressVideo(uri, videoPath);
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
//
//                if(filePath != null)
//                {
//                    Uri videoUri = Uri.fromFile(new File(filePath));
//                    filePath = videoUri.toString();
//                }


			}

			@Override
			public void onError(String message) {
				// Do error handling
			}
		};

		videoPicker.setVideoPickerCallback(videoPickerCallback);
		// videoPicker.allowMultiple(); // Default is false
		// videoPicker.shouldGenerateMetadata(false); // Default is true
		// videoPicker.shouldGeneratePreviewImages(false); // Default is true
		videoPicker.pickVideo();
	}

	private void pickAudio() {
		audioPicker = new AudioPicker(ChatActivity.this);
		//audioPicker.allowMultiple();

		audioPickerCallback = new AudioPickerCallback() {
			@Override
			public void onAudiosChosen(List<ChosenAudio> files) {
				// Send audio
				ChosenAudio chosenAudio = files.get(0);
				String previewImage = chosenAudio.getOriginalPath();
				String uri = chosenAudio.getQueryUri();

			}

			@Override
			public void onError(String message) {
				// Handle errors

			}
		};
		audioPicker.setAudioPickerCallback(audioPickerCallback);
		audioPicker.pickAudio();
	}

	private void newVoiceMessage() {
		ArrayList<String> permissions = new ArrayList<>();
		permissions.add(android.Manifest.permission.WAKE_LOCK);
		permissions.add(android.Manifest.permission.RECORD_AUDIO);
		permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		PermissionManager manager = PermissionManager.getInstance(getApplicationContext());
		manager.checkPermissions(permissions, new PermissionManager.PermissionRequestListener() {
			@Override
			public void onPermissionGranted() {
				// get external storage
				Storage storage = GlobalVariables.getInstance(getApplicationContext()).getStorage();

				String path = GlobalVariables.getInstance(getApplicationContext())
						              .getStorage().getExternalStorageDirectory();

				String Path = path + GlobalVariables.USER_SENT_VOICE_NOTES_PATH;

				boolean dirExists = storage.isDirectoryExists(Path);

				if (!dirExists) {
					storage.createDirectory(Path);
				}

				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
						                   .format(Calendar.getInstance().getTime());

				String filePath = Path + timeStamp + ".wav";

				voiceNoteFilePath = filePath;

				storage.createFile(filePath, "");

				int color = getResources().getColor(R.color.colorPrimaryDark);
				AndroidAudioRecorder.with(ChatActivity.this)
						// Required
						.setFilePath(filePath)
						.setColor(color)
						.setRequestCode(VOICE_MESSAGE_REQ_CODE)
						// Optional
						.setSource(AudioSource.MIC)
						.setChannel(AudioChannel.STEREO)
						.setSampleRate(AudioSampleRate.HZ_48000)
						.setAutoStart(true)
						.setKeepDisplayOn(true)
						// Start recording
						.record();
			}

			@Override
			public void onPermissionDenied() {
				Toast.makeText(getApplicationContext(),
				               "Permissions are needed to record voice message !", Toast.LENGTH_SHORT);
			}
		});
	}

	private void sendMessage() {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String timeStamp = df.format(new Date());

		//chatObject = chatQuery.setParameter(Chat_.withWhom, currentContact.getPhoneNumber()).findFirst();

		if (chatObject == null) {
			chatObject = new Chat(currentContact.getPhoneNumber(), 0);
			messagesList = chatObject.messages;
		}

		final ChatMessage dbMessage = new ChatMessage(chatObject, messageEditText.getText().toString()
				, "", "", timeStamp, "pending", true);

		FirebaseMessage firebaseMessage = new FirebaseMessage(messageEditText.getText().toString()
				, "", timeStamp, "sent", true);

		chatObject.messages.add(dbMessage);
		chatMessageBox.put(dbMessage);
		chatBox.put(chatObject);

		mMessageAdapter.notifyDataSetChanged();
		messagesView.scrollToPosition(mMessageAdapter.getItemCount() - 1);

		//messagesList.add(dbMessage);

		mMySentMessagesReference.push().setValue(firebaseMessage, new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

				onMessageSent(dbMessage, databaseReference);

			}
		});
		// Clear input box
		messageEditText.setText("");
	}

	private void onMessageSent(final ChatMessage dbMessage, final DatabaseReference databaseReference) {
		//chatObject = chatQuery.setParameter(Chat_.withWhom, currentContact.getPhoneNumber()).findFirst();

		// message delivered
		// update db
		//int index = chatObject.messages.indexOf(dbMessage);
		dbMessage.setStatus("sent");
		//chatObject.messages.set(index, dbMessage);
		chatMessageBox.put(dbMessage);
		//chatObject.messages.applyChangesToDb();
		chatBox.put(chatObject);
		//chatObject = chatQuery.setParameter(Chat_.withWhom, currentContact.getPhoneNumber()).findFirst();

		//change list message
		//int listIndex = messagesList.indexOf(dbMessage);
		//messagesList.set(listIndex, dbMessage);
		mMessageAdapter.notifyDataSetChanged();
		messagesView.scrollToPosition(mMessageAdapter.getItemCount() - 1);

		// attach listener to message reference
		databaseReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				FirebaseMessage newMessage = dataSnapshot.getValue(FirebaseMessage.class);

				if (newMessage != null) {
					if (!"sent".equals(newMessage.getStatus())) {
						// status has changed to delivered or seen
						onMessageStatusChange(dbMessage, databaseReference, newMessage.getStatus());
					}
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Log.e("message send ERROR", databaseError.toString());
			}
		});
	}

	private void onMessageStatusChange(final ChatMessage dbMessage,
	                                   DatabaseReference databaseReference, String newStatus) {
		//chatObject = chatQuery.setParameter(Chat_.withWhom, currentContact.getPhoneNumber()).findFirst();
		// update db
		//int index = chatObject.messages.indexOf(dbMessage);
		dbMessage.setStatus(newStatus);

		//chatObject.messages.set(index, dbMessage);
		chatMessageBox.put(dbMessage);
		chatBox.put(chatObject);
		//chatObject = chatQuery.setParameter(Chat_.withWhom, currentContact.getPhoneNumber()).findFirst();

		//change list message
		//int listIndex = messagesList.indexOf(dbMessage);
		//messagesList.set(listIndex, dbMessage);
		mMessageAdapter.notifyDataSetChanged();
		messagesView.scrollToPosition(mMessageAdapter.getItemCount() - 1);

		//                                       }
		//                                    });

		if (dbMessage.getStatus().equals("seen")) {
			databaseReference.setValue(null);
		}
	}

	private void sendMediaMessage(Uri mediaUri) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String timeStamp = df.format(new Date());

		if (chatObject == null) {
			chatObject = new Chat(currentContact.getPhoneNumber(), 0);
			messagesList = chatObject.messages;
		}

		final ChatMessage dbMessage = new ChatMessage(chatObject, messageEditText.getText().toString()
				, "", mediaUri.toString(), timeStamp, "pending", true);

		final FirebaseMessage firebaseMessage = new FirebaseMessage(messageEditText.getText().toString()
				, "", timeStamp, "sent", true);

		chatObject.messages.add(dbMessage);
		chatMessageBox.put(dbMessage);
		chatBox.put(chatObject);

		mMessageAdapter.notifyDataSetChanged();
		// modify adapter
		messagesView.scrollToPosition(mMessageAdapter.getItemCount() - 1);

		// Clear input box
		messageEditText.setText("");

		StorageReference reference = chatPhotosReference;

		if (mediaUri.toString().contains("Images")) {
			reference = chatPhotosReference;
		} else if (mediaUri.toString().contains("Movies")) {
			reference = chatVideosReference;
		}

		reference.putFile(mediaUri).
				                           addOnSuccessListener(ChatActivity.this,
				                                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
					                                                @Override
					                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
						                                                //firebaseMessage.setMediaUrl(taskSnapshot.getDownloadUrl().toString());
						                                                //dbMessage.setMediaUrl(taskSnapshot.getDownloadUrl().toString());
						                                                chatMessageBox.put(dbMessage);
						                                                mMessageAdapter.notifyDataSetChanged();
						                                                messagesView.scrollToPosition(mMessageAdapter.getItemCount() - 1);

						                                                mMySentMessagesReference.push().setValue(firebaseMessage, new DatabaseReference.CompletionListener() {
							                                                @Override
							                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
								                                                onMessageSent(dbMessage, databaseReference);
							                                                }
						                                                });
					                                                }
				                                                });
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO && data != null) {
				List<String> photoPaths = new ArrayList<>();

				photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));

				GlobalVariables vars = GlobalVariables.getInstance(getApplicationContext());

				// get external storage
				Storage storage = GlobalVariables.getInstance
						                                  (getApplicationContext()).getStorage();

				String path = storage.getExternalStorageDirectory();

				// photos dir
				String photoPath = path + GlobalVariables.USER_SENT_IMAGES_PATH;

				boolean dirExists = storage.isDirectoryExists(photoPath);

				if (!dirExists) {
					storage.createDirectory(photoPath);
				}

				String filePath = SiliCompressor.with(ChatActivity.this)
						                  .compress(Uri.parse(photoPaths.get(0)).toString(), new File(photoPath));

				Uri photoUri = Uri.fromFile(new File(filePath));

				// add photo to message and upload
				sendMediaMessage(photoUri);
			}

			if (requestCode == Picker.PICK_VIDEO_DEVICE) {
				if (videoPicker == null) {
					videoPicker = new VideoPicker(ChatActivity.this);
					videoPicker.setVideoPickerCallback(videoPickerCallback);
				}
				videoPicker.submit(data);
			} else if (requestCode == FilePickerConst.REQUEST_CODE_DOC) {
				if (data != null) {
					List<String> docPaths = new ArrayList<>();
					docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
				}
			} else if (requestCode == Picker.PICK_AUDIO) {
				if (audioPicker == null) {
					audioPicker = new AudioPicker(ChatActivity.this);
					audioPicker.setAudioPickerCallback(audioPickerCallback);
				}
				audioPicker.submit(data);
			} else if (requestCode == VOICE_MESSAGE_REQ_CODE) {
				if (resultCode == RESULT_CANCELED) {
					// Oops! User has canceled the recording
					Toast.makeText(getApplicationContext(), "Message discarded", Toast.LENGTH_SHORT).show();
					return;
				}
				// Great! User has recorded and saved the audio file
				// send message
				// voiceNoteFilePath
				String e = voiceNoteFilePath;
			}
		}
	}

	private void saveToStorage(DbContact contact, Bitmap loadedImage) {
		// get external storage
		Storage storage = GlobalVariables.getInstance(getApplicationContext()).getStorage();

		String path = GlobalVariables.getInstance(getApplicationContext())
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
	protected void onStart() {
		super.onStart();

		GlobalVariables.ACTIVITIES_COUNT.addAndGet(1);
	}

	@Override
	protected void onStop() {
		super.onStop();

		GlobalVariables.decrementActivities();
		if (!newMessagesSubscription.isCanceled()) {
			newMessagesSubscription.cancel();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!subscription.isCanceled()) {
			subscription.cancel();
		}
	}

	// Change start
	@Override
	protected void onResume() {
		super.onResume();

		ImageView chatImage = findViewById(R.id.messageToolbarChatImage);
		TextView chatName = findViewById(R.id.messageToolbarChatName);
		TextView chatInfo = findViewById(R.id.messageToolbarChatInfo);

		if (currentContact.getProfilePhotoPath().equals("")) {
			chatImage.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.ic_person_black_36dp));
		} else {
			chatImage.setImageURI(Uri.parse(currentContact.getProfilePhotoPath()));
		}

		chatName.setText(currentContact.getUserName());

		((TextView) findViewById(R.id.messageToolbarChatInfo)).setText(GlobalFunctions.getLastSeen(currentContact.getLastSeen()));

		LinearLayout backButton = findViewById(R.id.messageToolbarChatImageContainer);
		backButton.setDrawingCacheEnabled(true);
		backButton.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		backButton.layout(0, 0, backButton.getMeasuredWidth(), backButton.getMeasuredHeight());
		backButton.buildDrawingCache();

		getSupportActionBar().setHomeAsUpIndicator(new BitmapDrawable(getResources(), Bitmap.createBitmap(backButton.getDrawingCache())));
		backButton.setDrawingCacheEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	// Change end

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.message_bar_menu, menu);
		myMenu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			onBackPressed();
		} else if (id == R.id.messageBarMenuClearChat) {
			new AlertDialog.Builder(ChatActivity.this)
					.setTitle("Confirm Deletion")
					.setMessage("Are you sure you want to delete this chat?\n\nNOTE: This action cannot be undone!")
					.setIcon(R.drawable.alert_octagon)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {

							final DbUser currentUser = GlobalVariables.getInstance(ChatActivity.this).getCurrentUser();

							Box<Chat> chatBox = GlobalVariables.getInstance(ChatActivity.this).getBoxStore().boxFor(Chat.class);
							Box<ChatMessage> messageBox = GlobalVariables.getInstance(ChatActivity.this).getBoxStore().boxFor(ChatMessage.class);

							for (int j = 0, s = chatObject.messages.size(); i < s; i++) {
								messageBox.remove(chatObject.messages.get(j));
							}

							chatBox.remove(chatObject);

							mMessageAdapter.messages.clear();
							mMessageAdapter.notifyDataSetChanged();
							messagesView.scrollToPosition(mMessageAdapter.getItemCount() - 1);

							Toast.makeText(ChatActivity.this, "Chat Deleted Successfully", Toast.LENGTH_SHORT).show();
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {

						}
					})
					.show();
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
