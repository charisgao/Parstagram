package com.example.parstagram;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseException;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    public static final String TAG = "CommentsAdapter";
    private Context context;
    private List<Comment> comments;

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCommentUsername;
        private TextView tvCommentDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUsername =  itemView.findViewById(R.id.tvCommentUsername);
            tvCommentDescription = itemView.findViewById(R.id.tvCommentDescription);
        }

        public void bind(Comment comment) {
            tvCommentUsername.setText(comment.getUser().getUsername());
            tvCommentDescription.setText(comment.getComment());
        }
    }
}
