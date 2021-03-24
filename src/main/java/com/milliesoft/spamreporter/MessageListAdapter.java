package com.milliesoft.spamreporter;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<Message> {
    private Context ctx;
    public ArrayList<Message> messageListArray;
    public MessageListAdapter(Context context, int textViewResourceId,
            ArrayList<Message> messageListArray) {
        super(context, textViewResourceId);
        this.messageListArray = messageListArray;
        this.ctx = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View convertView1 = convertView;
        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.message_list_item, null);
            holder.messageTo = (TextView) convertView1.findViewById(R.id.txt_msgTO);
     //       holder.messageDate = (TextView) convertView1.findViewById(R.id.txt_msgDate);
            holder.messageContent = (TextView) convertView1.findViewById(R.id.txt_messageContent);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }
        Message message = getItem(position);

        holder.messageTo.setText(message.messageNumber +" : ");
       // holder.messageDate.setText(message.messageDate +" : ");
        if(message.isKnown){
            holder.messageTo.setTextColor(0xFF006400);
        } else {
            holder.messageTo.setTextColor(Color.BLACK);

        }

        holder.messageContent.setText(message.messageContent);
        if (position % 2 == 1) {
        	convertView1.setBackgroundColor(0x88CCCCCC);  
        } else {
        	convertView1.setBackgroundColor(0xFFCCCCCC);  
        }

        return convertView1;
    }

    @Override
    public int getCount() {
        return messageListArray.size();
    }

    @Override
    public Message getItem(int position) {
        return messageListArray.get(position);
    }

    public void setArrayList(ArrayList<Message> messageList) {
        this.messageListArray = messageList;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView messageTo, messageContent;
    }

}

