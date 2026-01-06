package whats.app.meme.data;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import whats.app.meme.VolleySingleton;
import whats.app.meme.model.Meme;

public class MemeRepository {
    private static final String BASE_URL = "https://meme-api.com/gimme";
    private final Context context;
    private final Gson gson;

    public interface MemeCallback {
        void onSuccess(Meme meme);
        void onError(String error);
    }

    public MemeRepository(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public void fetchMeme(String subreddit, MemeCallback callback) {
        String url = BASE_URL;
        if (subreddit != null && !subreddit.isEmpty() && !subreddit.equals("Random")) {
            url += "/" + subreddit;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Meme meme = gson.fromJson(response.toString(), Meme.class);
                            callback.onSuccess(meme);
                        } catch (Exception e) {
                            callback.onError("Parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                });

        VolleySingleton.getInstance(context).addToRequestQue(jsonObjectRequest);
    }
}
