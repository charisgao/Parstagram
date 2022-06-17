package com.example.parstagram;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.activities.MainActivity;
import com.example.parstagram.fragments.PostDetailsFragment;
import com.example.parstagram.fragments.ProfileFragment;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>{

    public static final String TAG = "PostsAdapter";
    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfile;
        private TextView tvUsername;
        private ImageView ivImage;
        private TextView tvDescription;
        private TextView tvCreatedAt;
        private ImageButton ibHeart;
        private ImageButton ibComment;
        private TextView tvNumLikes;
        private TextView tvNumComments;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUsername =  itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvNumLikes = itemView.findViewById(R.id.tvNumLikes);
            tvNumComments = itemView.findViewById(R.id.tvNumComments);
            ibHeart = itemView.findViewById(R.id.ibHeart);
            ibComment = itemView.findViewById(R.id.ibComment);

            // When the user clicks on a row, show PostDetailsFragment for the selected post
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Post post = posts.get(position);

                        PostDetailsFragment postDetailsFragment = new PostDetailsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("post", post);
                        postDetailsFragment.setArguments(bundle);
                        FragmentTransaction transaction =((MainActivity) context).getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.flContainer, postDetailsFragment).addToBackStack(null).commit();
                    }
                }
            });
        }

        public void bind(Post post) {
            // Bind the post data to the view elements
            tvUsername.setText(post.getUser().getUsername());
            tvDescription.setText(post.getDescription());
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }

            ParseFile profile = post.getUser().getParseFile("profile");
            if (profile != null) {
                Glide.with(context).load(profile.getUrl()).placeholder(R.drawable.profile).circleCrop().into(ivProfile);
            }

            Date createdAt = post.getCreatedAt();
            String timeAgo = Post.calculateTimeAgo(createdAt);
            tvCreatedAt.setText(timeAgo);

            bindButton(post);
            bindLikeCount(post);
            bindCommentCount(post);

            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileFragment otherProfileFragment = new ProfileFragment(post.getUser());
                    FragmentTransaction transaction =((MainActivity) context).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.flContainer, otherProfileFragment).addToBackStack(null).commit();
                }
            });

            tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileFragment otherProfileFragment = new ProfileFragment(post.getUser());
                    FragmentTransaction transaction =((MainActivity) context).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.flContainer, otherProfileFragment).addToBackStack(null).commit();
                }
            });

            ibHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (post.isLiked) {
                        unlikePost(post);
                    } else {
                        likePost(post);
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
                        ibHeart.setImageResource(R.drawable.ufi_heart_active);
                    } else {
                        post.isLiked = false;
                        ibHeart.setImageResource(R.drawable.ufi_heart);
                    }
                }
            });
        }

        public void bindLikeCount(Post post) {
            ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
            query.whereEqualTo(Like.KEY_POST, post);
            query.countInBackground(new CountCallback() {
                @Override
                public void done(int count, ParseException e) {
                    if (count == 1) {
                        tvNumLikes.setText(String.valueOf(count + " like"));
                    } else {
                        tvNumLikes.setText(String.valueOf(count + " likes"));
                    }
                }
            });
        }

        public void bindCommentCount(Post post) {
            ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
            query.whereEqualTo(Comment.KEY_POST, post);
            query.countInBackground(new CountCallback() {
                @Override
                public void done(int count, ParseException e) {
                    if (count == 1) {
                        tvNumComments.setText(String.valueOf(count + " comment"));
                    } else {
                        tvNumComments.setText(String.valueOf(count + " comments"));
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
            post.isLiked = !post.isLiked;
            ibHeart.setImageResource(R.drawable.ufi_heart_active);
            int count = post.updateLikes();
            if (count == 1) {
                tvNumLikes.setText(String.valueOf(count + " like"));
            } else {
                tvNumLikes.setText(String.valueOf(count + " likes"));
            }
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
                    if (!likes.isEmpty()) {
                        likes.get(0).deleteInBackground();
                        post.isLiked = !post.isLiked;
                        ibHeart.setImageResource(R.drawable.ufi_heart);
                        int count = post.updateLikes();
                        if (count == 1) {
                            tvNumLikes.setText(String.valueOf(count + " like"));
                        } else {
                            tvNumLikes.setText(String.valueOf(count + " likes"));
                        }
                    }
                }
            });
        }
    }
}
