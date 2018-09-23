package svvvcse.collegenotification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import javax.xml.transform.Source;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.Source;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_REQUEST_CODE = 0;
    private static final int SIGN_UP_REQUEST_CODE = 1;
    private static final String TAG = "COLLEGE NOTIFIC. SYSTEM";

    Button signIn_Button;
    Button signUp_Button;
    EditText empId;
    EditText Password;
    RelativeLayout relativeLayout;
    private ProgressBar progressBarSignup;
    private View progressBarSignup_Overlay;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn_Button = findViewById(R.id.signin);
        signUp_Button = findViewById(R.id.to_signup);
        empId = findViewById(R.id.emp_id);
        Password = findViewById(R.id.password);
        relativeLayout = findViewById(R.id.parent_SnackBar_signin);
        progressBarSignup = findViewById(R.id.progressBar_signin);
        progressBarSignup_Overlay = findViewById(R.id.progress_overlay_signin);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {     // Signed In.
                    Log.d("AUTH STATE CHECK ", "SIGNED IN!");

                    Intent i = new Intent(MainActivity.this, Activity_UserProfile.class);
                    startActivity(i);
                    finish();
                }else Log.d("AUTH STATE CHECK ", "NOT SIGNED IN!");
            }
        };

        registerButtonsClickListener();
    }

    @Override
    public void onStart() {
        // When some user is already Signed In, open the user's profile.
        mAuth.addAuthStateListener(mAuthStateListener);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int status = googleAPI.isGooglePlayServicesAvailable(this);

        if (status != ConnectionResult.SUCCESS) {

            if (googleAPI.isUserResolvableError(status)) {
                Log.d("GOOGLE PLAY SERVICES: ", "SHOWING ERROR DIALOG....");

                googleAPI.getErrorDialog(this,
                        status, // Error Code
                        PLAY_SERVICES_REQUEST_CODE  // Request code for the dialog
                );
            }
            else {
                Log.d("GOOGLE PLAY SERVICES: ", "STATUS : PLAY SERVICES ERROR : STATUS : FAILURE!!");

                Toast.makeText(this, R.string.play_services_error, Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else {
            Log.d("GOOGLE PLAY SERVICES: ", "STATUS : SUCCESS!!");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGN_UP_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d("ACTIVITY RESULT", "USER SIGNED IN SUCCESSFULLY!");

                    // Start the profile's Activity of User.
                    Intent i = new Intent(MainActivity.this, Activity_UserProfile.class);
                    startActivity(i);
                    finish();
                }
                break;

            case PLAY_SERVICES_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED) {
                    Log.d("GOOGLE PLAY SERVICES: ", "STATUS : DIALOG CANCELLED!!");

                    Toast.makeText(this, R.string.play_services_cancelled, Toast.LENGTH_SHORT).show();
                    finish();
                } else if (resultCode == RESULT_OK) {
                    Log.d("GOOGLE PLAY SERVICES: ", "STATUS : DIALOG SUCCESS!!");

                    Toast.makeText(this, "Welcome to BMS!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onStop() {
        if (mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
        super.onStop();
    }

    private void registerButtonsClickListener() {
        signUp_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Activity_SignUp.class);
                startActivityForResult(i, SIGN_UP_REQUEST_CODE);
            }
        });

        signIn_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for network connectivity
                Boolean isConnected = checkNetworkConnectivity();

                if (!isConnected) {
                    Log.d("SIGN IN :", "NOT CONNECTED!!");

                    Snackbar.make(relativeLayout, R.string.network_error, Snackbar.LENGTH_LONG).show();
                }

                else if(fieldsValid()) {
                    Log.d("SIGN IN :", "FIELDS ARE VALID!!");

                    // ProgressBar and overlay should be visible now, in case the processing of getting the document takes time.
                    progressBarSignup_Overlay.setVisibility(View.VISIBLE);
                    progressBarSignup.setVisibility(View.VISIBLE);

                    db.collection("FAC_DETAILS")
                            .whereEqualTo(empId.getText().toString(), null)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){
                                        Log.d(TAG, "Query Successful!");
                                        List<DocumentSnapshot> res = task.getResult().getDocuments();
                                        boolean resEmpty = res.isEmpty();

                                        if (resEmpty){
                                            Snackbar.make(relativeLayout,
                                                    "Sorry, no Account for the Employee Id. found", Snackbar.LENGTH_LONG)
                                                    .show();
                                        }
                                        else {
                                            DocumentSnapshot doc = res.get(0);
                                            String email = doc.getId();
                                            Signin(email);
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private void Signin(String userEmail) {
        mAuth.signInWithEmailAndPassword(userEmail, Password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "Sign in successful!");
//                            No need of Intent as AuthStateListener will do the work for us.
//                            Intent i = new Intent(MainActivity.this, Activity_UserProfile.class);
//                            startActivity(i);
//                            finish();
                        }
                        else {
                            Log.d("SIGN IN ERROR: ", task.getException().getMessage());

                            // Since the processing is completed, no need of overlay and progressBar.
                            progressBarSignup_Overlay.setVisibility(View.GONE);
                            progressBarSignup.setVisibility(View.GONE);

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Snackbar.make(relativeLayout, R.string.wrong_Input, Snackbar.LENGTH_LONG).show();
                            }
                            else
                                Snackbar.make(relativeLayout, R.string.sign_in_error, Snackbar.LENGTH_LONG).show();
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

    private boolean fieldsValid() {
        if (TextUtils.isEmpty(empId.getText().toString()) ||
                TextUtils.isEmpty(Password.getText().toString()))
        {
            Log.d("FIELDS VALIDITY: ", "Fields empty!");
            Snackbar.make(relativeLayout, R.string.empty_fields, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }
}
