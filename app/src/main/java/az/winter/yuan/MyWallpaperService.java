package az.winter.yuan;

import android.app.KeyguardManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.io.IOException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    private class MyEngine extends Engine implements MediaPlayer.OnCompletionListener {

        private MediaPlayer mediaPlayer;
        private SurfaceHolder surfaceHolder;
        private boolean isLoopVideo = false;

        private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    // 屏幕关闭，重置视频
                    resetToInitialVideo();
                    Log.d("MyWallpaperService", "Screen off, reset video.");
                    Toast.makeText(MyWallpaperService.this, "Screen off, reset video.", Toast.LENGTH_LONG).show();
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            surfaceHolder = holder;
            setTouchEventsEnabled(true);

            // 注册屏幕关闭的广播接收器
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screenOffReceiver, filter);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(screenOffReceiver);
            releaseMediaPlayer();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                if (mediaPlayer == null) {
                    initMediaPlayer();
                } else {
                    mediaPlayer.start();
                }
            } else {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            // Surface 创建时不做操作，等待可见时初始化 MediaPlayer
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            releaseMediaPlayer();
        }

        private void initMediaPlayer() {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setSurface(surfaceHolder.getSurface());

                int videoResId = isLoopVideo ? R.raw.loop : R.raw.main;
                AssetFileDescriptor afd = getResources().openRawResourceFd(videoResId);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                mediaPlayer.setLooping(isLoopVideo);
                mediaPlayer.setOnCompletionListener(this);

                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void resetToInitialVideo() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            isLoopVideo = false;
            initMediaPlayer();
        }

        private void resetToLoopVideo() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();

                try {
                    int videoResId = R.raw.loop;
                    AssetFileDescriptor afd = getResources().openRawResourceFd(videoResId);
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();

                    mediaPlayer.setLooping(true);

                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void releaseMediaPlayer() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            // 可根据需要处理触摸事件，例如暂停/播放视频
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                }
            }
            super.onTouchEvent(event);
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (!isLoopVideo) {
                isLoopVideo = true;
                resetToLoopVideo();
            }
        }
    }
}
