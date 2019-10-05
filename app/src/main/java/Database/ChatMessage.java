package Database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import HelperClasses.FirebaseMessage;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

/**
 * Created by Suleman Khalid on 11/7/2017.
 */

@Entity
public class ChatMessage {
	public ToOne<Chat> chatToOne;
	@Id
	long id;
	private String content;
	private String mediaUrl;
	private String mediaPath;
	private String timeStamp;
	private String status;
	private boolean byMe;
	private boolean isSelected;

	public ChatMessage(ChatMessage message) {
		this.chatToOne.setTarget(message.chatToOne.getTarget());
		this.content = message.content;
		this.mediaUrl = message.mediaUrl;
		this.mediaPath = message.mediaPath;
		this.timeStamp = message.timeStamp;
		this.status = message.status;
		this.byMe = message.byMe;
	}

	public ChatMessage() {

	}

	public ChatMessage(Chat toOne, FirebaseMessage message) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date d = new Date();

		this.timeStamp = df.format(d);
		this.chatToOne.setTarget(toOne);
		this.mediaUrl = message.getMediaUrl();
		this.mediaPath = "";
		this.status = message.getStatus();
		this.byMe = false;
		this.content = message.getContent();
	}

	public ChatMessage(Chat chatToOne, String content, String mediaUrl, String mediaPath, String timeStamp, String status, boolean byMe) {
		this.chatToOne.setTarget(chatToOne);
		this.content = content;
		this.mediaUrl = mediaUrl;
		this.mediaPath = mediaPath;
		this.timeStamp = timeStamp;
		this.status = status;
		this.byMe = byMe;
	}

	/*Modified 2:52 am 12/4/17*/
	@Override
	public boolean equals(Object o) {
		if (o != null && o.getClass().equals(this.getClass())) {
			ChatMessage c = (ChatMessage) o;
			if (c.id == this.id) {
				return true;
			}
		}
		return false;
	}

	public boolean isByMe() {
		return byMe;
	}

	public void setByMe(boolean byMe) {
		this.byMe = byMe;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}
}
