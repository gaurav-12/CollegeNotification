package svvvcse.collegenotification;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

import io.opencensus.internal.StringUtil;

public class Activity_SignUp extends AppCompatActivity {

    private static final String TAG = "COLLEGE_ NOTIF. SYSTEM";
    private EditText empId, Email, Password, Name;
    private TextView PasswordRules;
    private Button signIn_Button, signUp_Button;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBarSignup;
    private View progressBarSignup_Overlay;

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        empId = findViewById(R.id.emp_id_signup);
        Email = findViewById(R.id.email_signup);
        Password = findViewById(R.id.password_signup);
        Name = findViewById(R.id.name);
        PasswordRules = findViewById(R.id.password_rules_textview);

        Password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    // Regex in JAVA is Case Sensitive by default. Add (?i) at the beginning for case insensitivity.
                    if (!(Password.getText().toString().matches(
                            "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%&^+=\\-*,._?])(?!.*\\s).{6,}$")))
                    {
                        PasswordRules.animate()
                                .alpha(1f)
                                .setDuration(250)
                                .start();
                    }
                    else {
                        Log.d(TAG, "Password incorrect!");
                    }
                }
                else{
                    PasswordRules.animate()
                            .alpha(0f)
                            .setDuration(250)
                            .start();
                }
            }
        });

        Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(s.toString().matches(
                        "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%&^+=\\-*,._?])(?!.*\\s).{6,}$")))
                {
                    PasswordRules.animate()
                            .alpha(1f)
                            .setDuration(250)
                            .start();
                }
                else {
                    PasswordRules.animate()
                            .alpha(0f)
                            .setDuration(250)
                            .start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        signIn_Button = findViewById(R.id.to_signin);
        signUp_Button = findViewById(R.id.signup);
        relativeLayout = findViewById(R.id.parent_SnackBar_signup);
        progressBarSignup = findViewById(R.id.progressBar_signup);
        progressBarSignup_Overlay = findViewById(R.id.progress_overlay_signup);

        mAuth = FirebaseAuth.getInstance();

        registerButtonsClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void registerButtonsClickListener() {
        signIn_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signUp_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SIGN UP :", "SIGN UP TAPPED!!!");

                // Check for network connectivity
                Boolean isConnected = checkNetworkConnectivity();

                if (!isConnected) {
                    Log.d("SIGN UP :", "NOT CONNECTED!!");

                    Snackbar.make(relativeLayout, R.string.network_error, Snackbar.LENGTH_LONG).show();
                }

                else if(fieldsValid()) {
                    Log.d("SIGN UP :", "FIELDS ARE VALID!!");

                    // ProgressBar and overlay should be visible now, in case the processing of getting the document takes time.
                    progressBarSignup_Overlay.setVisibility(View.VISIBLE);
                    progressBarSignup.setVisibility(View.VISIBLE);

                    db.collection("FAC_DETAILS")
                            .document(Name.getText().toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot result = task.getResult();
                                        if (result.contains("ACCOUNT_ACTIVE")){
                                            Snackbar.make(relativeLayout,
                                                    "Account for this Email already active, try Signing In",
                                                    Snackbar.LENGTH_LONG).show();
                                        }else {
                                            if (result.contains(Name.getText().toString().toUpperCase())){
                                                signupUser(Name.getText().toString());
                                            }else {
                                                Snackbar.make(relativeLayout,
                                                        "Email and Name did not match, try rechecking fields",
                                                        Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private void signupUser(final String name) {
        mAuth.createUserWithEmailAndPassword(Email.getText().toString(), Password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "Signup successful!");
                            FirebaseUser user = task.getResult().getUser();
                            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(Name.getText().toString())
                                    .build();
                            updateProfile(user, changeRequest);
                        }
                    }
                });
    }

    private void updateProfile(FirebaseUser user, UserProfileChangeRequest profileUpdates) {
        final Map<String, Object> data = new HashMap<>();
        data.put("ACCOUNT_ACTIVE", null);
        data.put(empId.getText().toString(), null);

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("USER UPDATE: ", "Profile updated!");
                            db.collection("FAC_DETAILS").document(Email.getText().toString())
                                    .update(data)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                finish();
                                            }
                                        }
                                    });
                        }
                        else {
                            Log.d("PROFILE UPDATE ERROR: ", task.getException().getMessage());

                            Snackbar.make(relativeLayout, R.string.sign_up_error, Snackbar.LENGTH_LONG).show();

                            // Since the processing is completed, no need of overlay and progressBar.
                            progressBarSignup_Overlay.setVisibility(View.GONE);
                            progressBarSignup.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @NonNull
    private Boolean checkNetworkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

//    Email Regex explanation:

    //         ^ asserts position at start of the string
//         Match a single character present in the list below [A-Z0-9._%+-]+
//              + Quantifier — Matches between one and unlimited times, as many times as possible, giving back as needed
//              A-Z a single character in the range between A (index 65) and Z (index 90) (case insensitive)
//              0-9 a single character in the range between 0 (index 48) and 9 (index 57) (case insensitive)
//              ._%+- matches a single character in the list ._%+- (case insensitive).
//         @ matches the character @ literally (case insensitive).
//         Match a single character present in the list below [A-Z]{2,4}
//              {2,4} Quantifier — Matches between 2 and 4 times, as many times as possible, giving back as needed
//              A-Z a single character in the range between A (index 65) and Z (index 90) (case insensitive)
//         $ asserts position at the end of the string
    private boolean fieldsValid() {
        if (TextUtils.isEmpty(empId.getText().toString()) ||
                TextUtils.isEmpty(Email.getText().toString()) ||
                TextUtils.isEmpty(Password.getText().toString()) ||
                TextUtils.isEmpty(Name.getText().toString()))
        {
            Log.d("FIELDS VALIDITY: ", "Fields empty!");
            Snackbar.make(relativeLayout, R.string.invalid_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else if (!(Email.getText().toString().matches("^[A-Za-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")) ||
                !(Password.getText().toString().matches(
                        "^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%&^+=\\-*,._?])(?!.*\\s).{6,}$")))
        {
            Log.d("FIELDS VALIDITY: ", "Email or Password invalid!");
            Snackbar.make(relativeLayout, R.string.invalid_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }
}
