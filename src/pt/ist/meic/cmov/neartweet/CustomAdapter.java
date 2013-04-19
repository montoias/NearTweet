package pt.ist.meic.cmov.neartweet;
import java.util.ArrayList;

import pt.ist.meic.cmov.neartweet.dto.TweetDto;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends BaseAdapter {

	private ArrayList<TweetDto> _data;
	Context _c;

	CustomAdapter(ArrayList<TweetDto> data, Context c) {
		_data = data;
		_c = c;
	}

	public int getCount() {
		return _data.size();
	}

	public Object getItem(int position) {
		return _data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) _c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.list_item_message, null);
		}

		ImageView image = (ImageView) v.findViewById(R.id.icon);
		ImageView attachment = (ImageView) v.findViewById(R.id.attachment);
		TextView fromView = (TextView) v.findViewById(R.id.From);
		TextView descView = (TextView) v.findViewById(R.id.description);
		TextView timeView = (TextView) v.findViewById(R.id.time);
		
		TweetDto msg = _data.get(position);
		if(msg.getAvatar() == null)
			image.setImageResource(R.drawable.ic_launcher);
		else
			image.setImageBitmap(Utils.convertBytesToBmp(msg.getAvatar()));
		
		fromView.setText("@" + msg.getSender());
		descView.setText(msg.getTweet());
		timeView.setText(msg.getCurrentTime());
		
		if(msg.getImage() != null){
			attachment.setImageResource(R.drawable.paperclip_white);
		} else if(msg.getType() == msg.TYPE_POLL){
			attachment.setImageResource(R.drawable.pie_chart);
		} else 
			attachment.setImageResource(0);
		return v;
	}
}
