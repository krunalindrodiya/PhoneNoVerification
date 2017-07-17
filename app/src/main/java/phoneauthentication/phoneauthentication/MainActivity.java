package phoneauthentication.phoneauthentication;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button submit;
    private EditText phoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submit = (Button) findViewById(R.id.submit);
        phoneNo = (EditText) findViewById(R.id.input_phone);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = phoneNo.getText().toString();
                if (!TextUtils.isEmpty(number) && number.length() > 8) {
                    makePhoneVerification(number);
                } else {
                    Snackbar.make(view, "Please enter Phone number", Snackbar.LENGTH_LONG).show();
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
                        Snackbar.make(phoneNo, "Phone number authentication completed.", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.d("MainActivity", "FirebaseException=" + e);
                        Snackbar.make(phoneNo, "Phone number authentication fail.", Snackbar.LENGTH_LONG).show();
                    }
                });        // OnVerificationStateChangedCallbacks

    }
}
