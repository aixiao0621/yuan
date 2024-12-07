package az.winter.yuan

import android.content.Context

object WallpaperConfig {
    // 获取资源ID
    var resId1: Int = -1
        private set
    var resId2: Int = -1
        private set

    // 保存资源ID
    fun saveResId(context: Context?, res1: Int, res2: Int) {
        resId1 = res1
        resId2 = res2
    }
}

