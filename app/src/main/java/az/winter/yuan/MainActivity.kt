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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.twotone.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import az.winter.yuan.ui.theme.YuanTheme
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.decode.ImageDecoderDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YuanTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    RenderCardsWithSwipe(this@MainActivity)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RenderCardsWithSwipe(context: Context) {
    // 定义 PagerState 来管理当前页
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    var expanded by remember { mutableStateOf(false) }
    var loopOnly by remember { mutableStateOf(false) }
    // 创建滑动分页
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.matchParentSize(),
            contentPadding = PaddingValues(horizontal = 32.dp),
            userScrollEnabled = true,
            pageSpacing = 16.dp
        ) { page ->
            CardContent(page = page, context = context, loopOnly)
        }

        ExtendedFloatingActionButton(
            onClick = {
                expanded = !expanded
                loopOnly = !loopOnly
            },
            modifier = Modifier
                .padding(45.dp)
                .align(Alignment.BottomCenter)
                .animateContentSize(),
            icon = {Icon( if (loopOnly)Icons.Rounded.Check else Icons.TwoTone.Clear, contentDescription = "Toggle Loop")},
            text = {
                if (expanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Loop Only")
                    }
                }
            },
            expanded = expanded
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardContent(page: Int, context: Context, loopOnly: Boolean) {
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(ImageDecoderDecoder.Factory())
        }
        .build()

    when (page) {
        0 -> {
            Column {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    onClick = {
                        setLiveWallpaper(context, loopOnly, R.raw.dai_loop, R.raw.dai_main)
                    },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberImagePainter(
                                data = R.raw.dai,
                                imageLoader = imageLoader,
                                builder = { crossfade(true) }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize(1.0f)
                                .scale(1.4f)
                        )
                    }
                }
            }
        }

        1 -> {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                onClick = {
                    setLiveWallpaper(context, loopOnly, R.raw.ru_loop, R.raw.ru_main)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = R.raw.ru,
                            imageLoader = imageLoader,
                            builder = { crossfade(true) }
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(1.0f)
                            .scale(1.4f)
                    )
                }
            }
        }
    }
}

fun setLiveWallpaper(context: Context, loopOnly: Boolean, resId1: Int, resId2: Int) {
    WallpaperConfig.saveResId(context, resId1, resId2)
    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
        putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(
                context,
                if (loopOnly) LoopWallpaperService::class.java else StartWallpaperService::class.java
            )
        )
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to set live wallpaper", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}