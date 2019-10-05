package Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.whatsappclone.R;

import java.util.List;

import Database.ChatMessage;
import HelperClasses.GlobalFunctions;

/**
 * Created by Saad on 12/3/17.
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageHolder> {
	public List<ChatMessage> messages;
	Context context;
	View.OnClickListener mClickListener;

	public ChatMessageAdapter(List<ChatMessage> msgsList, Context ctx) {
		this.messages = msgsList;
		context = ctx;
	}

	public void add(ChatMessage msg) {
		messages.add(msg);
	}


//    public void update(List<ChatMessage> list)
//    {
//        messages = list;
//        notifyDataSetChanged();
//    }

	@Override
	public ChatMessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final View itemView = LayoutInflater.from(parent.getContext())
				                      .inflate(R.layout.send_message_bubble, parent, false);

		itemView.setTag("unselected");
		RecyclerView.ViewHolder holder = new ChatMessageAdapter.ChatMessageHolder(itemView);

//        holder.itemView.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                mClickListener.onClick(view);
//                if(itemView.getTag().equals("unselected"))
//                {
//
//                }
//            }
//        });

		return new ChatMessageHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final ChatMessageHolder holder, int position) {
		final ChatMessage message = messages.get(position);

		if (!message.getMediaPath().equals("")) {
			holder.messageImage.setVisibility(View.VISIBLE);
			if (message.getMediaUrl().equals("")) {
				holder.mediaLoadingProgressBar.setIndeterminate(true);
				holder.mediaLoadingProgressBar.setVisibility(View.VISIBLE);
			} else {
				holder.mediaLoadingProgressBar.setVisibility(View.GONE);
				holder.messageImage.setImageURI(Uri.parse(message.getMediaPath()));
			}
		} else {
			holder.mediaLoadingProgressBar.setVisibility(View.GONE);
		}

		holder.messageContent.setText(message.getContent());

		String time = message.getTimeStamp();
		holder.messageTime.setText(GlobalFunctions.getHourMin(time));

		LinearLayout layout = (LinearLayout) holder.getLayout();
		LinearLayout internal = layout.findViewById(R.id.messageContainer);

		if (!message.isByMe()) {
			layout.setGravity(Gravity.START);
			internal.setBackground(ContextCompat.getDrawable(context, R.drawable.recieve_message_background));
			holder.messageStatus.setVisibility(View.GONE);
		} else {
			String temp = message.getStatus();
			holder.messageStatus.setVisibility(View.VISIBLE);
			layout.setGravity(Gravity.END);
			internal.setBackground(ContextCompat.getDrawable(context, R.drawable.send_message_background));

			if (temp.equals("pending")) {
				holder.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.waiting));
			} else if (temp.equals("sent")) {
				holder.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.sending));
			} else if (temp.equals("received")) {
				holder.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.recieved));
			} else if (temp.equals("seen")) {
				holder.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.opened));
			} else {
				holder.messageStatus.setVisibility(View.GONE);
			}
		}

//        holder.layout.setBackgroundColor(message.isSelected() ? Color.CYAN : null);
//        holder.layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                message.setSelected(!message.isSelected());
//                holder.layout.setBackgroundColor(message.isSelected() ? Color.CYAN : null);
//            }
//        });
	}

	@Override
	public int getItemCount() {
		return messages.size();
	}

	public void setClickListener(View.OnClickListener callback) {
		mClickListener = callback;
	}

	public class ChatMessageHolder extends RecyclerView.ViewHolder {
		public ImageView messageImage;
		public VideoView videoView;
		public TextView messageContent;
		public TextView messageTime;
		public ImageView messageStatus;
		public ProgressBar mediaLoadingProgressBar;
		View layout;

		public ChatMessageHolder(View view) {
			super(view);
			this.layout = view;

			messageImage = view.findViewById(R.id.messageImage);
			videoView = view.findViewById(R.id.messageVideo);
			messageContent = view.findViewById(R.id.messageContent);
			messageTime = view.findViewById(R.id.messageTime);
			messageStatus = view.findViewById(R.id.messageStatus);
			mediaLoadingProgressBar = view.findViewById(R.id.messageMediaLoadingProgressBar);
		}

		public View getLayout() {
			return layout;
		}
	}

}
