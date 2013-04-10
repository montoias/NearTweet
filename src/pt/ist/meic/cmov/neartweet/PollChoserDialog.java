package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class PollChoserDialog extends DialogFragment {

	String sender;
	ArrayList<String> answers;

	public interface PollChoserListener {
		public void onDialogChoice(DialogFragment dialog, String answer, String sender);
	}

	PollChoserListener mListener;

	public PollChoserDialog() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		Bundle b = getArguments();
		sender = b.getString("sender");
		answers = b.getStringArrayList("answers");
		
		try{
			mListener = (PollChoserListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity + " must implement PollChoserListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Choose your answer")
		.setItems(answers.toArray(new CharSequence[answers.size()]), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onDialogChoice(PollChoserDialog.this, answers.get(which), sender);
			}
		});
		return builder.create();
	}
}
