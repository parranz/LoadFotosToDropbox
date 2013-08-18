LoadFotosToDropbox
==================

Android application that allows uploading images (from the gallery or camera) to a dropbox account using the Dropbox Core Api

In order to make the application work you need to register it as a development app in you dropbox account (App Console).

Once you've done that you have your app key and app secret. 

Change in the AndroidManifest.xml:
  
    data android:scheme="db-APP_KEY"

replace APP_KEY with your app key

Change in the MainActivity.java:

  	final static private String APP_KEY = "CHANGE_ME";
    final static private String APP_SECRET = "CHANGE_ME";
    
replace CHANGE_ME with your app key and app secret
