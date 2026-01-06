package whats.app.meme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import whats.app.meme.data.MemeRepository;
import whats.app.meme.model.Meme;
import whats.app.meme.utils.MemeStorage;

public class MainActivity extends AppCompatActivity {

    private MemeRepository memeRepository;
    private MemeStorage memeStorage;
    private Meme currentMeme;
    private String currentSubreddit = "Random";

    private ImageView imageView;
    private TextView memeTitle;
    private TextView memeAuthor;
    private ProgressBar progressBar;
    private ImageButton btnFavorite;
    private ExtendedFloatingActionButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memeRepository = new MemeRepository(this);
        memeStorage = new MemeStorage(this);

        initViews();
        setupSpinner();
        loadMeme();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imageView);
        memeTitle = findViewById(R.id.memeTitle);
        memeAuthor = findViewById(R.id.memeAuthor);
        progressBar = findViewById(R.id.progressBar);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnNext = findViewById(R.id.btnNext);
        // Using findViewById without casting to MaterialButton since XML defines Button but style/theme makes it Material
        View btnShare = findViewById(R.id.btnShare);
        View btnSave = findViewById(R.id.btnSave);

        btnNext.setOnClickListener(v -> loadMeme());

        btnShare.setOnClickListener(v -> {
            if (currentMeme != null) {
                shareMeme();
            }
        });

        btnSave.setOnClickListener(v -> {
            if (currentMeme != null) {
                saveMeme();
            }
        });

        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void setupSpinner() {
        Spinner spinner = findViewById(R.id.categorySpinner);
        String[] categories = {"Random", "memes", "dankmemes", "wholesomememes", "ProgrammerHumor", "funny"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = categories[position];
                if (!selected.equals(currentSubreddit)) {
                    currentSubreddit = selected;
                    loadMeme();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadMeme() {
        progressBar.setVisibility(View.VISIBLE);
        memeRepository.fetchMeme(currentSubreddit, new MemeRepository.MemeCallback() {
            @Override
            public void onSuccess(Meme meme) {
                currentMeme = meme;
                updateUI(meme);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Meme meme) {
        memeTitle.setText(meme.getTitle());
        memeAuthor.setText("u/" + meme.getAuthor());
        updateFavoriteIcon();

        Glide.with(this)
                .load(meme.getUrl())
                .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }

    private void toggleFavorite() {
        if (currentMeme == null) return;
        boolean isFav = memeStorage.isFavorite(currentMeme.getUrl());
        memeStorage.setFavorite(currentMeme.getUrl(), !isFav);
        updateFavoriteIcon();
        String msg = !isFav ? "Added to Favorites" : "Removed from Favorites";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteIcon() {
        if (currentMeme == null) return;
        boolean isFav = memeStorage.isFavorite(currentMeme.getUrl());
        btnFavorite.setImageResource(isFav ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
    }

    private void shareMeme() {
        Glide.with(this)
                .asBitmap()
                .load(currentMeme.getUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Uri uri = memeStorage.saveImageToCache(resource);
                        if (uri != null) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("image/png");
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "Share Meme via..."));
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to prepare image for sharing", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private void saveMeme() {
        Glide.with(this)
                .asBitmap()
                .load(currentMeme.getUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        String path = memeStorage.saveImageToGallery(resource, currentMeme.getTitle());
                        if (path != null) {
                            Toast.makeText(MainActivity.this, "Saved to " + path, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }
}
