package net.prahasiwi.smsapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewSmsActivity extends AppCompatActivity {

    EditText address, message,keyEnc;
    Button send_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sms);

        address = (EditText) findViewById(R.id.address);
        message = (EditText) findViewById(R.id.message);
        send_btn = (Button) findViewById(R.id.send_btn);
        keyEnc = (EditText) findViewById(R.id.key);

        send_btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String str_addtes = address.getText().toString();
                String str_message = message.getText().toString();
                String kunci = keyEnc.getText().toString();
                String encrypt = encryption(str_message,kunci);

                if (str_addtes.length() > 0 && str_message.length() > 0) {

                    if (Function.sendSMS(str_addtes, encrypt)) {
                        Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

        });
    }

    private String encryption(String plainText, String kunciRahasia) {
        StringBuffer encryptedString = new StringBuffer();
        int encryptedInt;
        for (int i = 0; i < plainText.length(); i++) {
            int plainTextInt = (int) plainText.charAt(i);
            int secretKeyInt = (int) kunciRahasia.charAt(i % kunciRahasia.length());
            encryptedInt = ((plainTextInt + secretKeyInt) * 76) + 3;
            encryptedString.append((char) encryptedInt);
        }
        return encryptedString.toString();
    }
}
