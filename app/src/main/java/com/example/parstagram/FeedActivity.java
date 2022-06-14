package com.example.parstagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    public static final String TAG = "FeedActivity";
    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    private RecyclerView rvPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        rvPosts = findViewById(R.id.rvPosts);

        adapter = new PostsAdapter(this, allPosts);
        allPosts = new ArrayList<>();

        // Set the adapter on the RV
        rvPosts.setAdapter(adapter);

        // Set the layout manager on the RV
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        queryPosts();
    }

    private void queryPosts() {
        // Specify what type of data we want to query – Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Include data referred by user key
        query.include(Post.KEY_USER);
        // Limit query to 20 items
        query.setLimit(20);
        // Order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // Start an asynchronous call for posts
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
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }
}