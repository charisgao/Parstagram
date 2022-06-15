package com.example.parstagram.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.parstagram.Post;
import com.example.parstagram.R;
import com.parse.ParseFile;

import java.util.Date;

public class PostDetailsFragment extends Fragment {

    public static final String TAG = "PostDetailsFragment";

    private ImageView ivDetailsProfile;
    private TextView tvDetailsUsername;
    private TextView tvCreatedAt;
    private ImageView ivDetailsImage;
    private ImageButton ibHeart;
    private ImageButton ibComment;
    private TextView tvDetailsUsername2;
    private TextView tvDetailsDescription;

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
        ibHeart = view.findViewById(R.id.ibHeart);
        ibComment = view.findViewById(R.id.ibComment);
        tvDetailsUsername2 = view.findViewById(R.id.tvDetailsUsername2);
        tvDetailsDescription = view.findViewById(R.id.tvDetailsDescription);

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
        }
    }
}