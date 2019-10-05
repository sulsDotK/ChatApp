package HelperClasses;

/**
 * Created by Suleman Khalid on 12/4/2017.
 */

public class FirebaseMessage {
	private String content;
	private String mediaUrl;
	private String timeStamp;
	private String status;

	public FirebaseMessage() {

	}

	public FirebaseMessage(String content, String mediaUrl, String timeStamp, String status, boolean byMe) {
		this.content = content;
		this.mediaUrl = mediaUrl;
		this.timeStamp = timeStamp;
		this.status = status;
	}


	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

}
