package com.example.parstagram.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.parstagram.Like;
import com.example.parstagram.Post;
import com.example.parstagram.R;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

public class PostDetailsFragment extends Fragment {

    public static final String TAG = "PostDetailsFragment";

    private ImageView ivDetailsProfile;
    private TextView tvDetailsUsername;
    private TextView tvCreatedAt;
    private ImageView ivDetailsImage;
    private ImageButton ibDetailsHeart;
    private ImageButton ibDetailsComment;
    private TextView tvDetailsUsername2;
    private TextView tvDetailsDescription;
    private TextView tvDetailsNumLikes;

    private Post post;

    public PostDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivDetailsProfile = view.findViewById(R.id.ivDetailsProfile);
        tvDetailsUsername = view.findViewById(R.id.tvDetailsUsername);
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
        ivDetailsImage = view.findViewById(R.id.ivDetailsImage);
        ibDetailsHeart = view.findViewById(R.id.ibDetailsHeart);
        ibDetailsComment = view.findViewById(R.id.ibDetailsComment);
        tvDetailsUsername2 = view.findViewById(R.id.tvDetailsUsername2);
        tvDetailsDescription = view.findViewById(R.id.tvDetailsDescription);
        tvDetailsNumLikes = view.findViewById(R.id.tvDetailsNumLikes);

        Bundle bundle = this.getArguments();

        if (bundle != null){
            post = bundle.getParcelable("post");
            tvDetailsUsername.setText(post.getUser().getUsername());
            tvDetailsUsername2.setText(post.getUser().getUsername());
            tvDetailsDescription.setText(post.getDescription());

            ParseFile profile = post.getUser().getParseFile("profile");
            if (profile != null) {
                Glide.with(this).load(profile.getUrl()).circleCrop().into(ivDetailsProfile);
            }

            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(this).load(image.getUrl()).into(ivDetailsImage);
            }

            Date createdAt = post.getCreatedAt();
            String timeAgo = Post.calculateTimeAgo(createdAt);
            tvCreatedAt.setText(timeAgo);

            bindButton(post);
            updateLikeCount(post);

            ibDetailsHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (post.isLiked) {
                        unlikePost(post);
                    } else {
                        likePost(post);
                    }
                    updateLikeCount(post);
                }
            });
        }
    }

    public void bindButton(Post post) {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);

        // See if post is liked by the current user
        query.whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Like.KEY_POST, post);

        query.findInBackground(new FindCallback<Like>() {
            @Override
            public void done(List<Like> likes, ParseException e) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting likes", e);
                    return;
                }

                if (!likes.isEmpty()) {
                    post.isLiked = true;
                    ibDetailsHeart.setImageResource(R.drawable.ufi_heart_active);
                } else {
                    post.isLiked = false;
                    ibDetailsHeart.setImageResource(R.drawable.ufi_heart);
                }
            }
        });
    }

    public void updateLikeCount(Post post) {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.whereEqualTo(Like.KEY_POST, post);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (count == 1) {
                    tvDetailsNumLikes.setText(String.valueOf(count + " like"));
                } else {
                    tvDetailsNumLikes.setText(String.valueOf(count + " likes"));
                }
            }
        });
    }

    public void likePost(Post post) {
        Like like = new Like();
        like.setUser(ParseUser.getCurrentUser());
        like.setPost(post);
        like.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
            }
        });
        post.updateLikes();
        post.isLiked = !post.isLiked;
        ibDetailsHeart.setImageResource(R.drawable.ufi_heart_active);
    }

    public void unlikePost(Post post) {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Like.KEY_POST, post);

        query.findInBackground(new FindCallback<Like>() {
            @Override
            public void done(List<Like> likes, ParseException e) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting likes", e);
                    return;
                }
                Log.i(TAG, "exists something");
                if (!likes.isEmpty()) {
                    likes.get(0).deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                        }
                    });
                    post.updateLikes();
                    post.isLiked = !post.isLiked;
                    ibDetailsHeart.setImageResource(R.drawable.ufi_heart);
                    Log.i(TAG, "should have deleted");
                }
            }
        });
    }
}