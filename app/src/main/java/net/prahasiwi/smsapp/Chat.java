package net.prahasiwi.smsapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Chat extends AppCompatActivity {

    ListView listView;
//    Context context;
    ChatAdapter adapter;
    LoadSms loadsmsTask;
    String name;
    String address;
    EditText new_message;
    ImageButton send_message;
    int thread_id_main;
    private Handler handler = new Handler();
    Thread t;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> customList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        address = intent.getStringExtra("address");
        thread_id_main = Integer.parseInt(intent.getStringExtra("thread_id"));

        listView = (ListView) findViewById(R.id.listView);
        new_message = (EditText) findViewById(R.id.new_message);
        send_message = (ImageButton) findViewById(R.id.send_message);

        startLoadingSms();

        send_message.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String text = new_message.getText().toString();

                sending(text);

            }
        });
    }

    private String encryption(final String plainText, String kunciRahasia) {
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

    private void sending(final String text) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Chat.this);
        final LayoutInflater inflater = (LayoutInflater) Chat.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mView = inflater.inflate(R.layout.dialog_key, null);
        final EditText etAddKategory = (EditText) mView.findViewById(R.id.d_key);
        mBuilder.setView(mView)
                .setTitle("Input Key")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etAddKategory.getText().toString().isEmpty()) {
                            Toast.makeText(Chat.this, "Input Key Dulu", Toast.LENGTH_SHORT).show();
                        } else {
                            String encrypt = encryption(text,etAddKategory.getText().toString());

                            if (encrypt.length() > 0) {
                                String tmp_msg = encrypt;
                                new_message.setText("Sending....");
                                new_message.setEnabled(false);

                                if (Function.sendSMS(address, tmp_msg)) {
                                    new_message.setText("");
                                    new_message.setEnabled(true);
                                    // Creating a custom list for newly added sms
                                    customList.clear();
                                    customList.addAll(smsList);
                                    customList.add(Function.mappingInbox(null, null, null, null, tmp_msg, "2", null, "Sending..."));
                                    adapter = new ChatAdapter(Chat.this, customList);
                                    listView.setAdapter(adapter);
                                    //=========================
                                } else {
                                    new_message.setText(tmp_msg);
                                    new_message.setEnabled(true);
                                }
                            }
                        }
                    }
                });
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    class LoadSms extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tmpList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");
                Cursor inbox = getContentResolver().query(uriInbox, null, "thread_id=" + thread_id_main, null, null);
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "thread_id=" + thread_id_main, null, null);
                Cursor c = new MergeCursor(new Cursor[]{inbox, sent}); // Attaching inbox and sent sms


                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {
                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String type = c.getString(c.getColumnIndexOrThrow("type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));

                        tmpList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp)));
                        c.moveToNext();
                    }
                }
                c.close();

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            Collections.sort(tmpList, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            if (!tmpList.equals(smsList)) {
                smsList.clear();
                smsList.addAll(tmpList);
                adapter = new ChatAdapter(Chat.this, smsList);
                listView.setAdapter(adapter);
            }
        }
    }


    public void startLoadingSms() {
        final Runnable r = new Runnable() {
            public void run() {
                loadsmsTask = new LoadSms();
                loadsmsTask.execute();
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(r, 0);
    }
}


class ChatAdapter extends BaseAdapter {
    //    Context context;
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;

    public ChatAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ChatViewHolder holder = null;
        if (convertView == null) {
            holder = new ChatViewHolder();
            convertView = LayoutInflater.from(activity).inflate(R.layout.chat_item, parent, false);

            holder.txtMsgYou = (TextView) convertView.findViewById(R.id.txtMsgYou);
            holder.lblMsgYou = (TextView) convertView.findViewById(R.id.lblMsgYou);
            holder.timeMsgYou = (TextView) convertView.findViewById(R.id.timeMsgYou);
            holder.lblMsgFrom = (TextView) convertView.findViewById(R.id.lblMsgFrom);
            holder.timeMsgFrom = (TextView) convertView.findViewById(R.id.timeMsgFrom);
            holder.txtMsgFrom = (TextView) convertView.findViewById(R.id.txtMsgFrom);
            holder.msgFrom = (LinearLayout) convertView.findViewById(R.id.msgFrom);
            holder.msgYou = (LinearLayout) convertView.findViewById(R.id.msgYou);

            convertView.setTag(holder);
        } else {
            holder = (ChatViewHolder) convertView.getTag();
        }
        holder.txtMsgYou.setId(position);
        holder.lblMsgYou.setId(position);
        holder.timeMsgYou.setId(position);
        holder.lblMsgFrom.setId(position);
        holder.timeMsgFrom.setId(position);
        holder.txtMsgFrom.setId(position);
        holder.msgFrom.setId(position);
        holder.msgYou.setId(position);

        holder.txtMsgYou.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                itemLongYou(String.valueOf(data.get(position).get(Function.KEY_MSG)), position);
                return true;
            }
        });

        holder.txtMsgFrom.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                itemLongFrom(String.valueOf(data.get(position).get(Function.KEY_MSG)), position);
                return true;
            }
        });

        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);
        try {


            if (song.get(Function.KEY_TYPE).contentEquals("1")) {
                holder.lblMsgFrom.setText(song.get(Function.KEY_NAME));
                holder.txtMsgFrom.setText(song.get(Function.KEY_MSG));
                holder.timeMsgFrom.setText(song.get(Function.KEY_TIME));
                holder.msgFrom.setVisibility(View.VISIBLE);
                holder.msgYou.setVisibility(View.GONE);
            } else {
                holder.lblMsgYou.setText("You");
                holder.txtMsgYou.setText(song.get(Function.KEY_MSG));
                holder.timeMsgYou.setText(song.get(Function.KEY_TIME));
                holder.msgFrom.setVisibility(View.GONE);
                holder.msgYou.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
        }
        return convertView;
    }

    private void itemLongYou(final String msg, final int pos) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mView = inflater.inflate(R.layout.dialog_key, null);
        final EditText etAddKategory = (EditText) mView.findViewById(R.id.d_key);
        mBuilder.setView(mView)
                .setTitle("Input Key")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etAddKategory.getText().toString().isEmpty()) {
                            Toast.makeText(activity, "Input Key Dulu", Toast.LENGTH_SHORT).show();
                        } else {
                            String keyDe = etAddKategory.getText().toString();
                            String encrypn = decryption(msg, keyDe);

                            Intent intent = new Intent(activity, LihatDekripActivity.class);
                            intent.putExtra("tipe", data.get(pos).get(Function.KEY_TYPE));
                            intent.putExtra("decrypt", encrypn);
                            intent.putExtra("waktu", data.get(pos).get(Function.KEY_TIME));
                            activity.startActivity(intent);
                        }
                    }
                });
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    private void itemLongFrom(final String msg, final int pos) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mView = inflater.inflate(R.layout.dialog_key, null);
        final EditText etAddKategory = (EditText) mView.findViewById(R.id.d_key);
        mBuilder.setView(mView)
                .setTitle("Input Key")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etAddKategory.getText().toString().isEmpty()) {
                            Toast.makeText(activity, "Input Key Dulu", Toast.LENGTH_SHORT).show();
                        } else {
                            String keyDe = etAddKategory.getText().toString();
                            String encrypn = decryption(msg, keyDe);

                            Intent intent = new Intent(activity, LihatDekripActivity.class);
                            intent.putExtra("tipe", data.get(pos).get(Function.KEY_TYPE));
                            intent.putExtra("decrypt", encrypn);
                            intent.putExtra("dari", data.get(pos).get(Function.KEY_NAME));
                            intent.putExtra("waktu", data.get(pos).get(Function.KEY_TIME));
                            activity.startActivity(intent);
                        }
                    }
                });
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    private String decryption(final String decryptedText, String kunciRahasia) {
        StringBuffer decryptedString = new StringBuffer();
        int decryptedInt;
        for (int i = 0; i < decryptedText.length(); i++) {
            int decryptedTextInt = (int) decryptedText.charAt(i);
            int secretKeyInt = (int) kunciRahasia.charAt(i%kunciRahasia.length());
            decryptedInt = ((decryptedTextInt-3)/76)-secretKeyInt;
            decryptedString.append((char) decryptedInt);
        }
        return decryptedString.toString();
    }

}


class ChatViewHolder {
    LinearLayout msgFrom, msgYou;
    TextView txtMsgYou, lblMsgYou, timeMsgYou, lblMsgFrom, txtMsgFrom, timeMsgFrom;

}


