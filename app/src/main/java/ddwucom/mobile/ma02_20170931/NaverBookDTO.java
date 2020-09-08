package ddwucom.mobile.ma02_20170931;

import android.text.Html;
import android.text.Spanned;

import java.io.Serializable;

public class NaverBookDTO implements Serializable {

    private int _id;
    private String title;
    private String author;
    private String publisher;
    private String total;
    private String link;
    private String description;
    private String imageLink;
    private String imageFileName;       // 외부저장소에 저장했을 때의 파일명

    public NaverBookDTO() {
        this.imageFileName = null;      // 생성 시에는 외부저장소에 파일이 없으므로 null로 초기화
    }

    public int get_id() { return _id; }
    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTotal() {
        return total;
    }
    public void setTotal(String total) {
        this.total = total;
    }

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageLink() {
        return imageLink;
    }
    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getImageFileName() {
        return imageFileName;
    }
    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getTitle() {
        Spanned spanned = Html.fromHtml(title);
        return spanned.toString();
//        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return  _id + ": " + title + " (" + author + ')';
    }
}
