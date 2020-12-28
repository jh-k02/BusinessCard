//수정
package com.example.businesscard

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import java.util.HashMap
import kotlinx.android.synthetic.main.item_modify.*
import kotlinx.android.synthetic.main.item_modify.view.*
import kotlinx.android.synthetic.main.modify.*

class ModifyActivity : AppCompatActivity() {
    private var profile: ImageView? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modify)

        val recyclerView = findViewById<View>(R.id.modify_recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ModifyAdapter()

        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val main_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(main_background)
        }

        profile = findViewById<View>(R.id.modify_after_image) as? ImageView
        //프로필을 누르면 앨범으로
        profile!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, PICK_FROM_ALBUM)
            //onActivityResult로 이동
        }

        //하나라도 입력을 안하면 생성하지 않음 (일단 기본적으로 지금 현재의 정보들이 들어가있음.)
        modify_button?.setOnClickListener(View.OnClickListener {
            if (recyclerView.modify_name.text  == null || recyclerView.modify_phonenumber.text  == null || recyclerView.modify_office.text  == null || recyclerView.modify_position.text == null) {// || imageUri == null) {
                return@OnClickListener
            }
            val stringObjectMap = HashMap<String, Any>()
            val uid = FirebaseAuth.getInstance().currentUser!!.uid

            if(imageUri != null) { //imageUri을 변경할 경우
                FirebaseStorage.getInstance().reference.child("userImages").child(uid).putFile(imageUri!!)
                    .addOnCompleteListener { task ->
                        val ImageUrl = task.result!!.storage.downloadUrl
                        while (!ImageUrl.isComplete);

                        stringObjectMap["userName"] = modify_name.text.toString()
                        stringObjectMap["userPhonenumber"] = modify_phonenumber.text.toString()
                        stringObjectMap["userOffice"] = modify_office.text.toString()
                        stringObjectMap["userPosition"] = modify_position.text.toString()
                        stringObjectMap["profileImageUrl"] = ImageUrl.result!!.toString()
                        FirebaseDatabase.getInstance().reference.child("users").child(uid)
                            .updateChildren(stringObjectMap)
                            .addOnSuccessListener {
                                this@ModifyActivity.finish() //변하면 수정 창 닫음
                            }
                    }
            }

            else { //imageUri 변경 안했을 경우
                stringObjectMap["userName"] = modify_name.text.toString()
                stringObjectMap["userPhonenumber"] = modify_phonenumber.text.toString()
                stringObjectMap["userOffice"] = modify_office.text.toString()
                stringObjectMap["userPosition"] = modify_position.text.toString()
                FirebaseDatabase.getInstance().reference.child("users").child(uid)
                    .updateChildren(stringObjectMap)
                    .addOnSuccessListener {
                        this@ModifyActivity.finish() //변하면 수정 창 닫음
                    }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            profile!!.setImageURI(data!!.data)
            imageUri = data.data
        }
    }

    companion object {

        val PICK_FROM_ALBUM = 10
    }
}


