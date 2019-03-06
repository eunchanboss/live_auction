package chanboss.liveauction.notice.img;


import android.net.Uri;

public class ImgVO {
    Uri imgUri;

    public ImgVO(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }
}
