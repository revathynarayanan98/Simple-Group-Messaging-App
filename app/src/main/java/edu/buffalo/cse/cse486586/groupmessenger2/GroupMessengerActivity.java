package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.buffalo.cse.cse486586.groupmessenger2.messageBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static android.content.ContentValues.TAG;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {


    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT_1="11108";
    static final String REMOTE_PORT_2="11112";
    static final String REMOTE_PORT_3="11116";
    static final String REMOTE_PORT_4="11120";
    static final String REMOTE_PORT_5="11124";
    String[] REMOTE_PORT_ARRAY = {REMOTE_PORT_1, REMOTE_PORT_2, REMOTE_PORT_3, REMOTE_PORT_4, REMOTE_PORT_5};  // Remoteports[i]


    static final int SERVER_CONNECTION_PORT = 10000;
    String SENDING_PORT;
    EditText getText;
    Button sendButton;
    String message_to_send,EMPTY_STRING="";
    TextView viewText;
    int message_ID=0;
    String message_Type="PROPOSAL";
    int sequence=0;
    String PACKET_FROM_CLIENT_PROPOSED;
    String PACKET_FROM_CLIENT_AGREED;
    int[] prop_Seq ={0,0,0,0,0};
    int[] agreed_Seq={0,0,0,0,0};
    Queue<messageBuilder> buffer = new PriorityQueue<messageBuilder>(11, new comparator());
    List<String> DEAD_AVDS = new ArrayList<String>();
    int dead_control=0;
    int send_Port_check=0;
    int receive_port_check=0;
    String receiver;


    class comparator implements Comparator<messageBuilder> {
        public int compare(messageBuilder m1, messageBuilder m2) {
            if (m1.CONSENSUS_SEQUENCE_NUM < m2.CONSENSUS_SEQUENCE_NUM)
                return -1;
            else if (m1.CONSENSUS_SEQUENCE_NUM > m1.CONSENSUS_SEQUENCE_NUM)
                return 1;
            return 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        getText = (EditText) findViewById(R.id.editText1);
        sendButton = (Button) findViewById(R.id.button4);
        viewText = (TextView) findViewById(R.id.textView1);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        SENDING_PORT=myPort;




        try {
            /*
             * Creating a server socket
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_CONNECTION_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
            Log.e("This is the ErRoR: ",e.getMessage());
            return;
        }


        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message_to_send = getText.getText().toString()+"\n";
                getText.setText(EMPTY_STRING);
                //viewText.append(message_to_send);

                Log.v("MEssage:",""+message_to_send);
                Log.v("myPort:",""+myPort);

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message_to_send, myPort);

            }
        });

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }





    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {




        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            try{
                while(true) {
                    Socket client_Socket = serverSocket.accept();
                    client_Socket.setSoTimeout(2000);
                    DataInputStream dataInputStream = new DataInputStream(client_Socket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(client_Socket.getOutputStream());


                    try {
                        PACKET_FROM_CLIENT_PROPOSED = dataInputStream.readUTF();
                        Log.v("PACKET_FROM_CLIENT:"," "+PACKET_FROM_CLIENT_PROPOSED);


                    } catch (Exception e) {
                        Log.d("Server:","No Initial MEssage");

                    }

                    String[] ALL_MESSAGE_DATA_PROPOSED = unpack(PACKET_FROM_CLIENT_PROPOSED);

                    Log.v("ALL_msg_DATA(P):","Message ID:  "+ALL_MESSAGE_DATA_PROPOSED[0]+"   Message:   "+ALL_MESSAGE_DATA_PROPOSED[1]+"   MEssage Type:   "+ALL_MESSAGE_DATA_PROPOSED[2]+" SendPort:  "+ALL_MESSAGE_DATA_PROPOSED[3]+"  Receive Port(this):   "+ALL_MESSAGE_DATA_PROPOSED[4]+"    Receive Port Num:    "+ALL_MESSAGE_DATA_PROPOSED[5]+"   Consensus Num"+ALL_MESSAGE_DATA_PROPOSED[6]);


                    messageBuilder buildMessage_PROPOSED = new messageBuilder(Integer.parseInt(ALL_MESSAGE_DATA_PROPOSED[0]), ALL_MESSAGE_DATA_PROPOSED[1], ALL_MESSAGE_DATA_PROPOSED[2], ALL_MESSAGE_DATA_PROPOSED[3], Integer.parseInt(ALL_MESSAGE_DATA_PROPOSED[4]), Integer.parseInt(ALL_MESSAGE_DATA_PROPOSED[5]),Integer.parseInt(ALL_MESSAGE_DATA_PROPOSED[6]));

                    Log.v("buildMessage_PROPOSED: ","MessageID:"+buildMessage_PROPOSED.getMessage_ID()+"    Message:"+buildMessage_PROPOSED.getMessage()+"    MessageType:"+buildMessage_PROPOSED.getMessage_Type()+"    SendPort:"+buildMessage_PROPOSED.getSEND_PORT()+"     Receiveport"+buildMessage_PROPOSED.getRECEIVE_PORT()+"   REceive Port NUm:"+buildMessage_PROPOSED.getRECEIVE_PORT_NUM()+"    COnsensus seq num:"+buildMessage_PROPOSED.getCONSENSUS_SEQUENCE_NUM());
                    send_Port_check = Integer.parseInt(buildMessage_PROPOSED.getSEND_PORT());

                    receive_port_check = buildMessage_PROPOSED.getRECEIVE_PORT();
                    /*




                DataOutputStream dataOutputStream = new DataOutputStream(client_Socket.getOutputStream());
                dataOutputStream.writeUTF(buildMessage.getMessage());

                 */
                    //Choosing proposed sequence number
                    if(buildMessage_PROPOSED.getMessage_Type().equals("PROPOSAL")) {
                        Log.v("inside Chsing Prop Seq:","YEs");
                        choose_prop_seq_num(buildMessage_PROPOSED.getMessage_Type(), buildMessage_PROPOSED.getRECEIVE_PORT_NUM());   //FINISHED CHECKING TILL HERE



                        //Add message to buffer
                        buildMessage_PROPOSED.proposed.add(prop_Seq[buildMessage_PROPOSED.getRECEIVE_PORT_NUM()]);
                        for(int i=0;i<buildMessage_PROPOSED.proposed.size();i++){
                            Log.v("buildmsg_PROP.proposed["+i+"]","->"+buildMessage_PROPOSED.proposed.get(i));
                        }
                        buffer.add(buildMessage_PROPOSED);

                        //Sending proposed sequence num to client
                        dataOutputStream.writeUTF(prop_Seq[buildMessage_PROPOSED.getRECEIVE_PORT_NUM()] + "@@"); //Propose_seq[i]
                    }


                    //---------------------- AGREED-----------------------
                    String[] ALL_MESSAGE_DATA_AGREED = null;

                    try {
                        PACKET_FROM_CLIENT_AGREED = dataInputStream.readUTF();
                        ALL_MESSAGE_DATA_AGREED = unpack(PACKET_FROM_CLIENT_AGREED);      //Here at 12:46


                    } catch (Exception e) {
                        Log.v("SendPort"+SENDING_PORT,"RemotePort:  ");



//                        if(!DEAD_AVDS.contains(buildMessage_PROPOSED.getSEND_PORT()));
//                        {
//                            DEAD_AVDS.add(buildMessage_PROPOSED.getSEND_PORT());
//                        }
//                        Log.v("Dead avd added",DEAD_AVDS.toString());

                    }


                    // remove_DEADAVDS_from_buffer();

                    if(!DEAD_AVDS.isEmpty()){
                        for(String port : DEAD_AVDS){
                            for(messageBuilder message_obj : buffer){
                                if(REMOTE_PORT_ARRAY[message_obj.getRECEIVE_PORT_NUM()].equals(port)){                //// CHANGE MADE at 5.45
                                    buffer.remove(message_obj);
                                }
                            }
                        }
                        Log.v("Updating buffer",DEAD_AVDS.toString());
                    }




                    messageBuilder buildMessage_AGREED = new messageBuilder(Integer.parseInt(ALL_MESSAGE_DATA_AGREED[0]), ALL_MESSAGE_DATA_AGREED[1], ALL_MESSAGE_DATA_AGREED[2], ALL_MESSAGE_DATA_AGREED[3], Integer.parseInt(ALL_MESSAGE_DATA_AGREED[4]), Integer.parseInt(ALL_MESSAGE_DATA_AGREED[5]),Integer.parseInt(ALL_MESSAGE_DATA_AGREED[6]));
                    Log.v("buildMessage_AGREED: "," MEssageID:"+buildMessage_AGREED.getMessage_ID()+"     Message:"+buildMessage_AGREED.getMessage()+"     MessageType:"+buildMessage_AGREED.getMessage_Type()+"       Sendport:"+buildMessage_AGREED.getSEND_PORT()+"     Receiveport:"+buildMessage_AGREED.getRECEIVE_PORT()+"      ReceivePortNum:"+buildMessage_AGREED.getRECEIVE_PORT_NUM()+"   COnsensusSeqNUm:"+buildMessage_AGREED.getCONSENSUS_SEQUENCE_NUM());




                    // updateBuffer(buildMessage_AGREED);
                    if(buildMessage_AGREED != null && buildMessage_AGREED.getMessage_Type().equals("AGREED")){
                        Log.v("ENTERING THIIOIIIIS:"," MEssageType:"+buildMessage_AGREED.getMessage_Type());

                        for(messageBuilder msg_obj : buffer){
                            if(msg_obj.getMessage().equals(buildMessage_AGREED.getMessage())){
                                buffer.remove(msg_obj);
                                Log.v("BUFFER REMOVED OBJJ:"," "+buildMessage_AGREED.getRECEIVE_PORT());

                            }
                            buffer.add(buildMessage_AGREED);

                        }

                        int i= buildMessage_AGREED.getRECEIVE_PORT_NUM();
                        agreed_Seq[i] = buildMessage_AGREED.getCONSENSUS_SEQUENCE_NUM();

                    }






                    for(int i=0;i<agreed_Seq.length;i++){
                        Log.v("Agreed_Seq["+i+"]","->"+agreed_Seq[i]);

                    }

                    messageBuilder buildMessage_FINAL = null;

                    while (!buffer.isEmpty()) {
                        buildMessage_FINAL = buffer.poll();
                        publishProgress(buildMessage_FINAL.getMessage());
                        try {
                            dataOutputStream.writeUTF("WRITTEN");
                        } catch (Exception e) {
                            Log.d("Exception in printing:",buildMessage_FINAL.getMessage());

                        }

                    }
                    dataInputStream.close();
                    dataOutputStream.close();


                }


            }catch (Exception e){
                Log.e(TAG," NO INPUT TO SERVERSOCKET");
//                if(!DEAD_AVDS.contains(REMOTE_PORT_ARRAY[dead_control])){
//                    DEAD_AVDS.add(REMOTE_PORT_ARRAY[dead_control]);
//                }




            }




            return null;
        }







        protected void onProgressUpdate(String...strings) {


            Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
            ContentResolver contentResolver = getContentResolver();
            ContentValues mContentValues = new ContentValues();

            String strReceived = strings[0].trim();

            String key = Integer.toString(sequence);

            mContentValues.put("key", key);
            mContentValues.put("value", strReceived);
            sequence++;

            contentResolver.insert( mUri, mContentValues);


            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");

           TextView remoteTextView_check = (TextView) findViewById(R.id.textView2);
            remoteTextView_check.append("Send Port: "+send_Port_check+" Receive Port: "+receive_port_check + "\n");

            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");

           TextView localTextView_check = (TextView) findViewById(R.id.textView2);
            localTextView_check.append("Send Port: "+send_Port_check+" Receive Port: "+receive_port_check);



            return;
        }
    }

    /*
    private void remove_DEADAVDS_from_buffer() {
        if(!DEAD_AVDS.isEmpty()){
            for(String port : DEAD_AVDS){
                for(messageBuilder message_obj : buffer){
                    if(REMOTE_PORT_ARRAY[message_obj.getRECEIVE_PORT_NUM()].equals(port)){                //// CHANGE MADE at 5.45
                        buffer.remove(message_obj);
                    }
                }
            }
            Log.v("Updating buffer",DEAD_AVDS.toString());
        }


    }

    */

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

