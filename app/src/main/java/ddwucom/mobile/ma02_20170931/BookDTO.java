package ddwucom.mobile.ma02_20170931;

import android.text.Html;
import android.text.Spanned;

import java.io.Serializable;

public class BookDTO implements Serializable {
    private int _id;
    private String type;
    private String imgType;
    private String imgUrl;
    private String title;
    private String author;
    private String publisher;
    private String description;
    private String paragraph;
    private String context;
    private String startDay;
    private String endDay;
    private String imageLink;
    private String imageFileName;       // 외부저장소에 저장했을 때의 파일명

    public BookDTO() {
        this.imageFileName = null;
    }// 생성 시에는 외부저장소에 파일이 없으므로 null로 초기화


    /* 읽는중, 위시 리스트 + 이미지 */
    public BookDTO(int _id, String title, String author, String publisher, String imgType, String imgUrl, String startDay){
        this._id = _id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.imgType = imgType;
        this.imgUrl = imgUrl;
        this.startDay = startDay;
    }
    /* 독서기록 리스트에 쓰여질 DTO */
    public BookDTO(int _id, String title, String author, String publisher,
                   String imgType, String imgUrl, String paragraph, String context, String startDay, String endDay){
        this._id = _id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.imgType = imgType;
        this.imgUrl = imgUrl;
        this.paragraph = paragraph;
        this.context = context;
        this.startDay = startDay;
        this.endDay = endDay;
    }
    /* 독서기록 세부정보에 쓰여질 DTO */
    public BookDTO(int _id, String title, String author, String publisher,
                   String paragraph, String context, String startDay, String endDay){
        this._id = _id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.paragraph = paragraph;
        this.context = context;
        this.startDay = startDay;
        this.endDay = endDay;
    }

    public int get_id(){ return _id; }
    public void set_id(int _id){ this._id = _id; }


    public String getTitle() {
        Spanned spanned = Html.fromHtml(title);
        return spanned.toString();
        //   return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getImgType() { return imgType; }
    public void setImgType(String imgType) { this.imgType = imgType; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public String getParagraph() { return paragraph; }
    public void setParagraph(String paragraph) { this.paragraph = paragraph; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }


    public String getStartDay() { return startDay; }
    public void setStartDay(String startDay) { this.startDay = startDay; }

    public String getEndDay() { return endDay; }
    public void setEndtDay(String endDay) { this.endDay = endDay; }

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



    @Override
    public String toString() {
        return  _id + ": " + title + " (" + author + ')';
    }

}
