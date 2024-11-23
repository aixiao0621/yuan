package az.winter.yuan

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import az.winter.yuan.ui.theme.YuanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YuanTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { paddingValues ->
                        Column {
                            Text(
                            text = "Hello World",
                            modifier = Modifier.padding(paddingValues)
                            )
                            Button(onClick = { setLiveWallpaper(this@MainActivity) }) {
                                Text(text = "Button")
                            }
                        }
                    }
                )
            }
        }
    }
}
fun setLiveWallpaper(context: Context) {
    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
        putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, MyWallpaperService::class.java)
        )
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to set live wallpaper.", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}