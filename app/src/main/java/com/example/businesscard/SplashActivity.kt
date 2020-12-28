//첫화면 배경
package com.example.businesscard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.LinearLayout
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class SplashActivity: AppCompatActivity() {

    private var linearLayout: LinearLayout? = null
    private var mfirebaseRemoteConfig: FirebaseRemoteConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //상태바 제거

        linearLayout = findViewById(R.id.splash_linearlayout)
        //초기화
        mfirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build() // DEBUG 안해주면 요청에 제한이 있음.
        mfirebaseRemoteConfig!!.setConfigSettings(configSettings)
        mfirebaseRemoteConfig!!.setDefaults(R.xml.default_config)
        //서버값을 덮어씌우는 부분
        mfirebaseRemoteConfig!!.fetch(0)  //시간 얼마마다 요청하게 되는것, 서버값이 있으면 서버값으로 채움
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    mfirebaseRemoteConfig!!.activateFetched()
                } else {
                }
                displayMessage()
            }
    }

    internal fun displayMessage() {
        val main_backgound = mfirebaseRemoteConfig!!.getString("main_background")
        val caps = mfirebaseRemoteConfig!!.getBoolean("main_message_caps")
        val main_message = mfirebaseRemoteConfig!!.getString("main_message")

        linearLayout!!.setBackgroundColor(Color.parseColor(main_backgound))

        if (caps) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(main_message).setPositiveButton("확인") { dialog, which -> finish() }
            builder.create().show() //확인을 누르면 앱이 꺼짐
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()  //splash가 밑에 남아있지 않도록 자기도 꺼짐
        }

    }
}
