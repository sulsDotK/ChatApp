package HelperClasses;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.whatsappclone.R;

/**
 * Created by Saad on 12/1/17.
 */

public class HeaderView extends LinearLayout {
	//    @Bind(R.id.name)
	TextView name;

	//    @Bind(R.id.chatInfoLastSeen)
	TextView lastSeen;

	public HeaderView(Context context) {
		super(context);
	}

	public HeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public HeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
//        ButterKnife.bind(this);
	}

	public void setVariables() {
		name = findViewById(R.id.chatInfoChatName);
		lastSeen = findViewById(R.id.chatInfoLastSeen);
	}

	public void bindTo(String name, String lastSeen) {
		this.name.setText(name);
		this.lastSeen.setText(lastSeen);
	}

	public void setTextSize(float size) {
		name.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
	}
}
