package Database;

import java.io.Serializable;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

/**
 * Created by Suleman Khalid on 11/28/2017.
 */

@Entity
public class Chat implements Cloneable, Serializable {
	public @Id
	long id;
	@Backlink
	public ToMany<ChatMessage> messages;
	@Backlink
	public ToMany<PushKey> unreadKeys;
	private String withWhom;
	private int position;

	public Chat() {

	}

	public Chat(String withWhom, int position) {

		this.withWhom = withWhom;
		this.position = position;
	}

	public ChatMessage getLastMessage() {
		if (messages.size() > 0) {
			return messages.get(messages.size() - 1);
		} else {
			return null;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		Chat chat = new Chat();
		chat.id = id;
		chat.unreadKeys = unreadKeys;
		chat.messages = messages;
		chat.position = position;
		chat.withWhom = withWhom;

		return chat;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getWithWhom() {
		return withWhom;
	}

	public void setWithWhom(String withWhom) {
		this.withWhom = withWhom;
	}

}
