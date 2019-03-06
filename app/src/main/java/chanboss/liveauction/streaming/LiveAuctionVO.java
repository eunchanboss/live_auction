package chanboss.liveauction.streaming;

public class LiveAuctionVO {
    String liveId;
    String liveName;
    String userId;
    int startPrice;
    int nowPrice;
    String nowPriceUser;
    String userName;
    String userImg;
    String pdImg;
    String liveStat;

    public LiveAuctionVO(String liveId, String liveName, String userId, int startPrice, int nowPrice, String nowPriceUser, String userName, String userImg,String pdImg,String liveStat) {
        this.liveId = liveId;
        this.liveName = liveName;
        this.userId = userId;
        this.startPrice = startPrice;
        this.nowPrice = nowPrice;
        this.nowPriceUser = nowPriceUser;
        this.userName = userName;
        this.userImg = userImg;
        this.pdImg = pdImg;
        this.liveStat = liveStat;
    }

    public String getLiveStat() {
        return liveStat;
    }

    public void setLiveStat(String liveStat) {
        this.liveStat = liveStat;
    }

    public String getPdImg() {
        return pdImg;
    }

    public void setPdImg(String pdImg) {
        this.pdImg = pdImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public int getNowPrice() {
        return nowPrice;
    }

    public void setNowPrice(int nowPrice) {
        this.nowPrice = nowPrice;
    }

    public String getNowPriceUser() {
        return nowPriceUser;
    }

    public void setNowPriceUser(String nowPriceUser) {
        this.nowPriceUser = nowPriceUser;
    }

    public String getLiveId() {
        return liveId;
    }

    public void setLiveId(String liveId) {
        this.liveId = liveId;
    }

    public String getLiveName() {
        return liveName;
    }

    public void setLiveName(String liveName) {
        this.liveName = liveName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(int startPrice) {
        this.startPrice = startPrice;
    }
}
