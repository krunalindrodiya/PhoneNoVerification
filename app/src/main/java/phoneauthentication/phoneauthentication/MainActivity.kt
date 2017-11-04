package phoneauthentication.phoneauthentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var isVerification = false
    private var phoneAuthCredential: PhoneAuthCredential? = null
    private var verificationId: String? = null
    private val RESOLVE_HINT: Int = 10
    private val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this) { connectionResult ->
                    Log.d("MainActivity", "conenction failed")
                }
                .addApi(Auth.CREDENTIALS_API)
                .build()

        requestHint(apiClient)

        submit.setOnClickListener { view ->
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            if (isVerification) {

                if (phoneAuthCredential == null && !verificationId.isNullOrEmpty()) {
                    val code = input_code.text.toString()
                    if (!code.isNullOrEmpty()) {
                        val credential = verificationId?.let { PhoneAuthProvider.getCredential(it, input_code.toString()) }

                        credential?.let { signInWithPhoneAuthCredential(it) }
                    } else {
                        Snackbar.make(view, "Please enter OTP", Snackbar.LENGTH_LONG).show()
                    }

                }

                phoneAuthCredential?.let { phoneAuthCredential ->
                    val code = input_code.text.toString()
                    if (code == phoneAuthCredential.smsCode) {
                        signInWithPhoneAuthCredential(phoneAuthCredential)
                        textError.text = "Athentication successfully.\n provider=" + phoneAuthCredential.provider + "\n SMS Code=" + phoneAuthCredential.smsCode
                    } else {
                        textError.text = "OTP is not validate."
                    }
                }
            } else {
                val number = input_phone.text.toString()
                if (number.isNotEmpty() && number.length > 8) {
                    progressBar.visibility = View.VISIBLE
                    makePhoneVerification(number)
                } else {
                    Snackbar.make(view, "Please enter Phone number", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    // Obtain the phone number from the result
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == Activity.RESULT_OK) {
                val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                Log.d("MainActivity", "Credential-" + credential.id)
                if (credential.id.isNotEmpty()) {
                    input_phone.setText(credential.id.toString())
                }
            }
        }
    }


    private fun requestHint(apiClient: GoogleApiClient) {
        val hintRequest = HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build()

        val intent = Auth.CredentialsApi.getHintPickerIntent(
                apiClient, hintRequest)
        startIntentSenderForResult(intent.intentSender,
                RESOLVE_HINT, null, 0, 0, 0)
    }


    private fun makePhoneVerification(phoneNumber: String) {


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, mCallbacks)
    }

    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("MainActivity", "Phone Authentication=" + phoneAuthCredential)
            Log.d("MainActivity", "SMS=" + phoneAuthCredential?.smsCode)

//                        phoneAuthCredential?.let { signInWithPhoneAuthCredential(it) }

            if (phoneAuthCredential != null) {
                this@MainActivity.phoneAuthCredential = phoneAuthCredential
                Snackbar.make(submit, "Please enter verification code, received by sms.", Snackbar.LENGTH_LONG).show()
                isVerification = true
                input_code.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                input_phone.isEnabled = false
            } else {
                Snackbar.make(submit, "Server error or network busy.", Snackbar.LENGTH_INDEFINITE).show()
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Snackbar.make(input_phone, "Phone number authentication fail.", Snackbar.LENGTH_LONG).show()
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {

                    Log.e(TAG, "Invalid request ${e.printStackTrace()}")
                }
                is FirebaseTooManyRequestsException -> {

                    Log.e(TAG, "The SMS quota for the project has been exceeded ${e.printStackTrace()}")
                }
                else -> Log.e(TAG, "Others ${e.printStackTrace()}")
            }
        }

        override fun onCodeSent(verifiId: String?,
                                token: PhoneAuthProvider.ForceResendingToken?) {
            verificationId = verifiId

            Snackbar.make(submit, "Please enter verification code, received by sms.", Snackbar.LENGTH_LONG).show()
            isVerification = true
            input_code.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            input_phone.isEnabled = false

            Log.d(TAG, "verificationId $verificationId")
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("MainActivity", "signInWithCredential:success")

                        val user = task.result.user
                        Log.d("MainActivity", "User detail $user")

                        Toast.makeText(this, "Sign in successfully", Toast.LENGTH_LONG).show()
                        // ...
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
                }
    }

}
