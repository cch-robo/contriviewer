package com.example.kanetaka.problem.contriviewer.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.kanetaka.problem.contriviewer.BuildConfig
import com.example.kanetaka.problem.contriviewer.util.Utilities.debugLog
import java.lang.NullPointerException
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

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
    fun debugLog(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", message)
        }
    }

    /**
     * デバッグ出力用ログ(Unit Test)
     */
    fun debugTestLog(message: String) {
        if (BuildConfig.DEBUG) {
            System.out.println(message)
        }
    }

    /**
     * Instrumented test 実行中判定
     */
    fun isRunningOnTest() : Boolean {
        if (Thread.currentThread().name
                .lastIndexOf("androidx.test.runner.AndroidJUnitRunner") >= 0) return true
        return false
    }
}

/**
 * 簡易依存性注入ファクトリ
 *
 * - Instrumented test 実行時でなければ、機能しません。
 * - Instrumented test 実行時であれば、注入条件を１つ設定できます。
 * - 注入条件が満たされている場合：create()は、注入オブジェクトを返します。
 * - 注入条件が満たされていない場合：create()は、引数の value オブジェクトを返します。
 */
object SimpleFactory {
    // 注入オブジェクト
    private var injectValue: Any? = null

    // 注入オブジェクト・継承元クラス
    private var injectValueSuper: KClass<*>? = null

    // 注入オブジェクトの所有オブジェクト・クラスパス名
    private var injectTargetOwnerName: String? = null

    /**
     * オブジェクト生成
     *
     * 生成オブジェクトの型と 所有先のクラスパスについて、
     *
     * ・注入条件と一致しなければ、value を返します。
     *
     * ・注入条件と一致すれば、注入オブジェクトを返します。
     *
     * - T: 生成オブジェクト直系継承元クラス型 (インターフェース)
     * - value: 生成オブジェクト（T型を継承⇒インターフェース実装していること）
     * - owner: 生成オブジェクトの所有先オブジェクト
     */
    fun <T:Any> create(value: T, owner: Any) : T {
        //debugLog("SimpleFactory  create - value super=${value.javaClass.kotlin.superclasses[0].simpleName}, value=${value.javaClass.kotlin.simpleName}, owner=${owner.javaClass.kotlin.simpleName}")
        //debugLog("SimpleFactory  create - inject super=${injectValueSuper?.simpleName}, inject=${injectValue?.javaClass?.kotlin?.simpleName}")
        return if (injectValue == null) {
            value
        } else if (!injectTargetOwnerName.equals(owner.javaClass.kotlin.qualifiedName)) {
            value
        } else if (!injectValueSuper?.qualifiedName.equals(value.javaClass.kotlin.superclasses[0].qualifiedName)) {
            value
        } else {
            // injectValue と value は、同じ継承元を継承(同じインターフェースを実装)している。
            debugLog("SimpleFactory  create - inject - ${injectValue?.javaClass?.kotlin?.simpleName}")
            injectValue as T
        }
    }

    /**
     * 注入条件を設定します。
     *
     * （注意）Instrumented　Test スレッド下でないと条件設定がクリアされます。
     * - T: 注入オブジェクト直系継承元クラス型 (インターフェース)
     * - injectValue: 注入オブジェクト (直系継承元が、valueの直系継承元と同じであること)
     * − injectTargetOwnerName: 注入オブジェクト・所有先クラスパス
     */
    fun <T:Any> setCondition(value: T, ownerName: String) {
        debugLog("SimpleFactory  setCondition - Thread.name=${Thread.currentThread().name}")
        if (Utilities.isRunningOnTest()) {
            // Kotlin オブジェクトからクラス情報を取得するには、value.javaClass : Class<T> を使わないで下さい。
            // Kotlin オブジェクトからクラス情報を取得するには、value.javaClass.kotlin : KClass<T> を利用します。
            // Kotlin では、オブジェクトのスーパクラス(継承元)は、KClass<T>#superclasses: List<KClass<T>) を利用します。
            injectValue = value
            injectValueSuper = value.javaClass.kotlin.superclasses[0]
            injectTargetOwnerName = ownerName
            debugLog("SimpleFactory  setCondition - condition inject value=${injectValue?.javaClass?.kotlin?.simpleName}")
        } else {
            clearCondition()
            debugLog("SimpleFactory  setCondition - condition cleared")
        }
    }

    /**
     * 注入条件をクリアします。
     */
    fun clearCondition() {
        injectValue = null
        injectValueSuper = null
        injectTargetOwnerName = null
    }
}

/**
 * 値が null でないことが保証できる場合は、
 * ViewHolder#value で直接値が取得できる可変値ホルダー。
 *
 * - ValueHolder#value は not null 型だが注意が必要。
 * - isNotNull() が false なら例外が発生する。
 * - isNotNull() が true なら必ず値が返せる。
 * - 要するに昔の NullPointerException が発生する変数と同じ扱いになります。
 */
class VariableHolder<T>(private var _value: T? = null) {
    /*
    private var _value: T? = null
    */

    /**
     * 変数値を not null として扱います。
     * （null の場合は例外が発生します。）
     */
    var value: T
        get() = getValueOrRuntimeException()
        set(v) {
            _value = v
        }

    /**
     * 変数値を nullable として扱います。
     */
    var nullableValue: T?
        get() = _value
        set(v) {
            _value = v
        }

    /**
     * 変数値が not null かをチェックします。
     */
    fun isNotNull():Boolean {
        return _value != null
    }

    /**
     * 変数値が null でなければ、
     * 引数指定の処置を行います。
     */
    fun ifNotNull(operation : (v:T) -> Unit) {
        if (isNotNull()) operation(_value!!)
    }

    /**
     * 変数値が null であれば、
     * 引数指定の処置を行います。
     */
    fun ifNull(operation : () -> Unit) {
        if (!isNotNull()) operation()
    }

    /**
     * VariableHolder を破棄する。
     * 変数を破棄(null代入)します。
     */
    fun destroy() {
        _value = null
    }

    private fun getValueOrRuntimeException(): T {
        if (isNotNull()) return _value!!
        throw NullPointerException("value is null")
    }
}
