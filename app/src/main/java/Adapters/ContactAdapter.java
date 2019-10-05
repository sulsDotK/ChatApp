package Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.whatsappclone.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import Database.DbContact;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Saif Ullah on 10/18/2017.
 */

public class ContactAdapter extends ArrayAdapter implements Filterable {
	List<DbContact> userContacts;
	List<DbContact> filteredUserContacts;
	private Filter filter;

	public ContactAdapter(@NonNull Context context, @LayoutRes int resource, List<DbContact> userContacts) {
		super(context, resource);
		this.userContacts = userContacts;
		this.filteredUserContacts = userContacts;
	}

	public void updateAdapter(List<DbContact> c) {
		this.userContacts = c;
		this.filteredUserContacts = c;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new ContactsFilter();
		}
		return filter;
	}

	@Override
	public int getCount() {
//        return userContacts.size();
		return filteredUserContacts.size();
	}

	@Nullable
	@Override
	public Object getItem(int position) {
//        return this.userContacts.get(position);
		return this.filteredUserContacts.get(position);
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.contact_view, parent, false);
		}

		DbContact entry = filteredUserContacts.get(position);

		if (position < filteredUserContacts.size()) {
			TextView name = convertView.findViewById(R.id._Name);
			CircleImageView image = convertView.findViewById(R.id._pImage);
			TextView _textStatus = convertView.findViewById(R.id._tStatus);

			if (!entry.getProfilePhotoPath().isEmpty()) {
				Uri uri = Uri.parse(entry.getProfilePhotoPath());
				try {
					InputStream stream = getContext().getContentResolver().openInputStream(uri);
					Drawable d = Drawable.createFromStream(stream, uri.toString());
					image.setImageDrawable(d);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					image.setImageResource(R.drawable.ic_person_black_36dp);
				}
			} else if (!entry.getProfilePhotoUrl().isEmpty()) {
				Glide.with(getContext()).load(entry.getProfilePhotoUrl()).into(image);
			} else {
				image.setImageResource(R.drawable.ic_person_black_36dp);
			}

			name.setText(entry.getUserName());
			_textStatus.setText(entry.getStatus());
		}
		if (entry.isDeleted()) {
			convertView.setVisibility(View.GONE);
		} else {
			convertView.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	private class ContactsFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence charSequence) {
			FilterResults filterResults = new FilterResults();
			String constraint = charSequence.toString();

			if (constraint != null && constraint.length() > 0) {
				filteredUserContacts = new ArrayList<DbContact>();

				for (int i = 0, s = userContacts.size(); i < s; i++) {
					if (userContacts.get(i).getUserName().contains(constraint)) {
						filteredUserContacts.add(userContacts.get(i));
					}
				}

				filterResults.count = filteredUserContacts.size();
				filterResults.values = filteredUserContacts;
			} else {
				filterResults.count = userContacts.size();
				filterResults.values = userContacts;
			}

			return filterResults;
		}

		@Override
		protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
			filteredUserContacts = (ArrayList<DbContact>) filterResults.values;
			notifyDataSetChanged();
		}
	}
}
