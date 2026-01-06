package whats.app.meme.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import whats.app.meme.R;
import whats.app.meme.utils.MemeStorage;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MemeStorage memeStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        memeStorage = new MemeStorage(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        List<String> favUrls = memeStorage.getFavoriteMemes();
        if (favUrls.isEmpty()) {
            Toast.makeText(getContext(), "No favorites yet", Toast.LENGTH_SHORT).show();
        }
        recyclerView.setAdapter(new ImageGridAdapter(favUrls));
    }

    private class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ViewHolder> {
        private final List<String> urls;

        ImageGridAdapter(List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 4);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(FavoritesFragment.this)
                    .load(urls.get(position))
                    .into((ImageView) holder.itemView);

            holder.itemView.setOnClickListener(v -> {
                 // Open full screen or something? For now just Toast
                 Toast.makeText(getContext(), "Selected Favorite", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
