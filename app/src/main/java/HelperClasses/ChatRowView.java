package HelperClasses;

import android.graphics.drawable.Drawable;

/**
 * Created by Saif Ullah on 10/18/2017.
 */

public class ChatRowView {
	Drawable profileImage;
	String name;
	String lastMessage;

	public ChatRowView(Drawable profileImage, String name) {
		this.profileImage = profileImage;
		this.name = name;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}
}
