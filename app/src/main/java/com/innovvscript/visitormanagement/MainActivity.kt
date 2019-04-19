package com.innovvscript.visitormanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.lang.Double.parseDouble




class MainActivity : AppCompatActivity() {

    var phoneInput : TextInputEditText?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phoneInput = findViewById<TextInputEditText>(R.id.phone_input_id)
        val uploadbtn = findViewById<MaterialButton>(R.id.upload_btn_id)


        uploadbtn.setOnClickListener { view ->  verifyNstart() }
    }

    private fun verifyNstart() {
        if (checkPhone()){
          val intent = Intent(this,CamActivity::class.java)
            Log.w("verify","correct")
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString("phone","${phoneInput?.text}")
                    .apply()
            startActivity(intent)
            finish()
        }else{
          phoneInput!!.setError("Invalid phone number")
        }
    }

    private fun checkPhone(): Boolean {
        var phoneNo = phoneInput?.text.toString()
        return phoneNo.length.equals(10) && isValidNumber(phoneNo)
    }

    private fun isValidNumber(phone: String): Boolean {
        var numeric = true
          try{
             parseDouble(phone)
              numeric = true
          }catch (e: NumberFormatException){
              numeric = false
              Log.w("catch","$e")
          }
        return numeric
    }
}
