package com.example.parstagram.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ThemedSpinnerAdapter;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parstagram.BitmapScaler;
import com.example.parstagram.R;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileFragment extends Fragment {

    private TextView etProfileUsername;
    private ImageView ivEditProfilePicture;

    private File photoFile;
    public String photoFileName = "profile.jpg";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final String TAG = "EditProfileFragment";

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etProfileUsername = view.findViewById(R.id.etProfileUsername);
        ivEditProfilePicture = view.findViewById(R.id.ivEditProfilePicture);

        etProfileUsername.setText(ParseUser.getCurrentUser().getUsername());

        ParseFile profile = ParseUser.getCurrentUser().getParseFile("profile");
        if (profile != null) {
            Glide.with(getContext()).load(profile.getUrl()).placeholder(R.drawable.profile).circleCrop().into(ivEditProfilePicture);
        }

        etProfileUsername.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                ParseUser.getCurrentUser().setUsername(etProfileUsername.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        ivEditProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage("Do you want to change your profile picture?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        launchCamera();
                        dialog.cancel();
                    }
                });

                builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert11 = builder1.create();
                alert11.show();

                alert11.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                alert11.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
    }

    private void launchCamera() {
        // Create Intent to take a picture and return control to the calling application
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a File reference to access
        photoFile = getPhotoFileUri(photoFileName);

        // Wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        i.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If there's an app that can handle the intent
        if (i.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // By this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                // Scale the image smaller
                Bitmap resizedBitmap = BitmapScaler.scaleToFitHeight(takenImage, 200);

                // Store smaller bitmap to disk
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(resizedFile);
                        // Write the bytes of the bitmap to file
                        fos.write(bytes.toByteArray());
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ivEditProfilePicture.setImageBitmap(resizedBitmap);
                photoFile = resizedFile;

                ParseFile imageFile = new ParseFile(photoFile);
                ParseUser.getCurrentUser().put("profile", imageFile);
                ParseUser.getCurrentUser().saveInBackground();
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // getExternalFilesDir used to access package specific directories, so don't need to request external read/write runtime permissions
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }
}