package com.example.kanetaka.problem.contriviewer.page

/**
 * 不特定多数用の状態更新通知インターフェース。
 * 不特定先への状態更新通知インターフェースを提供します。
 * 状態には、ジェネリクスで enum class 列挙型 を指定します。
 */
interface DestinationUnspecifiedStateChangeNotifier {
    // 状態遷移開始通知
    fun updateState()
}