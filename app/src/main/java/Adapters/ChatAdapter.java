package Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.whatsappclone.R;

import java.util.List;

import Database.Chat;
import Database.ChatMessage;
import Database.DbContact;
import Database.DbContact_;
import HelperClasses.GlobalFunctions;
import HelperClasses.GlobalVariables;
import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.Box;
import io.objectbox.query.Query;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
	Context context;
	View.OnClickListener mClickListener;
	private List<Chat> chats;

	public ChatAdapter(List<Chat> chatsList, Context ctx) {
		this.chats = chatsList;
		context = ctx;
	}

	public void updateList(List<Chat> list) {
		this.chats = list;
		notifyDataSetChanged();
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				                .inflate(R.layout.chat_view, parent, false);

		RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mClickListener.onClick(view);
			}
		});

		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		Chat chat = chats.get(position);

		Box<DbContact> contactsBox = GlobalVariables.getInstance(context).getBoxStore().boxFor(DbContact.class);

		Query<DbContact> query = contactsBox.query().equal(DbContact_.phoneNumber, "").build();

		DbContact res = query.setParameter(DbContact_.phoneNumber, chat.getWithWhom()).findFirst();

		holder.chatDisplayName.setText(res.getUserName());

		ChatMessage lastMessageObj = chat.getLastMessage();

		holder.chatLastMessage.setText(lastMessageObj.getContent());

		holder.chatLastMessageDate.setText(GlobalFunctions.getLastMessageTime
				                                                   (lastMessageObj.getTimeStamp()));

		if (res.getProfilePhotoPath().isEmpty()) {
			holder.chatDisplayImage.setImageResource(R.drawable.ic_person_black_36dp);
		} else {
			holder.chatDisplayImage.setImageURI(Uri.parse(res.getProfilePhotoPath()));
		}

		if (chat.unreadKeys.size() > 0) {
			holder.unreadCount.setText(String.valueOf(chat.unreadKeys.size()));
			holder.unreadCount.setVisibility(View.VISIBLE);
		} else {
			holder.unreadCount.setText("0");
			holder.unreadCount.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public int getItemCount() {
		return chats.size();
	}

	public void setClickListener(View.OnClickListener callback) {
		mClickListener = callback;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {
		public CircleImageView chatDisplayImage;
		public TextView chatDisplayName;
		public TextView chatLastMessageDate;
		public TextView chatLastMessage;
		public TextView unreadCount;

		View layout;

		public MyViewHolder(View view) {
			super(view);
			this.layout = view;

			chatDisplayImage = view.findViewById(R.id.chatDisplayImage);
			chatDisplayName = view.findViewById(R.id.chatDisplayName);
			chatLastMessageDate = view.findViewById(R.id.chatLastMessageDate);
			chatLastMessage = view.findViewById(R.id.chatDisplayLastMessage);
			unreadCount = view.findViewById(R.id.chatViewUnreadCount);
		}

		public View getLayout() {
			return layout;
		}
	}

//    public void reorder(Chat obj)
//    {
//        int index = chatRowViews.indexOf(obj);
//        chatRowViews.remove(obj);
//        chatRowViews.add(0, obj);
//    }

}
