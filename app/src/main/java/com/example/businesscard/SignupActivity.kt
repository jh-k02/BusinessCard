//회원가입
package com.example.businesscard

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.businesscard.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {
    private var name: EditText? = null
    private var phonenumber: EditText? = null
    private var office: EditText? = null
    private var email: EditText? = null
    private var password: EditText? = null
    private var position: EditText? = null
    private var signup: Button? = null
    private var profile: ImageView? = null
    private var imageUri: Uri? = null //업로드할때 uri를 담아주는것

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val main_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(main_background)
        }

        profile = findViewById<View>(R.id.signup_image) as ImageView
        //사진을 클릭하면 앨범이 열림
        profile!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, PICK_FROM_ALBUM)
            //결과값은 onActivityResult로
        }

        name = findViewById<View>(R.id.signup_name) as EditText
        phonenumber = findViewById<View>(R.id.signup_phonenumber) as EditText
        office = findViewById<View>(R.id.signup_office) as EditText
        email = findViewById<View>(R.id.signup_email) as EditText
        password = findViewById<View>(R.id.signup_password) as EditText
        position = findViewById<View>(R.id.signup_position) as EditText
        signup = findViewById<View>(R.id.signup_button) as Button

        // 등록된 모든 정보를 불러옴
        var userModels: MutableList<UserModel> = ArrayList()
        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userModels.clear()
                for (snapshot in dataSnapshot.children) {
                    val userModel = snapshot.getValue(UserModel::class.java)

                    if (userModel != null) {
                        userModels.add(userModel)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        //하나라도 입력을 안하면 생성하지 않음
        signup!!.setOnClickListener(View.OnClickListener {
            if (email!!.text.toString() == null || password!!.text.toString() == null ||name!!.text.toString() == null || phonenumber!!.text.toString() == null || office!!.text.toString() == null || position!!.text.toString() == null || imageUri == null) {
                Toast.makeText(this, "모든 칸을 채워주시기 바랍니다.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            // 이메일 중복 체크
            var equal_flag = true   // 입력한 이메일과 중복되는 이메일 존재 여부 확인 용도
            for(equal_check in 0..userModels.size - 1) {
                // 중복된 이메일이 있으면 equal_flag를 false로 변경하고 중복된 아이디가 있음을 알림
                if(userModels[equal_check].userEmail == email!!.text.toString()) {
                    Toast.makeText(this, "중복된 아이디가 있습니다.", Toast.LENGTH_LONG).show()
                    equal_flag = false
                    break
                }
            }

            // 이메일 형식 체크
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email!!.text.toString()).matches()) {
                Toast.makeText(this, "이메일 형식이 아닙니다.", Toast.LENGTH_LONG).show()
                equal_flag = false
            }

            // 전화번호 유효성 체크
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", phonenumber!!.text.toString())) {
                Toast.makeText(this, "전화번호 형식이 아닙니다.", Toast.LENGTH_LONG).show()
                equal_flag = false
            }

            // 이메일이 중복되지 않았을 경우 실행
            if(equal_flag) {
                FirebaseAuth.getInstance() // 파이어베이스 연동
                    .createUserWithEmailAndPassword(email!!.text.toString(), password!!.text.toString())
                    .addOnCompleteListener(this@SignupActivity) { task ->
                        val uid = task.result!!.user.uid  //데이터베이스에
                        FirebaseStorage.getInstance().reference.child("userImages").child(uid).putFile(imageUri!!)
                            .addOnCompleteListener { task ->
                                val imageUrl = task.result!!.storage.downloadUrl //파일 저장된 경로를 다시 보내줌
                                while (!imageUrl.isComplete);

                                val userModel = UserModel()
                                userModel.userName = name!!.text.toString()
                                userModel.profileImageUrl = imageUrl.result!!.toString()
                                userModel.userPhonenumber = phonenumber!!.text.toString()
                                userModel.userOffice = office!!.text.toString()
                                userModel.userEmail = email!!.text.toString()
                                userModel.userPassword = password!!.text.toString()
                                userModel.userPosition = position!!.text.toString()
                                userModel.uid = FirebaseAuth.getInstance().currentUser!!.uid //회원가입을 할 때마다 생김
                                                                                                    //uid안에 이름을 넣어준다고 생각(userModel을 넣어준다고)
                                FirebaseDatabase.getInstance().reference.child("users").child(uid).setValue(userModel)
                                    .addOnSuccessListener {
                                        this@SignupActivity.finish() //데이터베이스안에 들어가면 회원가입 창 닫힘
                                    }
                            }
                    }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //결과값이 넘어옴
        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK) { //결과값을 요청한 사람이 누구인지 판단
            profile!!.setImageURI(data!!.data) //가운데 뷰(사진들어가는 부분)를 바꿈. profile에 이미지 setting
            imageUri = data.data //이미지 경로 저장 원본을 가지고 있음.
        }
    }

    companion object {

        val PICK_FROM_ALBUM = 10
    }

}