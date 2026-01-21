package com.example.chat114.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat114.databinding.ActivitySignUpBinding;
import com.example.chat114.utilities.constants;
import com.example.chat114.utilities.preferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Activity for creating an account for registration.
 * Lets users upload a profile photo and provide their name, email, and password.
 */
public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private preferenceManager preferenceManager;
    private String encodeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new preferenceManager(getApplicationContext());
        setListeners();
    }

    /**
     * Sets up click listeners .
     */
    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        binding.buttonSingUp.setOnClickListener(v -> {
            if (isValidToSignUpDetails()) {
                signUp();
            }
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        }



    /**
     * Displays a toast message.
     * @param message Message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the user sign up process by saving user information to the Firebase Firestore.
     */
    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, String> user = new HashMap<>();
        user.put(constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(constants.KEY_IMAGE, encodeImage);

        database.collection(constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);


                    preferenceManager.putBoolean(constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(constants.KEY_IMAGE, encodeImage);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);


                }).addOnFailureListener(e -> {
                    loading(false);
                    showToast(e.getMessage());


        });


    }

    /**
     * Encodes a bitmap image into a Base64 string for storage.
     * @param bitmap The bitmap to encode.
     * @return Base64 encoded string.
     */
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }

    /**
     * Launcher to allow the user to pick an image from the gallery.
     *
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage = encodeImage(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
      );



    /**
     *
     * Checks if an image is selected, image, name, email, and password.
     * @return True if all details are valid, false otherwise.
     */
    private boolean isValidToSignUpDetails() {
        if (encodeImage == null) {
            showToast("Please select profile image");
            return false;
        } else if (binding.inputName.getText().toString().isEmpty()) {
            showToast("Please enter your name");
            return false;
        } else if (binding.inputEmail.getText().toString().isEmpty()){
            showToast("Please enter your email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Please enter a valid email");
            return false;
        }else if (binding.inputPassword.getText().toString().isEmpty()){
            showToast("Please enter your password");
            return false;
        }else if (binding.inputConfirmPassword.getText().toString().isEmpty()) {
            showToast("Please confirm  your password");
            return false;
        }else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Password does not match");
            return false;
        }
        else {
            return true;
            }


        }

    /**
     * Displays or hides the loading indicator based on the boolean value.
     * @param isLoading
     */
        private void loading(boolean isLoading) {
            if (isLoading) {
                binding.buttonSingUp.setVisibility(View.INVISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.INVISIBLE);
                binding.buttonSingUp.setVisibility(View.VISIBLE);
            }
        }
    }

