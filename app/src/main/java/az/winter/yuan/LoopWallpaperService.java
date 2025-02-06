package az.winter.yuan;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.IOException;

public class LoopWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        // 将 Service 的 Context 传递给 Engine
        return new LoopEngine(this);
    }

    private class LoopEngine extends Engine implements MediaPlayer.OnCompletionListener {
        private MediaPlayer mediaPlayer;
        private SurfaceHolder surfaceHolder;
        private DisplayManager.DisplayListener displayListener;
        private int videoResId;
        private final Context serviceContext; // 保存 Service 的 Context

        public LoopEngine(Context serviceContext) {
            this.serviceContext = serviceContext;
            // 首次创建 Engine 时，先读取一次资源 ID
            videoResId = WallpaperConfig.INSTANCE.getResId1(serviceContext);
        }
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            surfaceHolder = holder;
            setTouchEventsEnabled(true);

            // 注册 DisplayManager 的屏幕状态监听器
            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            displayListener = new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                    // 不需要处理
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                    // 不需要处理
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    Display display = displayManager.getDisplay(displayId);
                    if (display != null && display.getState() == Display.STATE_OFF) {
                        // 屏幕关闭时重置视频
                        pauseVideo();
                    }
                }
            };

            displayManager.registerDisplayListener(displayListener, null);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            // 注销 DisplayListener
            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            if (displayManager != null && displayListener != null) {
                displayManager.unregisterDisplayListener(displayListener);
            }

            releaseMediaPlayer();
        }

        private void pauseVideo() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                // 重新从 WallpaperConfig 读取最新的资源 ID
                int newVideoResId = WallpaperConfig.INSTANCE.getResId1(serviceContext);
                if (newVideoResId != videoResId) { // 如果资源 ID 发生变化
                    videoResId = newVideoResId; // 更新 videoResId
                    releaseMediaPlayer(); // 释放旧的 MediaPlayer
                    initMediaPlayer(videoResId); // 使用新的资源 ID 初始化 MediaPlayer
                } else {
                    if (mediaPlayer == null) {
                        initMediaPlayer(videoResId);
                    } else {
                        mediaPlayer.start();
                    }
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

        private void initMediaPlayer(int resId) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setSurface(surfaceHolder.getSurface());

                AssetFileDescriptor afd = getResources().openRawResourceFd(resId);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                mediaPlayer.setLooping(true);
                mediaPlayer.setOnCompletionListener(this);

                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
            } catch (IOException e) {
                e.printStackTrace();
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
//            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                if (mediaPlayer != null) {
//                    if (mediaPlayer.isPlaying()) {
//                        mediaPlayer.pause();
//                    } else {
//                        mediaPlayer.start();
//                    }
//                }
//            }
            super.onTouchEvent(event);
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
        }
    }
}
