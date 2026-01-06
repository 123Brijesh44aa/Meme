package whats.app.meme.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import whats.app.meme.R;
import whats.app.meme.utils.MemeStorage;

public class SavedFragment extends Fragment {

    private RecyclerView recyclerView;
    private MemeStorage memeStorage;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                loadSaved();
            } else {
                Toast.makeText(getContext(), "Permission required to view saved memes", Toast.LENGTH_SHORT).show();
            }
        });
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
        checkPermissionAndLoad();
    }

    private void checkPermissionAndLoad() {
        String permission = Build.VERSION.SDK_INT >= 33 ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadSaved();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void loadSaved() {
        List<Uri> uris = memeStorage.getSavedMemes();
        if (uris.isEmpty()) {
            Toast.makeText(getContext(), "No saved memes found", Toast.LENGTH_SHORT).show();
        }
        recyclerView.setAdapter(new ImageGridAdapter(uris));
    }

    private class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ViewHolder> {
        private final List<Uri> uris;

        ImageGridAdapter(List<Uri> uris) {
            this.uris = uris;
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
            Glide.with(SavedFragment.this)
                    .load(uris.get(position))
                    .into((ImageView) holder.itemView);
        }

        @Override
        public int getItemCount() {
            return uris.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
