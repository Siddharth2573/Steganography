package com.stegano.strenggeheim.utils;

import android.graphics.Color;

public class Constants {

  // Permissions
  public static final int PERMISSIONS_CAMERA = 3;
  public static final int PERMISSIONS_EXTERNAL_STORAGE = 4;

  // Request Codes
  public static final int REQUEST_CAMERA = 3;
  public static final int SELECT_FILE = 4;

  // Cover/Secret
  public static final int COVER_IMAGE = 3;
  public static final int SECRET_IMAGE = 4;

  //SharedPreferences
  public static final String SHARED_PREF_NAME = "cryptomessenger_spref";
  public static final String PREF_COVER_PATH = "cover_image_pref";
  public static final String PREF_COVER_IS_SET = "cover_is_set_pref";

  //Stego Image Map Keys
  public static final String MESSAGE_TYPE = "message_type";
  public static final String MESSAGE_BITS = "message_bits";

  // Secret Message Types
  public static final int TYPE_TEXT = 5;
  public static final int TYPE_IMAGE = 6;
  public static final int TYPE_UNDEFINED = 7;

  //Bundle arguments
  public static final String EXTRA_STEGO_IMAGE_PATH = "stego_image_path";
  public static final String EXTRA_SECRET_TEXT_RESULT = "secret_text_result";
  public static final String EXTRA_SECRET_IMAGE_RESULT = "secret_image_result";

  //Colors for Stego Image. They are the most rare colors in nature
  public static final int COLOR_RGB_END = Color.rgb(96, 62, 148); //Saint's Row Purple
  public static final int COLOR_RGB_TEXT = Color.rgb(135, 197, 245); //Killfom (Baby Blue)
  public static final int COLOR_RGB_IMAGE = Color.rgb(255, 105, 180); //Hot pink

}
