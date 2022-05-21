package com.stegano.strenggeheim.fragment;

import static com.stegano.strenggeheim.Constants.CAMERA;
import static com.stegano.strenggeheim.Constants.ENCODE_PROGRESS_MESSAGE;
import static com.stegano.strenggeheim.Constants.ENCODE_PROGRESS_TITLE;
import static com.stegano.strenggeheim.Constants.ERROR_SHORT_PASSWORD;
import static com.stegano.strenggeheim.Constants.FILE_TYPE_IMAGE;
import static com.stegano.strenggeheim.Constants.GALLERY;
import static com.stegano.strenggeheim.Constants.IMAGE_DIRECTORY;
import static com.stegano.strenggeheim.Constants.IMAGE_HEIGHT;
import static com.stegano.strenggeheim.Constants.IMAGE_WIDTH;
import static com.stegano.strenggeheim.Constants.MIN_PASSWORD_LENGTH;
import static com.stegano.strenggeheim.Constants.PICTURE_DIALOG_ITEM1;
import static com.stegano.strenggeheim.Constants.PICTURE_DIALOG_ITEM2;
import static com.stegano.strenggeheim.Constants.PICTURE_DIALOG_ITEM3;
import static com.stegano.strenggeheim.Constants.PICTURE_DIALOG_TITLE;
import static com.stegano.strenggeheim.Constants.PNG;
import static com.stegano.strenggeheim.Constants.SECRET_DATA_KEY;
import static com.stegano.strenggeheim.Constants.TEXTFILE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.mikhaellopez.circularimageview.BuildConfig;
//import com.stegano.strenggeheim.BuildConfig;
import com.stegano.strenggeheim.R;
import com.stegano.strenggeheim.activity.TextDialogActivity;
import com.stegano.strenggeheim.activity.encrypt.EncryptImageActivity;
import com.stegano.strenggeheim.utils.imageUtil.BitmapHelper;
import com.stegano.strenggeheim.utils.stego.Steganographer;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class FragmentEncode extends Fragment {
    private static int requestType;
    private File imageFile;
    private Bitmap bmpImage;
    private String secretText;
    private String hashingAlgo;
    private String encryptionAlgo;
    TextView imageTextMessage;
    TextView passwordToEncode;
    ImageView loadImage;
    Button textInputButton;
    Button encodeButton;
    Button encryptImg;
    ProgressDialog progress;

    public FragmentEncode() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void galleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType(FILE_TYPE_IMAGE);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA);
    }

    @Nullable
    private Uri getOutputMediaFileUri() {
        try {
            imageFile = getOutputMediaFile();

            return FileProvider.getUriForFile(getActivity(),
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
        }
        catch (IOException ex){
            showToastMessage(getString(R.string.error_fail_message));
        }
        return null;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encode, container, false);

        imageTextMessage = view.findViewById(R.id.imageTextMessage);
        loadImage =  view.findViewById(R.id.loadImage);
        textInputButton = view.findViewById(R.id.textInputButton);
        encodeButton = view.findViewById(R.id.encodeButton);
        passwordToEncode = view.findViewById(R.id.passwordToEncode);
        encryptImg = view.findViewById(R.id.encodeImageButton);


        initializeProgressDialog();

        loadImage.setOnClickListener(v -> showPictureDialog());


        textInputButton.setOnClickListener(v -> {
            Intent intent=new Intent(getContext(), TextDialogActivity.class);
            startActivityForResult(intent, TEXTFILE);
        });

        encryptImg.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EncryptImageActivity.class);
            startActivity(intent);
        });

        encodeButton.setOnClickListener(v -> {
            if(!isTextExist()) {
                showToastMessage(getString(R.string.error_no_text));
                return;
            }
            else if(!isImageExist()) {
                showToastMessage(getString(R.string.error_no_image));
                return;
            }
            else if(!isPasswordValid()){
                passwordToEncode.setError(ERROR_SHORT_PASSWORD);
                return;
            }
            else {
                switch (requestType) {
                    case GALLERY:
                        encodeImageFromGallery();
                        break;
                    case CAMERA:
                        encodeImageFromCamera();
                        break;
                }
            }
        });
        return view;
    }

    private boolean isPasswordValid() {
        return passwordToEncode.length() == 0 || passwordToEncode.length() >= MIN_PASSWORD_LENGTH;
    }

    private boolean isImageExist() {
        return imageFile != null && imageFile.exists();
    }

    private boolean isTextExist() {
        return secretText != null && !secretText.isEmpty();
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(requireContext());
        pictureDialog.setTitle(PICTURE_DIALOG_TITLE);
        String[] pictureDialogItems = {
                PICTURE_DIALOG_ITEM1,
                //PICTURE_DIALOG_ITEM2,
                PICTURE_DIALOG_ITEM3
        };
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            galleryIntent();
                            break;
                        case 1:
                            cameraIntent();
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                });
        pictureDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //getActivity();
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        try {
            if (requestCode == GALLERY && data != null) {
                requestType = GALLERY;
                bmpImage = getBitmapFromData(data, getContext());
                imageFile = getOutputMediaFile();
                loadImage.setImageBitmap(bmpImage);
                showToastMessage(getString(R.string.message_image_selected));
                imageTextMessage.setVisibility(View.INVISIBLE);
            }
            else if (requestCode == CAMERA) {
                requestType = CAMERA;
                bmpImage = compressBitmap(imageFile.getAbsolutePath());
                loadImage.setImageBitmap(bmpImage);
                showToastMessage(getString(R.string.message_image_selected));
                imageTextMessage.setVisibility(View.INVISIBLE);
            }
            else if (requestCode == TEXTFILE && data != null){
                secretText = data.getExtras().getString(SECRET_DATA_KEY);
            }
        } catch (Exception ex) {
            showToastMessage(getString(R.string.error_fail_message));
        }
    }

    private Bitmap getBitmapFromData(@NonNull Intent intent, @NonNull Context context){
        Uri selectedImage = intent.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver()
                .query(selectedImage,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return compressBitmap(picturePath);
    }

    private Bitmap compressBitmap(String picturePath){
        return BitmapHelper.decodeSampledBitmap(picturePath, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    private void encodeImageFromGallery() {
        progress.show();
        new Thread(() -> {
            encode();
            requireActivity().runOnUiThread(() -> {
                if (isImageExist()) {
                    MediaScannerConnection.scanFile(getContext(),
                            new String[]{imageFile.getPath()},
                            new String[]{FILE_TYPE_IMAGE}, null);
                    showToastMessage(getString(R.string.message_encoding_success));
                }
                reset();
            });
        }).start();
    }

    private void encodeImageFromCamera() {
        progress.show();
        new Thread(() -> {
            encode();
            requireActivity().runOnUiThread(() -> {
                if (isImageExist()) {
                    Intent mediaScanIntent =
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(imageFile);
                    mediaScanIntent.setData(contentUri);
                    requireContext().sendBroadcast(mediaScanIntent);
                    showToastMessage(getString(R.string.message_encoding_success));
                }
                else {
                    showToastMessage(getString(R.string.error_encoding_failed));
                }
                reset();
            });
        }).start();
    }

    private void encode() {
        try {
            getAlgoNamesFromSharedPreferences();
            String password = passwordToEncode.getText().toString();
            if(isPasswordEntered(password)) {
                Steganographer.withInput(bmpImage)
                        .withPassword(password)
                        .encode(secretText, encryptionAlgo, hashingAlgo)
                        .intoFile(imageFile);
            }
            else {
                Steganographer.withInput(bmpImage)
                        .encode(secretText, encryptionAlgo, hashingAlgo)
                        .intoFile(imageFile);
            }
        }
        catch (Exception e) {
            deleteFile();
        }
    }

    private void getAlgoNamesFromSharedPreferences() {
        Context context = requireActivity().getApplicationContext();
        SharedPreferences sharedPref =  PreferenceManager.getDefaultSharedPreferences(context);
        String defaultHashingAlgo = getString(R.string.list_prefs_default_hashing);
        String defaultEncryptionAlgo = getString(R.string.list_prefs_default_encryption);
        String hashingPref = getString(R.string.list_prefs_key_hashing);
        String encryptionPref = getString(R.string.list_prefs_key_encryption);
        hashingAlgo = sharedPref.getString(hashingPref, defaultHashingAlgo);
        encryptionAlgo = sharedPref.getString(encryptionPref, defaultEncryptionAlgo);
    }

    private boolean isPasswordEntered(String password) {
        return password != null && !password.isEmpty();
    }

    private void initializeProgressDialog(){
        progress = new ProgressDialog(getContext());
        progress.setTitle(ENCODE_PROGRESS_TITLE);
        progress.setMessage(ENCODE_PROGRESS_MESSAGE);
        progress.setCancelable(false);
    }

    @NonNull
    private File getOutputMediaFile() throws IOException {
        Log.i("Testing","Yahan pahunch gaye");

        File encodeImageDirectory =
                new File(Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        Log.i("Testing","Yahan pahunch behc");

        if (!encodeImageDirectory.exists()) {
            Log.i("Testing","Yahan pahunch gaye mac");

            encodeImageDirectory.mkdirs();
            Log.i("Testing","Yahan pahunch gaye sd");

        }
        Log.i("Testing","Yahan pahunch gaye gan");

        String uniqueId = UUID.randomUUID().toString();
        Log.i("Testing","Yahan pahunch gaye sa");

        File mediaFile = new File(encodeImageDirectory, uniqueId + PNG);
        Log.i("Testing","Yahan pahunch gaye hab");

        mediaFile.createNewFile();
        Log.i("Testing","Yahan pahunch gaye an");

        return mediaFile;
    }

    private void deleteFile(){
        if (isImageExist()) {
            imageFile.delete();
        }
    }

    private void reset(){
        secretText = "";
        imageFile = null;
        bmpImage = null;
        requestType = -1;
        progress.dismiss();
        loadImage.setImageResource(android.R.color.transparent);
        imageTextMessage.setVisibility(View.VISIBLE);
        passwordToEncode.setText("");
    }

    private void showToastMessage(final String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

}
