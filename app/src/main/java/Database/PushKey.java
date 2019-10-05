package Database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

/**
 * Created by Suleman Khalid on 12/5/2017.
 */

@Entity
public class PushKey {
	public ToOne<Chat> chatToOne;
	@Id
	long id;
	String key;
	boolean byMe;
	long msgID;

	public PushKey() {

	}

	public PushKey(String key) {
		this.key = key;
	}

	public long getMsgID() {
		return msgID;
	}

	public void setMsgID(long msgID) {
		this.msgID = msgID;
	}

	public boolean isByMe() {
		return byMe;
	}

	public void setByMe(boolean byMe) {
		this.byMe = byMe;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj != null && obj.getClass().equals(this.getClass())) {
			return ((PushKey) obj).key.equals(this.key);
		}

		return false;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
