package com.example.twist_talk;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.twist_talk.MainActivity;
import com.example.twist_talk.R;
import com.example.twist_talk.SetupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        UserConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        CreateAccountButton = (Button) findViewById(R.id.register_create_account);

        loadingBar = new ProgressDialog(this);


        //Function call when the create account button is clicked
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            SendUsertoMainActivity();
        }
        super.onStart();
    }

    //Gets the data entered into the activity_register data fields
    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmpassword = UserConfirmPassword.getText().toString();

        //Checks if the email field is empty
        if(TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "Please enter an email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirmpassword)) {
            Toast.makeText(RegisterActivity.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
        }
        else if(! password.equals(confirmpassword))
        {
            Toast.makeText(RegisterActivity.this, "Your passwords don't match", Toast.LENGTH_SHORT).show();
        }
        else {

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating your new Account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                SendUserToSetupActivity();

                                Toast.makeText(RegisterActivity.this, "Account Authentication Successful", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this,"Error occurred: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUsertoMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
