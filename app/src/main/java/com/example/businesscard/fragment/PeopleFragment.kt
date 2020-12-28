//지인 목록(명함 주고받은 사람)
package com.example.businesscard.fragment

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.businesscard.Model.UserModel
import com.example.businesscard.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_people.*
import kotlinx.android.synthetic.main.item_friend.view.*
import java.util.ArrayList
import java.util.HashMap

class PeopleFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_people, container, false)
        val recyclerView = view.findViewById<View>(R.id.fragmentpeople_recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        recyclerView.adapter = PeopleFragmentRecyclerViewAdapter()

        return view
    }

    internal inner class PeopleFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var userModels: MutableList<UserModel>  // RecyclerView와 연동되는 userModel 리스트
        var userList: MutableList<UserModel>    // 검색 기능을 사용할 때 친구 목록이 초기화되는 것에 대비하여 친구 목록을 백업해두는 용도
        var holder_userModels: UserModel? = null

        init { //DB검색
            userModels = ArrayList()
            userList = ArrayList()
            val myUid = FirebaseAuth.getInstance().currentUser!!.uid
            var index = 0
            var count_flag = true   // 공백을 제외한 검색 입력 여부 체크

            FirebaseDatabase.getInstance().reference.child("users").orderByChild("userName").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userModels.clear()
                    for (snapshot in dataSnapshot.children) {
                        val userModel = snapshot.getValue(UserModel::class.java)

                        // userModel의 uid가 자신의 것이면 index에 UserModels에 정보가 들어갈 인덱스 저장
                        if (userModel!!.uid == myUid) {
                            index = userModels.size
                            holder_userModels = userModel
                        }

                        userModels.add(userModel)
                    }

                    var sp: List<String>? = null

                    // 자신의 친구목록이 비어있다면 0명 표기
                    if((userModels[index].userfriends == null) || (userModels[index].userfriends == "")) {
                        if(count_flag) {
                            people_num.text = "0명"
                            count_flag = false
                        }
                    }

                    // 현재 자신의 친구목록이 비어있지 않은 경우 String으로 저장된 친구목록을 List로 저장
                    if((userModels[index].userfriends != null) && (userModels[index].userfriends != "")){
                        sp = userModels[index].userfriends!!.split(" ")

                        // 친구 수 표기
                        if(count_flag) {
                            people_num.text = sp.size.toString() + "명"
                            count_flag = false
                        }
                    }

                    // userModels 초기화
                    userModels.clear()

                    for (snapshot in dataSnapshot.children) {
                        val userModel = snapshot.getValue(UserModel::class.java)

                        if (userModel!!.uid == myUid) { //usermodel의 uid가 내꺼면 continue
                            continue
                        }

                        if (sp != null) {
                            for (check in 0..sp.size - 1) {
                                // userModel의 이메일이 현재 자신의 친구목록에 존재한다면 추가
                                if (userModel.userEmail == sp[check]) {
                                    userModels.add(userModel)
                                    userList.add(userModel)
                                }
                            }
                        }

                    }
                    notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)

            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //이미지 넣어줌
            Glide.with(holder.itemView.context)
                .load(userModels[position].profileImageUrl)
                .apply(RequestOptions().circleCrop())
                .into((holder as CustomViewHolder).imageView)
            holder.name.text = userModels[position].userName
            holder.phonenumber.text = userModels[position].userPhonenumber
            holder.office.text = userModels[position].userOffice
            holder.position.text = userModels[position].userPosition
            holder.email.text = userModels[position].userEmail

            //친구삭제 버튼 클릭 이벤트
            holder.deleteBtn.setOnClickListener {
                val sp = holder_userModels!!.userfriends!!.split(" ")

                // 친구 목록 초기화
                holder_userModels!!.userfriends = ""
                for(check in 0..sp.size - 1) {
                    if(holder.email.text != sp[check]) {
                        if(holder_userModels!!.userfriends != "")
                            holder_userModels!!.userfriends += " "
                        holder_userModels!!.userfriends = holder_userModels!!.userfriends + sp[check]
                    }
                }

                // 친구 수 변경
                val friends_check = holder_userModels!!.userfriends!!.split(" ")
                people_num.text = friends_check.size.toString() + "명"
                if(holder_userModels!!.userfriends == "")
                    people_num.text = "0명"

                val stringObjectMap = HashMap<String, Any>()

                stringObjectMap["userfriends"] = holder_userModels!!.userfriends!!.toString()
                FirebaseDatabase.getInstance().reference.child("users").child(holder_userModels!!.uid!!).updateChildren(stringObjectMap)

                notifyDataSetChanged()
            }

            // 검색 버튼 클릭 이벤트
            people_search_button.setOnClickListener {
                userModels.clear()  // userModels 초기화
                var search_fail = true      // 검색한 이름과 동일한 이름의 친구 존재 여부 확인

                // 검색하는 단어가 공백이 아닐 경우 검색한 친구 목록 탐색
                // 검색한 이름과 같은 친구가 없다면 빈 목록 출력
                if(people_search.text.toString() != "") {
                    for(check in 0..userList.size - 1) {
                        if (userList[check].userName == people_search.text.toString()) {
                            search_fail = false
                            userModels.add(userList[check])
                            break
                        }
                    }

                    // 검색한 이름과 동일한 이름의 친구가 존재하지 않는다면 Toast 출력
                    if(search_fail) {
                        Toast.makeText(view.context, "검색한 이름과 동일한 이름의 친구가 없습니다.", Toast.LENGTH_LONG).show()
                    }
                }

                // 검색하는 단어가 공백이라면 userModels를 userList로 초기화
                else {
                    for(check in 0..userList.size - 1) {
                        userModels.add(userList[check])
                    }
                }

                notifyDataSetChanged()
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
            var email:TextView
            var deleteBtn: ImageButton

            init {
                imageView = view.findViewById<View>(R.id.frienditem_image) as ImageView //프로필
                name = view.findViewById<View>(R.id.frienditem_name) as TextView //이름
                phonenumber = view.findViewById<View>(R.id.frienditem_phonenumber) as TextView //전화번호
                office = view.findViewById<View>(R.id.frienditem_office) as TextView //직장
                position = view.findViewById<View>(R.id.frienditem_position) as TextView //직급
                email = view.findViewById<View>(R.id.frienditem_email) as TextView //이메일
                deleteBtn = view.findViewById<View>(R.id.delete_button) as ImageButton   // 친구 삭제 버튼
            }
        }
    }
}