package com.example.businesscard

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.view.View
import com.example.businesscard.fragment.AccountFragment
import com.example.businesscard.fragment.PeopleFragment
import com.example.businesscard.fragment.SendFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<View>(R.id.mainactivity_bottomnavigationview) as BottomNavigationView

        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_people -> {
                    fragmentManager.beginTransaction().replace(R.id.mainactivity_framelayout, PeopleFragment()).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_send -> {
                    fragmentManager.beginTransaction().replace(R.id.mainactivity_framelayout, SendFragment()).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_account -> {
                    fragmentManager.beginTransaction().replace(R.id.mainactivity_framelayout, AccountFragment())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        passPushTokenToServer()
    }

    internal fun passPushTokenToServer() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val token = FirebaseInstanceId.getInstance().token
        val map = HashMap<String, Any>()
        map["pushToken"] = token!!

        FirebaseDatabase.getInstance().reference.child("users").child(uid).updateChildren(map)
    }

}
