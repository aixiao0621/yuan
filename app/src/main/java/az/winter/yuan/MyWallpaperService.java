package az.winter.yuan;

import android.app.KeyguardManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
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


public class MyWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    private class MyEngine extends Engine implements MediaPlayer.OnCompletionListener {

        private MediaPlayer mediaPlayer;
        private SurfaceHolder surfaceHolder;
        private boolean isLoopVideo = false;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            surfaceHolder = holder;
            setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
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

                // 检查设备是否被锁定
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
                    // 设备处于锁定状态，重置为初始视频
                    resetToInitialVideo();
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
            // Surface 创建时不做操作，在可见性改变时初始化 MediaPlayer
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
