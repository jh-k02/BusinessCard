package com.example.businesscard


import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.businesscard.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList
import java.util.HashMap

class TagDispatch : AppCompatActivity() {
    private var textView: TextView? = null
    private var mNfcAdapter: NfcAdapter? = null
    internal var intent: Intent? = null
    private var mPendingIntent: PendingIntent? = null
    private var mIntentFilters: Array<IntentFilter>? = null
    internal var userEmail: String?= null       // NFC를 통해 태그된 친구의 email 정보 저장

    public override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(R.layout.nfc_main)
        textView = findViewById(R.id.nfc_text)

        // NFC 사용 가능/불가능 여부 체크
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mNfcAdapter != null) {
            textView!!.text = "Read an NFC tag"
        } else {
            textView!!.text = "This phone is not NFC enabled."
        }

        // PendingIntent 선언
        intent = Intent(applicationContext, TagDispatch::class.java)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        mPendingIntent = PendingIntent.getActivity(applicationContext, 0,
            intent, 0)
        val ndefIntent = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndefIntent.addDataType("*/*")
            mIntentFilters = arrayOf(ndefIntent)
        } catch (e: Exception) {
            Log.e("TagDispatch", e.toString())
        }

    }

    public override fun onResume() {
        super.onResume()
        // PendingIntent 사용 허용
        mNfcAdapter!!.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, null)
        Log.d("onResume", "onResume")
    }

    public override fun onPause() {
        super.onPause()
        // PendingIntent 사용 차단
        mNfcAdapter!!.disableForegroundDispatch(this)
    }

    // 현재 액티비티 재호출 시 발생
    public override fun onNewIntent(intent: Intent) {
        textView = findViewById(R.id.nfc_text)
        val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

        val msg = rawMsgs[0] as NdefMessage

        val strSite = String(msg.records[0].payload)
        if (strSite != null && strSite != "") {
            userEmail = strSite.substring(strSite.indexOf("userEmail") + 9, strSite.length) // NFC를 통해 태그된 친구의 email 저장
        }

        // 전송된 이메일 Toast 출력
        Toast.makeText(applicationContext, "전송된 email = $userEmail", Toast.LENGTH_LONG).show()

        // 전송된 이메일을 이전 액티비티에 전송
        val resultIntent = Intent()
        resultIntent.putExtra("result", userEmail)
        setResult(Activity.RESULT_OK, resultIntent)

        Log.d("onNewIntent", "onNewIntent")
        setIntent(intent)
        finish()
        return
    }
}