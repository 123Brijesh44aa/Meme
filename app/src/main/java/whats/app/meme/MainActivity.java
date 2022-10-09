package whats.app.meme;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.service.chooser.ChooserTargetService;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import whats.app.meme.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private String currentImageUrl=null;
    private ProgressDialog progressDialog;
    private String url="https://meme-api.herokuapp.com/gimme";
    private JsonObjectRequest jsonObjectRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("loading...");

        loadMeme();

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent=new Intent(Intent.ACTION_SEND);
//                Uri uri=Uri.parse(currentImageUrl);
//                intent.setType("image/jpg");
//                intent.putExtra(Intent.EXTRA_STREAM,"Hey! Checkout this cool meme.\n"+uri);
//                startActivity(Intent.createChooser(intent,"Share this using..."));

                shareItem(currentImageUrl);
            }
        });

        binding.load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMeme();
            }
        });
    }

    private void shareItem(String ImageUrl) {
       Picasso.get().load(ImageUrl).into(new com.squareup.picasso.Target() {
           @Override
           public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
               Intent intent=new Intent(Intent.ACTION_SEND);
               intent.setType("image/*");
               intent.putExtra(Intent.EXTRA_STREAM, getUriFromBitmap(bitmap) );
               startActivity(Intent.createChooser(intent,"Share this using..."));
           }

           @Override
           public void onBitmapFailed(Exception e, Drawable errorDrawable) {

           }

           @Override
           public void onPrepareLoad(Drawable placeHolderDrawable) {

           }
       });
    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bytes);
        String path= MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),bitmap,"Title",null);
        return Uri.parse(path);
    }

    private void loadMeme() {

        //show loading progress dialogue
        progressDialog.show();

        //create  JsonObject Request
        jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    currentImageUrl=response.getString("url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                using glide library to load the meme image in ImageView
                with added request listener to hide progress bar when image is loaded.
                 */
                Glide.with(MainActivity.this).load(currentImageUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressDialog.dismiss();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressDialog.dismiss();
                        return false;
                    }
                }).into(binding.imageView);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(binding.layout,error.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQue(jsonObjectRequest);
    }


}