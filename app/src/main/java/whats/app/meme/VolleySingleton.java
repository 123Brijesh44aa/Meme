package whats.app.meme;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    private  Context context;
    private static VolleySingleton INSTANCE;
    private  RequestQueue requestQueue;

    private VolleySingleton(Context context){
        this.context=context;
        this.requestQueue=getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue==null){
            requestQueue= Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized VolleySingleton getInstance(Context context){
        if (INSTANCE==null){
            INSTANCE=new VolleySingleton(context);
        }
        return INSTANCE;
    }

    public<T> void addToRequestQue(Request<T> request){
        requestQueue.add(request);

    }

}
