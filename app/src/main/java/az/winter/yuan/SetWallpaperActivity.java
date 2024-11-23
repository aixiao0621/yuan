package az.winter.yuan;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SetWallpaperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wallpaper);

        Button setWallpaperButton = findViewById(R.id.btn_set_wallpaper);
        setWallpaperButton.setOnClickListener(v -> setLiveWallpaper());
    }

    public void setLiveWallpaper() {
        try {
            Intent intent;
            intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(this, StartWallpaperService.class));
            startActivity(intent);
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                startActivity(intent);
//            } else {
//                Toast.makeText(this, "No app to handle live wallpaper setting.", Toast.LENGTH_SHORT).show();
//            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to set live wallpaper.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
