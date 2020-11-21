package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.ArrayList;
import java.util.List;

public class messageBuilder {

    int message_ID;
    String delimiter="|";
    String message;
    String message_Type;
    String SEND_PORT;
    int RECEIVE_PORT;
    int RECEIVE_PORT_NUM;
    int CONSENSUS_SEQUENCE_NUM; //Agreed_Sequence (Delete)
    List<Integer> proposed = new ArrayList<Integer>();

    public messageBuilder(){

    }


    public messageBuilder(int message_ID,String message,String message_Type,String SEND_PORT){
        this.message_ID=message_ID;
        this.message=message;
        this.message_Type=message_Type;
        this.SEND_PORT=SEND_PORT;

    }

    public messageBuilder(int message_ID,String message,String message_Type,String SEND_PORT,int RECEIVE_PORT,int RECEIVE_PORT_NUM,int CONSENSUS_SEQUENCE_NUM){
        this.message_ID=message_ID;
        this.message=message;
        this.message_Type=message_Type;
        this.SEND_PORT=SEND_PORT;
        this.RECEIVE_PORT=RECEIVE_PORT;
        this.RECEIVE_PORT_NUM=RECEIVE_PORT_NUM;
        this.CONSENSUS_SEQUENCE_NUM=CONSENSUS_SEQUENCE_NUM;

    }







    public int getMessage_ID(){
        return message_ID;
    }

    public String getMessage() {
        return message;
    }

    public String getSEND_PORT(){
        return SEND_PORT;
    }

    public int getRECEIVE_PORT(){
        return RECEIVE_PORT;
    }

    public String getMessage_Type(){
        return message_Type;
    }

    public int getCONSENSUS_SEQUENCE_NUM(){
        return CONSENSUS_SEQUENCE_NUM;
    }

    public void setMessage_Type(String message_Type) {
        this.message_Type = message_Type;
    }

    public void setRECEIVE_PORT(int RECEIVE_PORT) {
        this.RECEIVE_PORT = RECEIVE_PORT;
    }

    public int getRECEIVE_PORT_NUM() {
        return RECEIVE_PORT_NUM;
    }

    public void setRECEIVE_PORT_NUM(int RECEIVE_PORT_NUM) {
        this.RECEIVE_PORT_NUM = RECEIVE_PORT_NUM;
    }
}