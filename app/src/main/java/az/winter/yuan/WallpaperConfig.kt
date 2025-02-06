package az.winter.yuan

import android.content.Context

object WallpaperConfig {
    private const val PREF_NAME = "wallpaper_config"
    private const val KEY_RES_ID1 = "res_id_1"
    private const val KEY_RES_ID2 = "res_id_2"

    // 保存资源ID
    fun saveResId(context: Context?, resId1: Int, resId2: Int) {
        val prefs = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return
        prefs.edit()
            .putInt(KEY_RES_ID1, resId1)
            .putInt(KEY_RES_ID2, resId2)
            .apply()
    }

    // 获取资源ID 1
    fun getResId1(context: Context?): Int {
        val prefs = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return -1
        return prefs.getInt(KEY_RES_ID1, -1)
    }

    // 获取资源ID 2
    fun getResId2(context: Context?): Int {
        val prefs = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return -1
        return prefs.getInt(KEY_RES_ID2, -1)
    }
}