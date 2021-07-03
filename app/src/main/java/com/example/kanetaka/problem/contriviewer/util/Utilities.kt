package com.example.kanetaka.problem.contriviewer.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.kanetaka.problem.contriviewer.BuildConfig

object Utilities {

    /**
     * ネットワーク通信種別
     */
    enum class NetworkType {
        TRANSPORT_WIFI,
        TRANSPORT_CELLULAR,
        TRANSPORT_OTHER,
        TRANSPORT_NONE
    }

    /**
     * 現在のネットワーク通信状況を確認する
     */
    @RequiresApi(21)
    private fun checkNetwork(context: Context): NetworkType {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 注： getNetworkCapabilities() のサポートは、API21 から。
        val capabilities: NetworkCapabilities? = cm.getNetworkCapabilities(cm.activeNetwork)
        val type = when {
            capabilities == null -> NetworkType.TRANSPORT_NONE // インターネット接続なし
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.TRANSPORT_WIFI // Wifiに接続しています
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.TRANSPORT_CELLULAR // モバイル通信に接続しています
            else -> NetworkType.TRANSPORT_OTHER // その他のネットワークに接続しています
        }
        return type
    }

    /**
     * デバッグ出力用ログ
     */
    fun debugLog(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}
