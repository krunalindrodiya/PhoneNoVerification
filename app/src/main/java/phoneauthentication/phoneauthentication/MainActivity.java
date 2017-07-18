package phoneauthentication.phoneauthentication;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button submit;
    private EditText phoneNo;
    private EditText verificationCode;
    private ProgressBar progressBar;

    private boolean isVerification = false;

    private PhoneAuthCredential phoneAuthCredential;

    private TextView textError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submit = (Button) findViewById(R.id.submit);
        phoneNo = (EditText) findViewById(R.id.input_phone);
        verificationCode = (EditText) findViewById(R.id.input_code);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textError = (TextView) findViewById(R.id.textError);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if (isVerification) {
                    if (phoneAuthCredential != null) {
                        String code = verificationCode.getText().toString();
                        if (code.equals(phoneAuthCredential.getSmsCode())) {
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                            textError.setText("Athentication successfully.\n provider=" + phoneAuthCredential.getProvider() + "\n SMS Code=" + phoneAuthCredential.getSmsCode());
                        } else {
                            textError.setText("OTP is not validate.");
                        }
                    }
                } else {
                    String number = phoneNo.getText().toString();
                    if (!TextUtils.isEmpty(number) && number.length() > 8) {
                        progressBar.setVisibility(View.VISIBLE);
                        makePhoneVerification(number);
                    } else {
                        Snackbar.make(view, "Please enter Phone number", Snackbar.LENGTH_LONG).show();
                    }
                }

            }
        });

    }

    private void makePhoneVerification(final String phoneNumber) {


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        Log.d("MainActivity", "Phone Authentication=" + phoneAuthCredential);
                        Log.d("MainActivity", "SMS=" + phoneAuthCredential.getSmsCode());
//                        signInWithPhoneAuthCredential(phoneAuthCredential);

                        if (phoneAuthCredential != null) {
                            MainActivity.this.phoneAuthCredential = phoneAuthCredential;
                            Snackbar.make(submit, "Please enter verification code, received by sms.", Snackbar.LENGTH_LONG).show();
                            isVerification = true;
                            verificationCode.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            phoneNo.setEnabled(false);
                        } else {
                            Snackbar.make(submit, "Server error or network busy.", Snackbar.LENGTH_INDEFINITE).show();
                        }

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.d("MainActivity", "FirebaseException=" + e);
                        Snackbar.make(phoneNo, "Phone number authentication fail.", Snackbar.LENGTH_LONG).show();
                    }
                });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MainActivity", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("MainActivity", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

}