/*
    public void updateBuffer(messageBuilder messageBuilder){

        if(messageBuilder != null){
            if(messageBuilder.getMessage_Type().equals("AGREED")){
                for(messageBuilder msg_object : buffer){
                    if(msg_object.getMessage_ID() == messageBuilder.getMessage_ID()){
                        buffer.remove(msg_object);

                    }
                    buffer.add(messageBuilder);
                }
                agreed_Seq[messageBuilder.getRECEIVE_PORT_NUM()] = messageBuilder.getCONSENSUS_SEQUENCE_NUM();

            }
        }

    }

    */



    public void choose_prop_seq_num(String msgType,int RECEIVE_PORT_NUM){

        if(msgType.equals("PROPOSAL")){
            Log.v("chs_Prop_in:","Yes    msgType:"+msgType+"     Receive_port:"+RECEIVE_PORT_NUM);
            if(agreed_Seq[RECEIVE_PORT_NUM] > prop_Seq[RECEIVE_PORT_NUM]) {
                prop_Seq[RECEIVE_PORT_NUM] = agreed_Seq[RECEIVE_PORT_NUM] + 1;
            }
            else if(agreed_Seq[RECEIVE_PORT_NUM] <= prop_Seq[RECEIVE_PORT_NUM]){
                prop_Seq[RECEIVE_PORT_NUM]++;
            }
        }
        for(int i=0;i<prop_Seq.length;i++){
            Log.v("Agreed_Seq"+i,"  "+agreed_Seq[i]);

        }
        for(int i=0;i<prop_Seq.length;i++){
            Log.v("Prop_Seq"+i,"  "+prop_Seq[i]);

        }


        return;
    }

    public String unpack_PropSeqNum(String getPropSeqNum){
        String[] prop_Seq_num = null;
        if(getPropSeqNum.contains("@@")){
            prop_Seq_num = getPropSeqNum.split("@@");
        }
        Log.v("unpack_PropSeqNum: ",""+prop_Seq_num[0]);
        return prop_Seq_num[0];
    }





    public String[] unpack(String PACKET){
        String delimiter_used="@@";
        Log.v("Packed String:  ",""+PACKET);
        String[] unpack = PACKET.split(delimiter_used);
        for(int i=0;i<unpack.length;i++){
            Log.v("Data "+i," "+unpack[i]);
        }
        return unpack;
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String message = msgs[0];
                messageBuilder messageBuilder = new messageBuilder(message_ID++, message, message_Type, SENDING_PORT);


                Socket[] SOCKET_COLLECTION = new Socket[5];

                for(int i=0;i<DEAD_AVDS.size();i++){
                    Log.v("DEAD AVDS:",DEAD_AVDS.get(i));
                }

                for (int i = 0; i < REMOTE_PORT_ARRAY.length; i++) {
                    if(DEAD_AVDS.contains(REMOTE_PORT_ARRAY[i])){
                        continue;
                    }
                    else {
                        try {
                            receiver = REMOTE_PORT_ARRAY[i];
                            dead_control = i;
                            messageBuilder.setRECEIVE_PORT(Integer.parseInt(REMOTE_PORT_ARRAY[i]));
                            messageBuilder.setRECEIVE_PORT_NUM(i);
                            Log.v("Going in Client:", "Yes");

                            Log.v("messageBuilder: ", "Message ID: " + messageBuilder.getMessage_ID() + " Message : " + messageBuilder.getMessage() + " Message Type: " + messageBuilder.getMessage_Type() + " Sending Port:" + messageBuilder.getSEND_PORT() + "   Receive Port:" + messageBuilder.getRECEIVE_PORT() + "   Receive Port Num:" + messageBuilder.getRECEIVE_PORT_NUM());
                            Log.v("REMOTE_PORT: ", REMOTE_PORT_ARRAY[i]);
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(REMOTE_PORT_ARRAY[i])), 2000);
                            socket.setSoTimeout(2000);
                            Log.v("Is it working here?", "YEs");

                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                            SOCKET_COLLECTION[i] = socket;

                            messageBuilder.setRECEIVE_PORT(Integer.parseInt(REMOTE_PORT_ARRAY[i]));


                            String STRING_OF_VALUES = messageBuilder.getMessage_ID() + "@@" + messageBuilder.getMessage() + "@@" + messageBuilder.getMessage_Type() + "@@" + messageBuilder.getSEND_PORT() + "@@" + messageBuilder.getRECEIVE_PORT() + "@@" + messageBuilder.getRECEIVE_PORT_NUM() + "@@" + messageBuilder.getCONSENSUS_SEQUENCE_NUM();
                            Log.v("STRING_OF_VALUES:", STRING_OF_VALUES);
                            dataOutputStream.writeUTF(STRING_OF_VALUES);


                            String getPropSeqNum = null;

//                        try {
                            getPropSeqNum = dataInputStream.readUTF();
                            Log.v("getPropSeqNum->", getPropSeqNum);
                            messageBuilder.proposed.add(Integer.parseInt(unpack_PropSeqNum(getPropSeqNum)));    // HERE AT 12:32

                        }catch (Exception e) {
                            Log.d("Client:","Dead in Proposed. " + e.toString());


                            if (!DEAD_AVDS.contains(receiver)) {
                                DEAD_AVDS.add(receiver);
                            }

                            Log.d("Dead update in client: ",DEAD_AVDS.toString());

                        }
                    }


                }


                messageBuilder.CONSENSUS_SEQUENCE_NUM = Collections.max(messageBuilder.proposed);
                messageBuilder.setMessage_Type("AGREED");   //Change name (Delete)

                Log.v("Check Agreed msg: "," Message ID:"+messageBuilder.getMessage_ID()+"     Message:"+messageBuilder.getMessage()+"     MessageType:"+messageBuilder.getMessage_Type()+"     SendPort:"+messageBuilder.getSEND_PORT()+"    ReceivePort:"+messageBuilder.getRECEIVE_PORT()+"     ReceiveportNUM:"+messageBuilder.getRECEIVE_PORT_NUM()+"    ConsensusSeqNum:"+messageBuilder.getCONSENSUS_SEQUENCE_NUM());


                // AGREED
                for (int i = 0; i < REMOTE_PORT_ARRAY.length; i++) {
                    if(DEAD_AVDS.contains(REMOTE_PORT_ARRAY[i])){
                        continue;
                    }
                    else {
                        receiver = REMOTE_PORT_ARRAY[i];
                        messageBuilder.setRECEIVE_PORT(Integer.parseInt(REMOTE_PORT_ARRAY[i]));
                        messageBuilder.setRECEIVE_PORT_NUM(i);

                        Log.v("ReceivePort & NUm: ","  ReceivePort: "+messageBuilder.getRECEIVE_PORT()+"    ReceivePortNum:"+messageBuilder.getRECEIVE_PORT_NUM());

                        String STRING_OF_VALUES = messageBuilder.getMessage_ID()+"@@"+messageBuilder.getMessage()+"@@"+messageBuilder.getMessage_Type()+"@@"+messageBuilder.getSEND_PORT()+"@@"+messageBuilder.getRECEIVE_PORT()+"@@"+messageBuilder.getRECEIVE_PORT_NUM()+"@@"+messageBuilder.getCONSENSUS_SEQUENCE_NUM();

                        DataOutputStream dataOutputStream = new DataOutputStream(SOCKET_COLLECTION[i].getOutputStream());
                        dataOutputStream.writeUTF(STRING_OF_VALUES); // Control shifts here           //CHecked till here


                        DataInputStream dataInputStream = new DataInputStream(SOCKET_COLLECTION[i].getInputStream());
                        String get_Final_Message = null;
                        try {
                            get_Final_Message = dataInputStream.readUTF();
                        } catch (Exception e) {
                        }

                        if (get_Final_Message.equals("WRITTEN")) {
                            try {
                                dataOutputStream.close();
                                dataInputStream.close();
                                SOCKET_COLLECTION[i].close();
                            } catch (Exception e) {
                                Log.d("Client: ","Dead in agreed");


                                if(!DEAD_AVDS.contains(receiver)){
                                    DEAD_AVDS.add(receiver);
                                }
                            }
                        }
                    }


                }
            }
            catch (Exception e){
                Log.e(TAG,"ClientTask socket IOException");
//                if(!DEAD_AVDS.contains(SENDING_PORT)){
//                    DEAD_AVDS.add(SENDING_PORT);
//                }
            }












            return null;
        }
    }







}