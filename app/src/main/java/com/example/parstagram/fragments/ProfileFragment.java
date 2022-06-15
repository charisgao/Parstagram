package com.example.parstagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.parstagram.Post;
import com.example.parstagram.PostsAdapter;
import com.example.parstagram.ProfileAdapter;
import com.example.parstagram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    protected ProfileAdapter profileAdapter;
    protected List<Post> profilePosts;

    private RecyclerView rvGridPosts;
    private TextView tvProfileUsername;
    private ImageView ivProfilePicture;
    private Button btnEditProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvGridPosts = view.findViewById(R.id.rvGridPosts);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        tvProfileUsername.setText(ParseUser.getCurrentUser().getUsername());

        ParseFile profile = ParseUser.getCurrentUser().getParseFile("profile");
        if (profile != null) {
            Glide.with(getContext()).load(profile.getUrl()).circleCrop().into(ivProfilePicture);
        }

//            btnEditProfile.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // TODO: add fragment for edit profile (username, profile picture)
//                }
//            });

        profilePosts = new ArrayList<>();
        profileAdapter = new ProfileAdapter(getContext(), profilePosts);

        // Set the adapter on the RV
        rvGridPosts.setAdapter(profileAdapter);

        // Set the layout manager on the RV
        rvGridPosts.setLayoutManager(new GridLayoutManager(getContext(), 3));

        queryPosts();
    }

    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_KEY);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // For debugging
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }

                // Save received posts to list and notify adapter of new data
                profilePosts.addAll(posts);
                profileAdapter.notifyDataSetChanged();
            }
        });
    }
}
