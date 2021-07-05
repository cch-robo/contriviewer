# Contri Viewer
Android Architecture Components samplesリポジトリの contributors を確認閲覧(viewer)する Android アプリ

## misc ディレクトリについて

- misc ディレクトリには、Contri Viewer 開発版のスナップショットと APK を配置しています。

## snapshot

- 2021/07/05 14:00  
![nexus S API23 snapshots](./misc/snapshot/snapshot_1.png)  
![pixel3a API30 snapshot 2](./misc/snapshot/snapshot_pixel-3a_API30_2.png)

## アプリについて

- 画面遷移を Fragmentを使った Navigationで管理しています。  
アプリ実態は SPA(シングルページアプリケーション)になっており、  
画面間のパラメータ受け渡しには、Safe Argsを利用しています。

- ViewModel を使って、画面表示に関する状態を管理しています。  
このため画面の縦横回転を行っても表示が維持されます。 またLiveDataを使って状態と View表示の同期を図っています

- コントリビュータ情報をリポジトリパターンで管理しています。  
ViewModelは、リポジトリからコントリビュータ情報をもらい、View表示用のモデルにコンバートします。  
GitHub API アクセスは、リポジトリ内に閉じています。

  - GitHub API アクセスは、一般的なライブラリ Retrofit と Moshi を使っています。  
    ネットワーク処理は、Coroutine を使って IO スレッド下で 非同期で実行されす。  
    また viewModelScope で実行されるため画面ライフサイクルに従属します。
  - リポジトリは、コントリビュータ情報をキャッシュします。
  - リポジトリは、アプリケーションスコープで管理されます。  
  このため Activityが破棄されてもキャッシュは残るので、画面再表示に利用できます。

- MVVMを模した、View(ViewBinding)、ViewModl、Model(ContriViewerRepository)構成になっています。  
各層間は、インターフェースを介した通知による相互強調を行っています。  
ViewBindingは 表示関係に徹し、Repositoryは、コントリビュータ情報の取得と管理に徹し、  
ViewViewViewModelは、ViewとModel(Repository)との仲介に徹します。

- その他
  - 通信エラー時は、画面に通信不能アイコン(雲にスラッシュ)を表示します。
  - 一覧画面は、スワイプダウンでコントリビュータ情報の再読込を行います。  
  キャッシュを併用しているためスワイプダウンが有効になるのは、5分後としています。

アプリでやっていないこと  
*課題の優先順位と時間制限により、以下は行っていません。*

- DI(Hiltなど)を使っていません。
- マテリアルデザインの Themeや Widget使った UI表示を行っていません。
- テストコードは、充分でありません。
- コントリビュータ情報の永続化は行っていません。  
このためアプリを終了させるとキャッシュもクリアされます。
