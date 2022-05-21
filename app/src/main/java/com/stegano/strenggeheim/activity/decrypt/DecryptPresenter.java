package com.stegano.strenggeheim.activity.decrypt;


interface DecryptPresenter {

  void setStegiImgPath(String path);

  void selectImage(String path);


  void decryptMessage();
}
