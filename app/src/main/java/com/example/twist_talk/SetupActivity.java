package com.example.twist_talk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.service.autofill.SaveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.twist_talk.MainActivity;
import com.example.twist_talk.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserID;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_fullname);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationButton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();
            }
        });


        //Profile Image is of type CircleImageView
        //Opens up the gallery for viewing
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }

                    else
                    {
                        Toast.makeText(SetupActivity.this, "Please select profile image first", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }


    //Since an startActivityForResult was already called, it will automatically call onActivityResult upon return
    //This function is what allows the user to crop the photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        //This is when the "crop" button is pressed
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //The Uri of the cropped photo is saved
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your profile image");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                //Where the cropped image is stored
                Uri resultUri = result.getUri();

                //A child is created in the node with the current user ID with type jpg
                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                //The cropped image is stored with putFile(), and an addOnCompleteListener function is created to complete the task
                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful()) {
                            Toast.makeText(SetupActivity.this, "Profile Image stored successfully", Toast.LENGTH_SHORT).show();
                            final String downloadUri = task.getResult().toString();

                            //The URL of the newly posted profile picture is stored in the Database
                            //The actual photo itself is stored in Storage
                            //This method is ok because there can only be 1 profile picture at most
                            UsersRef.child("profileimage").setValue(downloadUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful()) {
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database successfully", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(SetupActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(SetupActivity.this, "Error occurred: Image can't be cropped", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }



    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if(TextUtils.isEmpty(username)) {
            Toast.makeText(this, "No username entered", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "No full name entered", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country)) {
            Toast.makeText(this, "No country entered", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Setting up account");
            loadingBar.setMessage("Please wait, while we are setting up your new account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationshipstatus", "none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful()) {
                        SendUsertoMainActivty();
                        Toast.makeText(SetupActivity.this, "Your account is now ready",Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }

                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void SendUsertoMainActivty() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
