package com.example.businesscard.fragment

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.businesscard.LoginActivity
import com.example.businesscard.Model.UserModel
import com.example.businesscard.ModifyActivity
import com.example.businesscard.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.item_account.*
import java.util.ArrayList
import java.util.HashMap

class AccountFragment : Fragment() {
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        val recyclerView = view.findViewById<View>(R.id.fragmentaccount_recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        recyclerView.adapter = AccountFragmentRecyclerViewAdapter()

        return view
    }

    internal inner class AccountFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var userModels: MutableList<UserModel>
        var delete_userModels: MutableList<UserModel>   // 계정삭제 시 모든 사용자의 친구목록에서 삭제된 계정 삭제를 위한 용도

        init {
            userModels = ArrayList()
            delete_userModels = ArrayList()
            val myUid = FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
                // 데이터가 변경될 때마다 호출
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userModels.clear()
                    delete_userModels.clear()
                    for (snapshot in dataSnapshot.children) {
                        val userModel = snapshot.getValue(UserModel::class.java)

                        //usermodel의 uid가 자신의 것이 아니면 delete_userModels에 추가 후 continue
                        if (userModel!!.uid != myUid) {
                            delete_userModels.add(userModel)    // 자신을 제외한 모든 사용자 목록 생성
                            continue
                        }

                        userModels.add(userModel)   // 자신의 정보 생성
                    }
                    notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)

            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // 사용자 정보 출력
            Glide.with(holder.itemView.context)
                .load(userModels[position].profileImageUrl)
                .apply(RequestOptions().circleCrop())
                .into((holder as CustomViewHolder).imageView)
            holder.name.text = userModels[position].userName
            holder.phonenumber.text = userModels[position].userPhonenumber
            holder.office.text = userModels[position].userOffice
            holder.position.text = userModels[position].userPosition
            holder.email.text = userModels[position].userEmail

            //수정액티비티 버튼
            holder.button.setOnClickListener {
                val intent = Intent(view.context, ModifyActivity::class.java)
                intent.putExtra("userName", userModels[position].userName)
                intent.putExtra("userPhonenumber", userModels[position].userPhonenumber)
                intent.putExtra("userOffice", userModels[position].userOffice)
                intent.putExtra("userPosition", userModels[position].userPosition)
                intent.putExtra("profileImageUrl", userModels[position].profileImageUrl)
                startActivity(intent)
            }
            //로그아웃
            holder.logout_button.setOnClickListener {
                firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth!!.signOut()

                val intent = Intent(view.context, LoginActivity::class.java)
                //로그아웃 시 뒤로가기를 눌렀을때 accountFragment로 오는것을 방지
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }

            // 계정삭제
            holder.delete_button.setOnClickListener {
                val stringObjectMap = HashMap<String, Any>()
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth!!.currentUser!!.delete().addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(view.context, "아이디 삭제가 완료되었습니다", Toast.LENGTH_LONG).show()

                        // 모든 사용자의 친구목록(자신의 정보) 삭제
                        for(delete_check in 0..delete_userModels.size - 1) {
                            val stringObjectMap = HashMap<String, Any>()
                            if(delete_userModels[delete_check].userfriends != null) {
                                val sp = delete_userModels[delete_check].userfriends!!.split(" ")
                                delete_userModels[delete_check].userfriends = ""

                                for(check in 0..sp.size - 1) {
                                    if (userModels[0].userEmail != sp[check]) {
                                        if(delete_userModels[delete_check].userfriends != "")
                                            delete_userModels[delete_check].userfriends += " "
                                        delete_userModels[delete_check].userfriends = delete_userModels[delete_check].userfriends + sp[check]
                                    }
                                }

                                stringObjectMap["userfriends"] = delete_userModels[delete_check].userfriends!!.toString()
                                FirebaseDatabase.getInstance().reference.child("users").child(delete_userModels[delete_check].uid!!).updateChildren(stringObjectMap)
                            }
                        }
                        stringObjectMap["userEmail"] = account_email.text.toString() + "0" //0을 추가하여 데이터베이스에 계정이 남아있어도 중복로그인에 걸리지 않음.
                        FirebaseDatabase.getInstance().reference.child("users").child(uid).updateChildren(stringObjectMap)
                        firebaseAuth!!.signOut() //계정 삭제후 원래있던 화면에서 로그아웃을 함

                        val intent = Intent(view.context, LoginActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(view.context, "다시 로그인을 하신 후 계정을 삭제해주시기 바랍니다.", Toast.LENGTH_LONG).show()
                        firebaseAuth!!.signOut()
                        val intent = Intent(view.context, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }

            }
        }

        override fun getItemCount(): Int {
            return userModels.size
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView
            var name: TextView
            var phonenumber: TextView
            var office: TextView
            var position: TextView
            var email: TextView
            var button: Button
            var logout_button: Button
            var delete_button: Button


            init {
                imageView = view.findViewById<View>(R.id.account_image) as ImageView //프로필
                name = view.findViewById<View>(R.id.account_name) as TextView //이름
                phonenumber = view.findViewById<View>(R.id.account_phonenumber) as TextView //전화번호
                office = view.findViewById<View>(R.id.account_office) as TextView //직장
                position = view.findViewById<View>(R.id.account_position) as TextView //직급
                email = view.findViewById<View>(R.id.account_email) as TextView //이메일
                button = view.findViewById<View>(R.id.account_button) as Button //수정 버튼
                logout_button = view.findViewById<View>(R.id.accountfragment_logout) as Button //로그아웃 버튼
                delete_button = view.findViewById<View>(R.id.delete_account_button) as Button // 계정삭제 버튼

            }
        }

    }
}