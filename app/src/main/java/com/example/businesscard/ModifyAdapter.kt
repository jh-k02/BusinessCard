package com.example.businesscard

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.businesscard.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class ModifyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    var userModels: MutableList<UserModel>

    init {
        userModels = ArrayList()
        val myUid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userModels.clear()
                for (snapshot in dataSnapshot.children) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    if (userModel!!.uid != myUid) { //usermodel의 uid가 내꺼면 continue
                        continue
                    }
                    userModels.add(userModel)
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_modify, parent, false)

        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(userModels[position].profileImageUrl)
            .apply(RequestOptions().circleCrop())
            .into((holder as CustomViewHolder).imageView)
        holder.name.setText(userModels[position].userName)
        holder.phonenumber.setText(userModels[position].userPhonenumber)
        holder.office.setText(userModels[position].userOffice)
        holder.position.setText(userModels[position].userPosition)
    }

    override fun getItemCount(): Int {
        return userModels.size
    }

    private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView
        var name: EditText
        var phonenumber: EditText
        var office: EditText
        var position: EditText

        init {
            imageView = view.findViewById<View>(R.id.modify_image) as ImageView //프로필
            name = view.findViewById<View>(R.id.modify_name) as EditText //이름
            phonenumber = view.findViewById<View>(R.id.modify_phonenumber) as EditText //전화번호
            office = view.findViewById<View>(R.id.modify_office) as EditText //직장
            position = view.findViewById<View>(R.id.modify_position) as EditText //직급

        }
    }

}