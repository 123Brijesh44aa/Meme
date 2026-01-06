package whats.app.meme;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import whats.app.meme.data.MemeRepository;
import whats.app.meme.model.Meme;
import whats.app.meme.ui.MemeAdapter;
import whats.app.meme.utils.MemeStorage;

public class MainActivity extends AppCompatActivity {

    private MemeRepository memeRepository;
    private MemeStorage memeStorage;
    private MemeAdapter memeAdapter;
    private ProgressBar progressBar;
    private ViewPager2 viewPager;
    private String currentSubreddit = "dankmemes"; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memeRepository = new MemeRepository(this);
        memeStorage = new MemeStorage(this);

        initViews();
        loadMemes(currentSubreddit);
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        viewPager = findViewById(R.id.viewPager);
        FloatingActionButton fabSearch = findViewById(R.id.fabSearch);
        FloatingActionButton fabCollection = findViewById(R.id.fabCollection);

        memeAdapter = new MemeAdapter(this, (meme, bitmap) -> shareMeme(meme, bitmap));
        viewPager.setAdapter(memeAdapter);

        // Infinite scroll simulation: Load more when nearing end
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position >= memeAdapter.getItemCount() - 3) {
                     loadMemes(currentSubreddit);
                }
            }
        });

        fabSearch.setOnClickListener(v -> showSearchDialog());
        fabCollection.setOnClickListener(v -> {
             // Intent to CollectionActivity (to be implemented)
             // Toast.makeText(this, "Collection Feature Coming Soon", Toast.LENGTH_SHORT).show();
             startActivity(new Intent(MainActivity.this, CollectionActivity.class));
        });
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Memes (Subreddit)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("e.g. funny, cats, gaming");
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) {
                currentSubreddit = query;
                // Clear adapter and reload
                memeAdapter.setMemes(new java.util.ArrayList<>());
                loadMemes(currentSubreddit);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void loadMemes(String subreddit) {
        if (progressBar.getVisibility() == View.VISIBLE) return; // Prevent multiple calls
        progressBar.setVisibility(View.VISIBLE);

        // Fetch batch of 10 memes
        memeRepository.fetchMemes(subreddit, 10, new MemeRepository.MemeListCallback() {
            @Override
            public void onSuccess(List<Meme> memes) {
                progressBar.setVisibility(View.GONE);
                if (memes != null && !memes.isEmpty()) {
                    memeAdapter.addMemes(memes);
                } else {
                    Toast.makeText(MainActivity.this, "No memes found for: " + subreddit, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareMeme(Meme meme, Bitmap bitmap) {
        Uri uri = memeStorage.saveImageToCache(bitmap);
        if (uri != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Meme via..."));
        } else {
            Toast.makeText(MainActivity.this, "Failed to share", Toast.LENGTH_SHORT).show();
        }
    }
}
