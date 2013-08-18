package com.example.loadfotos;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class MainActivity extends Activity {

	final static private int SELECT_IMAGE = 6987458;
	final static private int TAKE_FOTO = 6987459;
	
	final static private String APP_KEY = "CHANGE_ME";
	final static private String APP_SECRET = "CHANGE_ME";
	
	final static private String PREF_NAME_TOKEN_KEY = "key";
	final static private String PREF_NAME_TOKEN_SECRET = "secret";
	final static private String PREF_NAME = "LoadFotosPreferences";
	
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	private DropboxAPI<AndroidAuthSession> dropboxAPI;
	private UploadManager uploadManager;
	
	private void initDropbox() {
		
		AndroidAuthSession session = buildSession(); 
		dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
		dropboxAPI.getSession().startAuthentication(MainActivity.this);
		uploadManager = new UploadManager(MainActivity.this, dropboxAPI);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initDropbox();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void launchCamera(View view) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		startActivityForResult(intent,TAKE_FOTO);
	}

	public void loadFromGallery(View view) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(
				Intent.createChooser(intent,
						view.getContext().getText(R.string.selectImage)),
				SELECT_IMAGE);
	}
	
	public void changeUser(View view) {
		logoutDropbox();
		initDropbox();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if ((requestCode == SELECT_IMAGE || requestCode == TAKE_FOTO) & resultCode == RESULT_OK
				& data != null) {
			Uri selectedImage = data.getData();

			String path = getPath(selectedImage);
			uploadManager.execute(new File(path));

		}
		
	}

	protected void onResume() {
		super.onResume();

		if (dropboxAPI.getSession().authenticationSuccessful()) {
			try {
				// Required to complete auth, sets the access token on the
				// session
				dropboxAPI.getSession().finishAuthentication();
				AccessTokenPair tokens = dropboxAPI.getSession()
						.getAccessTokenPair();
				storeKeys(tokens);
			} catch (IllegalStateException e) {
				Log.i("DbAuthLog", "Error authenticating", e);
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		startManagingCursor(cursor);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
    private void storeKeys(AccessTokenPair tokens) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.putString(PREF_NAME_TOKEN_KEY, tokens.key);
        edit.putString(PREF_NAME_TOKEN_SECRET, tokens.secret);
        edit.commit();
    }

    private void logoutDropbox() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        AccessTokenPair tokens = getAccesTokenPair();
        if (tokens != null) {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, tokens);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    private AccessTokenPair getAccesTokenPair() {
    	AccessTokenPair accessToken = null; 
        SharedPreferences prefs =  getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = prefs.getString(PREF_NAME_TOKEN_KEY, null);
        String secret = prefs.getString(PREF_NAME_TOKEN_SECRET, null);
        if (key != null && secret != null) {
        	accessToken = new AccessTokenPair(key, secret);
        } 
        
        return accessToken;
    }

}
