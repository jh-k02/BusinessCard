// 핸드폰 두개로 테스트를 하려고 로그인 기능 생성
package com.example.businesscard

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class LoginActivity: AppCompatActivity() {

    private var id: EditText? = null
    private var password: EditText? = null

    private var login: Button? = null
    private var signup: Button? = null
    private var firebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null //로그인 성공 후 다음으로 넘어가게 즉 로그인 성공유무 확인

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val main_background = firebaseRemoteConfig!!.getString(getString(R.string.rc_color))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(main_background)
        }

        id = findViewById<View>(R.id.login_id) as EditText
        password = findViewById<View>(R.id.login_password) as EditText
        login = findViewById<View>(R.id.login_login_button) as Button
        signup = findViewById<View>(R.id.login_signup_button) as Button
        login!!.setBackgroundColor(Color.parseColor(main_background))
        signup!!.setBackgroundColor(Color.parseColor(main_background))

        login!!.setOnClickListener { loginEvent() }

        signup!!.setOnClickListener { startActivity(Intent(this@LoginActivity, SignupActivity::class.java)) } //회원가입

        //로그인 인터페이스 리스너 로그인이 되었는지 확인
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            //상태가 바뀌었을때 (로그인이 되었거나 안되었거나)
            val user = firebaseAuth.currentUser
            if (user != null) { //로그인
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish() //main액티비티 열기
            } else { //로그인x
            }
        }
    }

    internal fun loginEvent() {
        if(id!!.text.toString() == "" || password!!.text.toString() == ""){
            Toast.makeText(this@LoginActivity, "로그인을 할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } //아이디나 비밀번호 중 하나라도 안치면 로그인을 할 수 없음.(안넣으면 앱이 꺼짐)
        else {
            firebaseAuth!!.signInWithEmailAndPassword(id!!.text.toString(), password!!.text.toString())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this@LoginActivity, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth!!.addAuthStateListener(authStateListener!!) //로그인 액티비티에 붙여줌
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth!!.removeAuthStateListener(authStateListener!!)
    }
}