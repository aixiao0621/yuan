package az.winter.yuan;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.io.IOException;

public class StartWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        int loopResId = WallpaperConfig.INSTANCE.getResId1();
        int mainResId = WallpaperConfig.INSTANCE.getResId2();
        return new MyEngine(loopResId, mainResId);
    }

    private class MyEngine extends Engine implements MediaPlayer.OnCompletionListener {
        private MediaPlayer mediaPlayer;
        private SurfaceHolder surfaceHolder;
        private boolean isLoopVideo = false;
        private DisplayManager.DisplayListener displayListener;
        private final int loopResId;
        private final int mainResId;
        public MyEngine(int loopResId, int mainResId){
            this.loopResId = loopResId;
            this.mainResId = mainResId;
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
                        resetToInitialVideo();
                    }else if (display != null && display.getState() == Display.STATE_ON) {
                        // 屏幕重新打开时恢复视频播放
                        mediaPlayer.start();
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

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                if (mediaPlayer == null) {
                    initMediaPlayer(loopResId, mainResId);
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

        private void initMediaPlayer(int loopResId, int mainResId) {
            if (mediaPlayer == null) {
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setSurface(surfaceHolder.getSurface());

                    int videoResId = isLoopVideo ? loopResId : mainResId;
                    AssetFileDescriptor afd = getResources().openRawResourceFd(videoResId);
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();

                    mediaPlayer.setLooping(isLoopVideo);
                    mediaPlayer.setOnCompletionListener(this);

                    mediaPlayer.prepareAsync();
//                mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void resetToInitialVideo() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            isLoopVideo = false;
            initMediaPlayer(loopResId, mainResId);
        }

        private void resetToLoopVideo() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();

                try {
                    AssetFileDescriptor afd = getResources().openRawResourceFd(loopResId);
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
            if (!isLoopVideo) {
                isLoopVideo = true;
                resetToLoopVideo();
            }
        }
    }
}
