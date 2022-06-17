package com.example.parstagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Like.class);
        ParseObject.registerSubclass(Comment.class);

        // Register your parse models
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("FtpOpzKUZNTWdcUzy1r921jPXgtfgLkv0gKtmhy0")
                .clientKey("4PFGpSNY0VXfgQbCsuxeO8EFzfTQNw6DTE24Csgt")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
