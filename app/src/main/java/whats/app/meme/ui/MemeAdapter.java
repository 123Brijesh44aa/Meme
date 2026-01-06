package whats.app.meme.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import whats.app.meme.R;
import whats.app.meme.model.Meme;
import whats.app.meme.utils.MemeStorage;

public class MemeAdapter extends RecyclerView.Adapter<MemeAdapter.MemeViewHolder> {

    private List<Meme> memeList = new ArrayList<>();
    private final Context context;
    private final MemeStorage memeStorage;
    private final OnMemeActionListener actionListener;

    public interface OnMemeActionListener {
        void onShare(Meme meme, Bitmap bitmap);
    }

    public MemeAdapter(Context context, OnMemeActionListener actionListener) {
        this.context = context;
        this.memeStorage = new MemeStorage(context);
        this.actionListener = actionListener;
    }

    public void setMemes(List<Meme> memes) {
        this.memeList = memes;
        notifyDataSetChanged();
    }

    public void addMemes(List<Meme> memes) {
        int start = this.memeList.size();
        this.memeList.addAll(memes);
        notifyItemRangeInserted(start, memes.size());
    }

    @NonNull
    @Override
    public MemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meme_page, parent, false);
        return new MemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemeViewHolder holder, int position) {
        Meme meme = memeList.get(position);
        holder.bind(meme);
    }

    @Override
    public int getItemCount() {
        return memeList.size();
    }

    class MemeViewHolder extends RecyclerView.ViewHolder {
        ImageView memeImage;
        TextView memeTitle, memeAuthor;
        ImageButton btnFavorite, btnSave, btnShare;

        MemeViewHolder(@NonNull View itemView) {
            super(itemView);
            memeImage = itemView.findViewById(R.id.memeImage);
            memeTitle = itemView.findViewById(R.id.memeTitle);
            memeAuthor = itemView.findViewById(R.id.memeAuthor);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnSave = itemView.findViewById(R.id.btnSave);
            btnShare = itemView.findViewById(R.id.btnShare);
        }

        void bind(Meme meme) {
            memeTitle.setText(meme.getTitle());
            memeAuthor.setText("u/" + meme.getAuthor());

            updateFavoriteIcon(meme);

            Glide.with(context).load(meme.getUrl()).into(memeImage);

            btnFavorite.setOnClickListener(v -> {
                boolean isFav = memeStorage.isFavorite(meme.getUrl());
                memeStorage.setFavorite(meme.getUrl(), !isFav);
                updateFavoriteIcon(meme);
                Toast.makeText(context, !isFav ? "Added to Favorites" : "Removed", Toast.LENGTH_SHORT).show();
            });

            btnSave.setOnClickListener(v -> {
                Glide.with(context).asBitmap().load(meme.getUrl()).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        String path = memeStorage.saveImageToGallery(resource, meme.getTitle());
                        Toast.makeText(context, path != null ? "Saved to Gallery" : "Failed to Save", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
            });

            btnShare.setOnClickListener(v -> {
                Glide.with(context).asBitmap().load(meme.getUrl()).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        actionListener.onShare(meme, resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
            });
        }

        private void updateFavoriteIcon(Meme meme) {
            boolean isFav = memeStorage.isFavorite(meme.getUrl());
            btnFavorite.setImageResource(isFav ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        }
    }
}
