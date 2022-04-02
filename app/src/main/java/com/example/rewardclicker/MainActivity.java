package com.example.rewardclicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.games.AuthenticationResult;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.tasks.Task;
//import com.example.rewardclicker.databinding.ActivityFullscreenBinding;

public class MainActivity extends AppCompatActivity {
    public static String TAG = MainActivity.class.getName();

    private static final int RC_LEADERBOARD_UI = 9004;

    private static final String APP_PREF = "app_pref";
    private static final String KEY_RECORD = "key_record";

    private SharedPreferences sharedPreferences;
    private int record;
    TextView fullscreenContent;
    Button satisfyButton, leaderButton, signIn;
    //private ActivityFullscreenBinding binding;

    @Nullable
    private LeaderboardsClient leaderboardsClient() {
        return PlayGames.getLeaderboardsClient(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayGamesSdk.initialize(this);

        //binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);
        fullscreenContent = findViewById(R.id.fullscreen_content);
        satisfyButton = findViewById(R.id.satisfy_button);
        leaderButton = findViewById(R.id.leader_button);
        signIn = findViewById(R.id.sign_in);

        sharedPreferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        record = sharedPreferences.getInt(KEY_RECORD, 0);

        satisfyButton.setOnClickListener(view -> clickReward());
        leaderButton.setOnClickListener(view -> showLeaderboard());
        signIn.setOnClickListener(view -> startSignIn());

        updateRecordUI();

        GamesSignInClient signInClient = PlayGames.getGamesSignInClient(this);

        signInClient.isAuthenticated().addOnCompleteListener(task -> {
            boolean isAuthenticated =
                    (task.isSuccessful() &&
                            task.getResult().isAuthenticated());

            if (isAuthenticated) {
                Toast.makeText(this, R.string.success_sign, Toast.LENGTH_LONG).show();
            } else {
                //Блокируем
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences
                .edit()
                .putInt(KEY_RECORD, record).apply();
    }

    private void updateRecordUI() {

        fullscreenContent.setText(String.valueOf(record));
    }

    private void clickReward() {
        record++;
        updateRecordUI();
        PlayGames.getLeaderboardsClient(this)
                .submitScore(getString(R.string.leaderboard_id), record);
    }

    private void showLeaderboard() {
        PlayGames.getLeaderboardsClient(this)
                .getLeaderboardIntent(getString(R.string.leaderboard_id))
                .addOnSuccessListener(intent -> startActivityForResult(intent, RC_LEADERBOARD_UI));
    }

    private void startSignIn() {
        PlayGames.getGamesSignInClient(this).signIn().addOnCompleteListener(task ->
                Toast.makeText(
                        this,
                        task.isSuccessful() && task.getResult().isAuthenticated() ?
                                R.string.success_sign : R.string.error_signin,
                        Toast.LENGTH_LONG).show());
    }
}