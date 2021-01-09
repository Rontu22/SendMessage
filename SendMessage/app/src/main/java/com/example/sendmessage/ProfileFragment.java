package com.example.sendmessage;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    // firebase storage
    StorageReference storageReference;
    // path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";





    //views from xml
    ImageView avatarIV,coverIV;
    TextView nameTV,emailTV,phoneTV;
    FloatingActionButton fab;


    //Progress dialog
    ProgressDialog progressDialog;


    // permission constraints
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;


    // Arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];


    // uri of picked image
    Uri image_uri;


    // for checking profile or cover photo
    String profileOrCoverPhoto ;


    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();



        //init arrays of permissions
        cameraPermissions = new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};



        //init views
        avatarIV = view.findViewById(R.id.avatarIV);
        coverIV = view.findViewById(R.id.coverIV);
        nameTV = view.findViewById(R.id.nameTV);
        emailTV = view.findViewById(R.id.emailTV);
        phoneTV = view.findViewById(R.id.phoneTV);
        fab = view.findViewById(R.id.fab);



        // init progress dialog
        progressDialog = new ProgressDialog(getActivity());





        /* we have to get info of currently signed in user . We can get it
        using user's email or uid, here we are going to retrive user detail using email
        By using orderByChild query we will show the detail from a node
        whose key named email has value equal to currently signed in email.
        It will search all nodes , where the key matches it will get its detail
        */
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check untill required data get
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    // get the data
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();
                    // set data
                    nameTV.setText(name);
                    emailTV.setText(email);
                    phoneTV.setText(phone);

                    try {
                        // if image is received then set
                        Picasso.get().load(image).into(avatarIV);
                    }
                    catch (Exception e)
                    {
                        // if there is any exception while getting image
                        // then set default
                        Picasso.get().load(R.drawable.ic_default_img_profile).into(avatarIV);
                    }


                    try {
                        // if image is received then set
                        Picasso.get().load(cover).into(coverIV);
                    }
                    catch (Exception e)
                    {
                        // if there is any exception while getting image
                        // then set default
                        Picasso.get().load(R.drawable.ic_add_image).into(coverIV);
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });


        return view;
    }

    private boolean checkStoragePermissions(){

        // check if storage permissions are enabled or not
        // return true if enabled
        // return false if not enabled

        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        // request runtime storage permission
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);

    }



    private boolean checkCameraPermissions(){

        // check if storage permissions are enabled or not
        // return true if enabled
        // return false if not enabled

        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        // request runtime storage permission
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);

    }




    private void showEditProfileDialog() {
        /*
        Show dialog containing:
            1. Edit profile
            2. Edit cover photo
            3. Edit name
            4. Edit phone

         */

        //options to show in dialog
        String options[] = {"Edit Profile Picture", "Edit Cover Photo",
                "Edit Name", "Edit Phone "};
        //ALert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //set title
        builder.setTitle("Choose Action ");

        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle dialog item click
                if (i == 0)
                {
                    //Edit profile Picture clicked
                    progressDialog.setMessage("Updating Profile Picture ..");
                    profileOrCoverPhoto = "image";

                    showImagePicDialog();
                }
                else if(i == 1)
                {
                    // edit cover photo clicked
                    progressDialog.setMessage("Updating cover photo ..");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
                else if (i == 2)
                {
                    // edit name clicked
                    progressDialog.setMessage("Updating Name ..");
                    showNamePhoneUpdateDialog("name");

                }
                else if(i == 3)
                {
                    // edit phone clicked
                    progressDialog.setMessage("Updating Phone Number ..");
                    showNamePhoneUpdateDialog("phone");
                }

            }
        });

        // create and show dialog
        builder.create().show();


    }

    private void showNamePhoneUpdateDialog(String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);

        // set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        // add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);


        builder.setView(linearLayout);

        // add buttons in dialog
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // input text from editText
                String value = editText.getText().toString().trim();

                // validate if user has entered something or not
                if (!TextUtils.isEmpty(value))
                {
                    progressDialog.show();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // updated , dismiss progressbar
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Updated... ", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // failed , dismiss progressbar and show error
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else
                {
                    Toast.makeText(getActivity(), "Please enter "+key, Toast.LENGTH_SHORT).show();

                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        // create and show dialog
        builder.create().show();





    }

    private void showImagePicDialog() {
        // show dialog containing options Camera and Gallery to pick the image


        //options to show in dialog
        String options[] = {"Camera", "Gallery"};
        //ALert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //set title
        builder.setTitle("Pick Image From : ");

        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle dialog item click
                if (i == 0)
                {
                    //Camera clicked
                    if (!checkCameraPermissions())
                    {
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }

                }
                else if(i == 1)
                {
                    // Gallery clicked
                    if (!checkStoragePermissions())
                    {
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }


            }
        });

        // create and show dialog
        builder.create().show();

        /*
        For picking image from :
        1. Camera [Camera and Storage permission required]
        2. Gallery [ Storage permission required]
         */
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // This method called when user press Allow or deny from permission request dialog
        // here we will handle the permission cases
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:{
                // picking from camera, first check if camera and storage
                // permissions allowed or not
                if (grantResults.length > 0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted)
                    {
                        //permissions enabled
                       pickFromCamera();
                    }
                    else {
                        // permissions denied
                        Toast.makeText(getActivity(), "Please enable camera & storage permissions", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;
            case  STORAGE_REQUEST_CODE:{
                // picking from Gallery, first check if storage
                // permissions allowed or not
                if (grantResults.length > 0)
                {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted)
                    {
                        //permissions enabled
                        pickFromGallery();
                    }
                    else {
                        // permissions denied
                        Toast.makeText(getActivity(), "Please enable storage permissions", Toast.LENGTH_SHORT).show();
                    }

                }

            }
            break;


        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // This method will becalled after picking image from camera or Gallery
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                // image is picked from Gallery, get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);


            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                // image is picked from camera , get uri of image


                uploadProfileCoverPhoto(image_uri);
            }
        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        // show progress
        progressDialog.show();


        /* Instead of creating seperate function for profile picture and cover photo
        we are doing work for both in same function

         */

        // path and name of image to be stored in firebase storage
        String filePathName = storagePath + ""+profileOrCoverPhoto+"_"+user.getUid();


        StorageReference storageReference2nd = storageReference.child(filePathName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image is uploaded to the database , now get it's url and store
                // in user's database
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());


                Uri downloadUri = uriTask.getResult();

                //check if image is uploaded or not and url is received
                if (uriTask.isSuccessful())
                {
                    // image uploaded
                    // add/update url in user's database
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(profileOrCoverPhoto,downloadUri.toString());


                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // url in database of user is added successfully
                                    // dismiss progress bar
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // error adding url in database of user
                            // dismiss progress bar
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();


                        }
                    });



                }
                else
                {
                    // error
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                }





            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // there were some errors , get and show error message ,
                // dismiss progress dialog
                progressDialog.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });




    }

    private void pickFromCamera() {
        // Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");


        // put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        // intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);




    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }


    private void checkUserStatus()
    {
        // get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null)
        {
            // user is signed in so stay here
            //mProfileTv.setText(user.getEmail());

        }
        else
        {
            // user not signed in, go to MainActivity
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu option in fragment

        super.onCreate(savedInstanceState);
    }

    /*inflate options menu */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main,menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
    /* handle menu item clicks */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //getitem id
        int id = item.getItemId();
        if (id == R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }



        return super.onOptionsItemSelected(item);
    }


}