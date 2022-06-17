package com.example.parstagram.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.parstagram.Comment;
import com.example.parstagram.CommentsAdapter;
import com.example.parstagram.Like;
import com.example.parstagram.Post;
import com.example.parstagram.ProfileAdapter;
import com.example.parstagram.R;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostDetailsFragment extends Fragment {

    public static final String TAG = "PostDetailsFragment";

    protected CommentsAdapter commentsAdapter;
    protected List<Comment> comments;

    private RecyclerView rvComments;

    private ImageView ivDetailsProfile;
    private TextView tvDetailsUsername;
    private TextView tvCreatedAt;
    private ImageView ivDetailsImage;
    private ImageButton ibDetailsHeart;
    private ImageButton ibDetailsComment;
    private TextView tvDetailsUsername2;
    private TextView tvDetailsDescription;
    private TextView tvDetailsNumLikes;
    private TextView tvDetailsNumComments;

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
        tvDetailsNumComments = view.findViewById(R.id.tvDetailsNumComments);
        rvComments = view.findViewById(R.id.rvComments);

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

            updateCommentCount(post);

            ibDetailsComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddComment();
                }
            });
        }

        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getContext(), comments);

        // Set the adapter on the RV
        rvComments.setAdapter(commentsAdapter);

        // Set the layout manager on the RV
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));

        fillComments();
    }

    private void showAddComment(){
        View messageView = LayoutInflater.from(getContext()).inflate(R.layout.item_comment_snippet, null);

        // Create alert dialog builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        // set message_item.xml to AlertDialog builder
        alertDialogBuilder.setView(messageView);
        // Create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Extract content from alert dialog
                        EditText etAddComment = messageView.findViewById(R.id.etAddComment);

                        Comment newAddComment = new Comment();
                        newAddComment.setUser(ParseUser.getCurrentUser());
                        newAddComment.setPost(post);
                        newAddComment.setComment(etAddComment.getText().toString());
                        newAddComment.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                            }
                        });

                        dialog.cancel();
                    }
                });

        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            }
        });

        alertDialog.show();
    }

    private void fillComments() {
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.include(Comment.KEY_USER);
        query.whereEqualTo(Comment.KEY_POST, post);
        query.addAscendingOrder(Comment.KEY_CREATED_KEY);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> commentList, ParseException e) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments", e);
                    return;
                }
                comments.addAll(commentList);
                commentsAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateCommentCount(Post post) {
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.whereEqualTo(Comment.KEY_POST, post);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (count == 1) {
                    tvDetailsNumComments.setText(String.valueOf(count + " comment"));
                } else {
                    tvDetailsNumComments.setText(String.valueOf(count + " comments"));
                }
            }
        });
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