package net.prahasiwi.smsapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class LihatDekripActivity extends AppCompatActivity {

    private TextView mLblMsgFromDe;
    private TextView mTxtMsgFromDe;
    private TextView mTimeMsgFromDe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lihat_dekrip);
        initView();

        Bundle bundle = getIntent().getExtras();
        String from = bundle.getString("tipe");
        if (from.equals("1")) {
            mLblMsgFromDe.setText(bundle.getString("dari"));
            mTxtMsgFromDe.setText(bundle.getString("decrypt"));
            mTimeMsgFromDe.setText(bundle.getString("waktu"));
        } else {
            mLblMsgFromDe.setText("You");
            mTxtMsgFromDe.setText(bundle.getString("decrypt"));
            mTimeMsgFromDe.setText(bundle.getString("waktu"));
        }
    }

    private void initView() {
        mLblMsgFromDe = (TextView) findViewById(R.id.lblMsgFromDe);
        mTxtMsgFromDe = (TextView) findViewById(R.id.txtMsgFromDe);
        mTimeMsgFromDe = (TextView) findViewById(R.id.timeMsgFromDe);
    }
}
