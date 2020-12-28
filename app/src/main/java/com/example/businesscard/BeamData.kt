package com.example.businesscard


import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import java.nio.charset.Charset

class BeamData : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
    private var mNfcAdapter: NfcAdapter? = null
    private var mTextView: TextView? = null
    internal var userEmail: String? = null

    // onNdefPushComplete 함수 동작을 위한 handler
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            // NFC 전송이 성공했을 경우 Toast 메세지 출력
            when (msg.what) {
                MESSAGE_SENT -> Toast.makeText(applicationContext, "send message!!!", Toast.LENGTH_LONG).show()
            }
        }
    }

    public override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        // NFC로 전송할 메세지
        // 사용자의 email 전송
        userEmail = intent.getStringExtra("userEmail")
        setContentView(R.layout.nfc_main)
        mTextView = findViewById(R.id.nfc_text)

        // NFC 사용 가능/불가능 여부 체크
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mNfcAdapter != null) {
            mTextView!!.text = "Tap to beam to another NFC device"
        } else {
            mTextView!!.text = "This phone is not NFC enabled."
        }

        // 안드로이드 빔을 성공적으로 전송했을 경우 이벤트 호출
        mNfcAdapter!!.setNdefPushMessageCallback(this, this)
        mNfcAdapter!!.setOnNdefPushCompleteCallback(this, this)
    }

    // NFC 전송 데이터 타입 설정
    fun createMimeRecord(mimeType: String, payload: ByteArray): NdefRecord {
        val mimeBytes = mimeType.toByteArray(Charset.forName("US-ASCII"))
        val mimeRecord = NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, ByteArray(0), payload)
        Log.d("createMimeRecord", "createMimeRecord")
        return mimeRecord
    }

    // NFC 메세지 생성
    override fun createNdefMessage(event: NfcEvent): NdefMessage {
        val text = "userEmail$userEmail"
        val msg = NdefMessage(arrayOf(createMimeRecord(
            "application/com.example.android.text", text.toByteArray())))

        Log.d("createNdefMessage", "createNdefMessage")
        return msg
    }

    // 안드로이드 빔 작업이 성공적으로 끝나면 호출
    // 메인 스레드에서 동작하지 않으므로 별도의 handler 필요
    override fun onNdefPushComplete(arg0: NfcEvent) {
        Log.d("onNdefPushComplete", "onNdefPushComplete")
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget()
        finish()
    }

    companion object {
        private val MESSAGE_SENT = 1
    }
}