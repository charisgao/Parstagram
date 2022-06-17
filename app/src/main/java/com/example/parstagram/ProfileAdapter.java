package com.example.parstagram;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.activities.MainActivity;
import com.example.parstagram.fragments.PostDetailsFragment;
import com.example.parstagram.fragments.ProfileFragment;
import com.parse.ParseFile;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>{

    private Context context;
    private List<Post> posts;

    public ProfileAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false);
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
        private ImageView ivImageGrid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImageGrid = itemView.findViewById(R.id.ivImageGrid);
        }

        public void bind(Post post) {
            // Bind the post data to the view elements
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).centerCrop().into(ivImageGrid);
            }

            ivImageGrid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PostDetailsFragment postDetailsFragment = new PostDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("post", post);
                    postDetailsFragment.setArguments(bundle);
                    FragmentTransaction transaction =((MainActivity) context).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.flContainer, postDetailsFragment).addToBackStack(null).commit();
                }
            });
        }
    }
}
