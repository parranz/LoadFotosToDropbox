package com.example.loadfotos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class UploadManager extends AsyncTask<File, Long, Void> {

	private Context context;
	private String resultMsg;
	ProgressDialog dialog;

	private DropboxAPI<AndroidAuthSession> dropboxAPI;

	public UploadManager(Context context,
			DropboxAPI<AndroidAuthSession> dropboxAPI) {
		this.context = context;
		this.dropboxAPI = dropboxAPI;
	}

	private void uploadIntoDropbox(File file) throws DropboxException,
			IOException {

		FileInputStream inputStream = new FileInputStream(file);

		if (dropboxAPI.getSession().authenticationSuccessful()) {
			try {
				Entry response = dropboxAPI.putFile("/" + file.getName(),
						inputStream, file.length(), null, null);
				Log.i("uploadIntoDropbox", "The uploaded file's rev is: "
						+ response.rev);
			} catch (Exception e) {
				Log.e("uploadIntoDropbox",
						"error subiendo el fichero a dropbox", e);
			}
			inputStream.close();
		} else {
			resultMsg = "Not logged into dropbox!";
			Log.i("uploadIntoDropbox", "Not logged into dropbox!");
		}
		inputStream.close();
	}

	@Override
	protected Void doInBackground(File... params) {

		resultMsg = "";
		for (File file : params) {
			try {
				resultMsg += file.getName() + ": ";
				uploadIntoDropbox(file);
				resultMsg += context
						.getString(R.string.upload_finished_successfully)
						+ "\n";
			} catch (DropboxUnlinkedException e) {
				resultMsg += context
						.getString(R.string.dropbox_unlinked)
						+ "\n";
			} catch (DropboxFileSizeException e) {
				resultMsg += context
						.getString(R.string.file_too_big)
						+ "\n";
			} catch (DropboxPartialFileException e) {				
				resultMsg += context
						.getString(R.string.dropbox_error)
						+ "\n";
			} catch (DropboxServerException e) {
				if (e.body.userError != null) {
					resultMsg += e.body.userError + "\n";
				} else {
					resultMsg += e.body.error + "\n";
				}
			} catch (DropboxIOException e) {
				resultMsg += context
						.getString(R.string.network_error)
						+ "\n";
			} catch (DropboxParseException e) {
				// Probably due to Dropbox server restarting, should retry
				resultMsg += context
						.getString(R.string.dropbox_server_error)
						+ "\n";
			} catch (DropboxException e) {
				// Unknown error
				resultMsg += context
						.getString(R.string.unknown_error)
						+ "\n";
			} catch (IOException e) {
				resultMsg += context
						.getString(R.string.io_error)
						+ "\n";
			}
		}

		return null;
	}
		
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		showSpinner();
	}

	@Override
	protected void onPostExecute(Void result) {
		dialog.dismiss();
		Toast.makeText(context, resultMsg,Toast.LENGTH_LONG).show();
	}
	
	private void showSpinner() {
		if (dialog == null) {
			dialog = new ProgressDialog(context);	        
	        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        dialog.setIndeterminate(true);
		} 
		dialog.setMessage(context.getString(R.string.uploading_file));
		dialog.show();
	}

}
