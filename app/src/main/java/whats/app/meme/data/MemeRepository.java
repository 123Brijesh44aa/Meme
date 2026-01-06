package whats.app.meme.data;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import whats.app.meme.VolleySingleton;
import whats.app.meme.model.Meme;
import whats.app.meme.model.MemeResponse;

public class MemeRepository {
    private static final String BASE_URL = "https://meme-api.com/gimme";
    private final Context context;
    private final Gson gson;

    public interface MemeCallback {
        void onSuccess(Meme meme);
        void onError(String error);
    }

    public interface MemeListCallback {
        void onSuccess(List<Meme> memes);
        void onError(String error);
    }

    public MemeRepository(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public void fetchMeme(String subreddit, MemeCallback callback) {
        // Fallback or single fetch if needed, though mostly we will use fetchMemes now
        String url = BASE_URL;
        if (subreddit != null && !subreddit.isEmpty() && !subreddit.equals("Random")) {
            url += "/" + subreddit;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Meme meme = gson.fromJson(response.toString(), Meme.class);
                        callback.onSuccess(meme);
                    } catch (Exception e) {
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                },
                error -> callback.onError("Network error: " + error.getMessage()));

        VolleySingleton.getInstance(context).addToRequestQue(jsonObjectRequest);
    }

    public void fetchMemes(String subreddit, int count, MemeListCallback callback) {
        String url = BASE_URL;
        if (subreddit != null && !subreddit.isEmpty() && !subreddit.equals("Random")) {
            url += "/" + subreddit;
        }
        url += "/" + count;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        MemeResponse memeResponse = gson.fromJson(response.toString(), MemeResponse.class);
                        callback.onSuccess(memeResponse.getMemes());
                    } catch (Exception e) {
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                },
                error -> callback.onError("Network error: " + error.getMessage()));

        VolleySingleton.getInstance(context).addToRequestQue(jsonObjectRequest);
    }
}
