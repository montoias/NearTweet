package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class PollChoserDialog extends DialogFragment {

	String question;
	String asker;
	ArrayList<String> answers;

	public interface PollChoserListener {
		public void onDialogChoice(DialogFragment dialog, String answer);
	}

	PollChoserListener mListener;

	public PollChoserDialog() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		Bundle b = getArguments();
		question = b.getString("question");
		asker = b.getString("asker");
		answers = b.getStringArrayList("answers");
		
		question = asker + " asks:" + question.substring(5);
		
		try{
			mListener = (PollChoserListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity + " must implement PollChoserListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(question)
		.setItems(answers.toArray(new CharSequence[answers.size()]), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onDialogChoice(PollChoserDialog.this, answers.get(which));
			}
		});
		return builder.create();
	}
}
