package dev.sanggi.sonstagram

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    val TAG = LoginActivity::class.simpleName

    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        autoLoginCheck()

        email_login_button.setOnClickListener {
            if (email_edittext.text.toString().isNullOrEmpty() || password_edittext.text.toString().isNullOrEmpty())
                Toast.makeText(this, "아이디 혹은 비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show()
            else {
                signinAndSignUp()
            }
        }
        google_login_button.setOnClickListener {
            googleLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                var account = result.signInAccount
                // Second step
                firebaseAuthWithGoogle(account)

            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인
                    moveMainPage(task.result?.user)
                } else {
                    // 에러 메세지
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    Log.e(TAG, task.exception?.message)
                }
            }

    }

    fun signinAndSignUp() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 유저 생성
                    Toast.makeText(this, "아이디가 생성되었습니다.", Toast.LENGTH_LONG).show()
                    moveMainPage(task.result?.user)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    // 오류 메세지
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    Log.e(TAG, task.exception?.message)
                } else {
                    // 로그인
                    signinEmail()
                }
            }
    }

    fun signinEmail() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인
                    moveMainPage(task.result?.user)
                } else {
                    // 에러 메세지
                    Toast.makeText(this, "아이디 혹은 비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show()
                    Log.w(TAG, task.exception?.message)
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            autoLoginSave()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    fun autoLoginCheck() {
        val auto = getSharedPreferences("auto", MODE_PRIVATE)

        val autoLogin: Boolean = auto.getBoolean("auto_login", false)
        val saveId: Boolean = auto.getBoolean("save_id", false)

        saveid_checkbox.isChecked = saveId
        autologin_checkbox.isChecked = autoLogin

        if (autoLogin) {
            email_edittext.setText(auto.getString("email", ""))
            password_edittext.setText(auto.getString("password", ""))
            signinEmail()
        }

        if (saveId) {
            saveid_checkbox.isChecked = saveId
            email_edittext.setText(auto.getString("email", ""))
        }
    }

    fun autoLoginSave() {
        val auto = getSharedPreferences("auto", Context.MODE_PRIVATE)
        val edit = auto.edit()

        edit.putBoolean("auto_login", autologin_checkbox.isChecked)
        edit.putBoolean("save_id", saveid_checkbox.isChecked)

        if (saveid_checkbox.isChecked)
            edit.putString("email", email_edittext.text.toString())

        if (autologin_checkbox.isChecked)
            edit.putString("password", password_edittext.text.toString())

        edit.commit()
    }
}
