package chanboss.liveauction.chatting.chatroom;

public class ChatroomVO {
    String chatroomId;
    String lastMsg;
    String lastMsgDate;
    String lastMsgId;
    String reciverId;

    public ChatroomVO(String chatroomId, String lastMsg, String lastMsgDate,String reciverId) {
        this.chatroomId = chatroomId;
        this.lastMsg = lastMsg;
        this.lastMsgDate = lastMsgDate;
        this.reciverId = reciverId;
    }

    public String getReciverId() {
        return reciverId;
    }

    public void setReciverId(String reciverId) {
        this.reciverId = reciverId;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getLastMsgDate() {
        return lastMsgDate;
    }

    public void setLastMsgDate(String lastMsgDate) {
        this.lastMsgDate = lastMsgDate;
    }

    public String getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(String lastMsgId) {
        this.lastMsgId = lastMsgId;
    }
}
