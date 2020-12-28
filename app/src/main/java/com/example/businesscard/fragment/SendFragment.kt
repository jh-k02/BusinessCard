package com.example.businesscard.fragment

import android.app.Activity.RESULT_OK
import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.businesscard.BeamData
import com.example.businesscard.Model.UserModel
import com.example.businesscard.R
import com.example.businesscard.TagDispatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList
import java.util.HashMap
import kotlin.String.Companion as String1

class SendFragment : Fragment() {
    var tagEmail: String? = null
    var userModels: MutableList<UserModel>?= null
    var tagPosition: Int = -1       // onActivityResult에서 사용하기 위한 userModels에서 자신의 인덱스
    var uid: String?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_send, container, false)
        val recyclerView = view.findViewById<View>(R.id.fragmentsend_recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        recyclerView.adapter = SendFragmentRecyclerViewAdapter()

        return view
    }

    internal inner class SendFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        init { //DB검색
            userModels = ArrayList()
            val myUid = FirebaseAuth.getInstance().currentUser!!.uid
            uid = myUid
            FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userModels!!.clear()
                    for (snapshot in dataSnapshot.children) {
                        val userModel = snapshot.getValue(UserModel::class.java)

                        if (userModel!!.uid != myUid) { //usermodel의 uid가 내꺼면 continue
                            continue
                        }

                        userModels!!.add(userModel)
                    }
                    notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_send, parent, false)

            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //이미지 넣어줌
            Glide.with(holder.itemView.context)
                .load(userModels!![position].profileImageUrl)
                .apply(RequestOptions().circleCrop())
                .into((holder as CustomViewHolder).imageView)
            holder.name.text = userModels!![position].userName
            holder.phonenumber.text = userModels!![position].userPhonenumber
            holder.office.text = userModels!![position].userOffice
            holder.position.text = userModels!![position].userPosition
            holder.email.text = userModels!![position].userEmail

            // 보내기 버튼 클릭 이벤트
            holder.send_button.setOnClickListener {
                val intent = Intent(view.context, BeamData::class.java)
                intent.putExtra("userEmail", userModels!![position].userEmail)    // 사용자의 email 정보 전달
                startActivity(intent)
            }

            // 받기 버튼 클릭 이벤트
            holder.receive_button.setOnClickListener {
                val intent = Intent(view.context, TagDispatch::class.java)
                tagPosition = position
                startActivityForResult(intent, 3000)
            }

        }

        override fun getItemCount(): Int {
            return userModels!!.size
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView
            var name: TextView
            var phonenumber: TextView
            var office: TextView
            var position: TextView
            var email:TextView
            var send_button: Button
            var receive_button: Button


            init {
                imageView = view.findViewById<View>(R.id.send_image) as ImageView //프로필
                name = view.findViewById<View>(R.id.send_name) as TextView //이름
                phonenumber = view.findViewById<View>(R.id.send_phonenumber) as TextView //전화번호
                office = view.findViewById<View>(R.id.send_office) as TextView //직장
                position = view.findViewById<View>(R.id.send_position) as TextView //직급
                email = view.findViewById<View>(R.id.send_email) as TextView //이메일
                send_button = view.findViewById<View>(R.id.send) as Button  // 보내기 버튼
                receive_button = view.findViewById<View>(R.id.receive) as Button    // 받기 버튼
            }
        }

    }

    // TagDispatch 에서 처리된 결과를 받는 메소드
    // 처리된 결과 코드 (resultCode)가 RESULT_OK면 requestCode를 판별해 결과 처리를 진행
    // TagDispatch 에서 담겨온 이메일 정보를 firebase에 저장
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK) {
            // SendFragment 에서 요청할 때 보낸 요청코드(3000)
            if(requestCode == 3000) {
                if(tagPosition != -1) {
                    tagEmail = data!!.getStringExtra("result")  // TagDispatch 에서 보낸 친구의 이메일 정보

                    // 자신의 친구 목록이 비어있으면 별도의 동작 없이 저장
                    if(userModels!![tagPosition].userfriends == null) {
                        userModels!![tagPosition].userfriends = tagEmail
                    }

                    else {
                        val sp = userModels!![tagPosition].userfriends!!.split(" ")     // 현재 저장돼 있는 친구 이메일 정보를 리스트로 저장
                        var flag = true

                        // 저장된 친구 목록과 전달받은 친구 이메일을 비교
                        // 같은 이메일이 없는 경우 추가
                        for (index in 0..sp.size - 1) {
                            if(sp[index] == tagEmail) {
                                flag = false
                                break
                            }
                        }

                        // 친구 이메일이 추가되었으면 userModels 업데이트
                        if(flag) {
                            val text = userModels!![tagPosition].userfriends
                            userModels!![tagPosition].userfriends = "$text $tagEmail"
                        }
                    }

                    // 수정된 정보로 firebase 업데이트
                    val stringObjectMap = HashMap<String, Any>()

                    stringObjectMap["userfriends"] = userModels!![tagPosition].userfriends!!.toString()
                    FirebaseDatabase.getInstance().reference.child("users").child(uid!!).updateChildren(stringObjectMap)
                    tagPosition = -1
                }
            }
        }
    }
}