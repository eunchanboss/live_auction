package chanboss.liveauction.chatting.chatin;

public class ChatInVO {
    String chatroomId = null;
    String msg = null;
    String msgId = null;
    String msgDate = null;
    String senderId = null;
    String reciverId = null;
    String reciverNm = null;
    String imgMsg = null;
    String checkYn = null;


    public ChatInVO(String chatroomId, String msg, String senderId, String reciverId,String msgDate,String imgMsg) {
        this.chatroomId = chatroomId;
        this.msg = msg;
        this.senderId = senderId;
        this.reciverId = reciverId;
        this.msgDate = msgDate;
        this.imgMsg = imgMsg;
    }

    public ChatInVO(){

    }

    public String getReciverNm() {
        return reciverNm;
    }

    public void setReciverNm(String reciverNm) {
        this.reciverNm = reciverNm;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgDate() {
        return msgDate;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReciverId() {
        return reciverId;
    }

    public void setReciverId(String reciverId) {
        this.reciverId = reciverId;
    }

    public String getImgMsg() {
        return imgMsg;
    }

    public void setImgMsg(String imgMsg) {
        this.imgMsg = imgMsg;
    }

    public String getCheckYn() {
        return checkYn;
    }

    public void setCheckYn(String checkYn) {
        this.checkYn = checkYn;
    }
}
