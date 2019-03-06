package chanboss.liveauction.notice;



public class PostVO {

    String userId;
    String userName;
    String userImg;
    String auctionContents;
    String auctionContentsId;
    String auctionContentsImg;
    String startTime;
    String lastTime;
    String startPrice;
    String nowPrice;
    String nowBuy;
    String pdImg;
    String auctionTitle;
    String mainCategory;
    String lastPrice;



    public PostVO(String pdImg, String mainCategory, String auctionTitle, String userId, String userName, String userImg, String auctionContents, String auctionContentsId, String auctionContentsImg, String lastTime, String startPrice, String nowBuy, String lastPrice) {
        this.pdImg = pdImg;
        //받아옴
        this.userId = userId;
        //받아옴
        this.userName = userName;
        //받아옴
        this.userImg = userImg;
        //받아옴
        this.auctionContents = auctionContents;
        //받아옴
        this.auctionContentsId = auctionContentsId;
        //받아옴
        this.auctionContentsImg = auctionContentsImg;

        //받아옴
        this.lastTime = lastTime;
        //받아옴
        this.startPrice = startPrice;
        //현재 가격은 계속 갱신

        //받아옴
        this.nowBuy = nowBuy;
        //받아옴
        this.auctionTitle = auctionTitle;
        //받아옴
        this.mainCategory = mainCategory;

        this.lastPrice = lastPrice;
    }
    public String getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;
    }
    public String getPdImg() {
        return pdImg;
    }

    public void setPdImg(String pdImg) {
        this.pdImg = pdImg;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getAuctionTitle() {
        return auctionTitle;
    }

    public void setAuctionTitle(String auctionTitle) {
        this.auctionTitle = auctionTitle;
    }

    public String getNowBuy() {
        return nowBuy;
    }

    public void setNowBuy(String nowBuy) {
        this.nowBuy = nowBuy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getAuctionContents() {
        return auctionContents;
    }

    public void setAuctionContents(String auctionContents) {
        this.auctionContents = auctionContents;
    }

    public String getAuctionContentsId() {
        return auctionContentsId;
    }

    public void setAuctionContentsId(String auctionContentsId) {
        this.auctionContentsId = auctionContentsId;
    }

    public String getAuctionContentsImg() {
        return auctionContentsImg;
    }

    public void setAuctionContentsImg(String auctionContentsImg) {
        this.auctionContentsImg = auctionContentsImg;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getNowPrice() {
        return nowPrice;
    }

    public void setNowPrice(String nowPrice) {
        this.nowPrice = nowPrice;
    }

    public String getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(String startPrice) {
        this.startPrice = startPrice;
    }
}
