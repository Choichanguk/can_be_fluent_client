package com.example.canbefluent;

import android.content.Context;
import android.os.StrictMode;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.io.InputStream;

public class google_translate {
    Translate translate;
    Context mContext;

    public google_translate(Context mContext) {
        this.mContext = mContext;
    }

    public void getTranslateService() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = mContext.getResources().openRawResource(R.raw.canbefluent_437c34677353)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }

    public String translate(String lang_code, String originalText) {

        //Get input text to be translated:
//        originalText = inputToTranslate.getText().toString();
        Translation translation = translate.translate(originalText, Translate.TranslateOption.targetLanguage(lang_code), Translate.TranslateOption.model("base"));
        String translatedText = translation.getTranslatedText();

        return translatedText;
        //Translated text and original text are set to TextViews:
//        translatedTv.setText(translatedText);

    }
}
