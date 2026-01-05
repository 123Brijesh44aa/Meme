package whats.app.meme.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class MemeStorage {
    private static final String PREF_NAME = "MemeFavorites";
    private static final String KEY_FAVORITES = "favorites";
    private final Context context;
    private final SharedPreferences sharedPreferences;

    public MemeStorage(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFavorite(String url) {
        Set<String> favorites = sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>());
        return favorites.contains(url);
    }

    public void setFavorite(String url, boolean isFavorite) {
        Set<String> favorites = new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
        if (isFavorite) {
            favorites.add(url);
        } else {
            favorites.remove(url);
        }
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public Uri saveImageToCache(Bitmap bitmap) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            File stream = new File(cachePath, "shared_meme.png");
            FileOutputStream outputStream = new FileOutputStream(stream);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            return FileProvider.getUriForFile(context, "whats.app.meme.fileprovider", stream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String saveImageToGallery(Bitmap bitmap, String title) {
        String imageFileName = "MEME_" + System.currentTimeMillis() + ".jpg";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MemeApp");

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try {
                    OutputStream out = resolver.openOutputStream(uri);
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.close();
                        return "Gallery/MemeApp";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/MemeApp";
            File file = new File(imagesDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            File imageFile = new File(file, imageFileName);
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                // Add the image to the system gallery
                galleryAddPic(imageFile.getAbsolutePath());
                return imageFile.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void galleryAddPic(String imagePath) {
        android.content.Intent mediaScanIntent = new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
