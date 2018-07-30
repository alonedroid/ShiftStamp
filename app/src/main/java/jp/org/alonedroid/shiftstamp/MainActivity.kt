package jp.org.alonedroid.shiftstamp

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import jp.org.alonedroid.shiftstamp.feature.calendar.CalendarFragment
import jp.org.alonedroid.shiftstamp.feature.stamp.StampFragment
import jp.org.alonedroid.shiftstamp.util.CalendarInfoUtil
import jp.org.alonedroid.shiftstamp.util.SpUtil
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


/**
 * 残タスク
 * ・設定画面作成
 * ・設定モードの時にフリーと削除を無効にする
 * ・連携済みアカウントの表示＆変更対応
 * ・Googleカレンダーの共有ページへのリンクを追加
 */
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }

    private lateinit var credential: GoogleAccountCredential

    /**
     * Activityを作成する。
     *
     * @param savedInstanceState 以前に保存されたインスタンスのデータ
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ActivityにViewを設定する
        setContentView(R.layout.activity_main)

        prepareLogdingDialog()
        checkStatus()
    }

    private fun prepareLogdingDialog() {
        val progress = findViewById<FrameLayout>(R.id.main_loading)
        ViewModelProviders.of(this).get(MainViewModel::class.java)
                .loading.observe(this, Observer { isLoading ->
            progress.visibility = if (isLoading!!) View.VISIBLE else View.GONE
        })
    }

    private fun checkStatus() {
        credential = CalendarInfoUtil.getCredential(this)

        if (!isGooglePlayServicesAvailable()) {
            // Google Play Services が無効な場合
            acquireGooglePlayServices()
        } else if (credential.selectedAccountName == null) {
            // 有効な Google アカウントが選択されていない場合
            chooseAccount()
        } else if (!isDeviceOnline()) {
            // 端末がインターネットに接続されていない場合
            Toast.makeText(this, "ネットワークに接続出来ませんでした", Toast.LENGTH_LONG).show()
            finish()
        } else {
            startCalendar()
        }
    }

    private fun startCalendar() {
        val calendarFragment = CalendarFragment.newInstance()
        val stampFragment = StampFragment.newInstance()

        supportFragmentManager.beginTransaction()
                .add(R.id.main_frame1, calendarFragment)
                .add(R.id.main_frame2, stampFragment)
                .commit()
    }

    /**
     * 端末に Google Play Services がインストールされ、アップデートされているか否かを確認する。
     *
     * @return 利用可能な Google Play Services がインストールされ、アップデートされている場合にはtrueを、
     * そうでない場合にはfalseを返す。
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * ユーザーにダイアログを表示して、Google Play Services を利用可能な状態に設定するように促す。
     * ただし、ユーザーが解決できないようなエラーの場合には、ダイアログを表示しない。
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * 有効な Google Play Services が見つからないことをエラーダイアログで表示する。
     *
     * @param connectionStatusCode Google Play Services が無効であることを示すコード
     */
    fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }

    /**
     * Google　Calendar API の認証情報を使用するGoogleアカウントを設定する。
     *
     * 既にGoogleアカウント名が保存されていればそれを使用し、保存されていなければ、
     * Googleアカウントの選択ダイアログを表示する。
     *
     * 認証情報を用いたGoogleアカウントの設定には、"GET_ACCOUNTS"パーミッションを
     * 必要とするため、必要に応じてユーザーに"GET_ACCOUNTS"パーミッションを要求する
     * ダイアログが表示する。
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        // "GET_ACCOUNTS"パーミッションを取得済みか確認する
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            // SharedPreferencesから保存済みGoogleアカウントを取得する
            val accountName = SpUtil.ACCOUNT_NAME.getString(this, "")

            if (TextUtils.isEmpty(accountName)) {
                // Googleアカウントの選択を表示する
                // GoogleAccountCredentialのアカウント選択画面を使用する
                startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // ダイアログを表示して、ユーザーに"GET_ACCOUNTS"パーミッションを要求する
            EasyPermissions.requestPermissions(
                    this,
                    "Googleカレンダーと同期するために必要な権限の取得",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    /**
     * 現在、端末がネットワークに接続されているかを確認する。
     *
     * @return ネットワークに接続されている場合にはtrueを、そうでない場合にはfalseを返す。
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    /**
     * アカウント選択や認証など、呼び出し先のActivityから戻ってきた際に呼び出される。
     *
     * @param requestCode Activityの呼び出し時に指定したコード
     * @param resultCode  呼び出し先のActivityでの処理結果を表すコード
     * @param data        呼び出し先のActivityでの処理結果のデータ
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "アプリを起動出来ませんでした", Toast.LENGTH_LONG).show()
            finish()
        }

        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> {
                checkStatus()
                return
            }

            REQUEST_ACCOUNT_PICKER -> if (data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                accountName?.let { SpUtil.ACCOUNT_NAME.putString(this, it) }
                checkStatus()
                return
            }

            REQUEST_AUTHORIZATION -> {
                checkStatus()
                return
            }
        }

        Toast.makeText(this, "アプリを起動出来ませんでした", Toast.LENGTH_LONG).show()
        finish()
    }

    /**
     * Android 6.0 (API 23) 以降にて、実行時にパーミッションを要求した際の結果を受け取る。
     *
     * @param requestCode  requestPermissions(android.app.Activity, String, int, String[])
     * を呼び出した際に渡した　request code
     * @param permissions  要求したパーミッションの一覧
     * @param grantResults 要求したパーミッションに対する承諾結果の配列
     * PERMISSION_GRANTED または PERMISSION_DENIED　が格納される。
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * 要求したパーミッションがユーザーに承諾された際に、EasyPermissionsライブラリから呼び出される。
     *
     * @param requestCode 要求したパーミッションに関連した request code
     * @param list        要求したパーミッションのリスト
     */
    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // 何もしない
    }

    /**
     * 要求したパーミッションがユーザーに拒否された際に、EasyPermissionsライブラリから呼び出される。
     *
     * @param requestCode 要求したパーミッションに関連した request code
     * @param list        要求したパーミッションのリスト
     */
    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        Toast.makeText(this, "アプリを起動出来ませんでした", Toast.LENGTH_LONG).show()
        finish()
    }
}
