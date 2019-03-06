package chanboss.liveauction.database;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SessionManager {

    String userId;
    Context context;

    public SessionManager(Context context) {
        this.context = context;
    }

    public String getUserId() {
        SharedPreferences pref = context.getSharedPreferences("sessionId", MODE_PRIVATE);
        userId = pref.getString("sessionId", "");
        return userId;
    }
}
