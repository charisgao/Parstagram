package com.example.parstagram;

import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "Description";
    public static final String KEY_IMAGE = "Image";
    public static final String KEY_USER = "User";
    public static final String KEY_CREATED_KEY = "createdAt";
    public static final String KEY_NUMLIKES = "NumLikes";

    public boolean isLiked;

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public int updateLikes() {
        if (isLiked) {
            put(KEY_NUMLIKES, getNumber(KEY_NUMLIKES).intValue() + 1);
        } else {
            put(KEY_NUMLIKES, getNumber(KEY_NUMLIKES).intValue() - 1);
        }
        try {
            save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getNumber(KEY_NUMLIKES).intValue();
    }

    public static String calculateTimeAgo(Date createdAt) {
        int SECOND_MILLIS = 1000; // 1000 milliseconds in a second
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 60 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }
        return "";
    }
}
