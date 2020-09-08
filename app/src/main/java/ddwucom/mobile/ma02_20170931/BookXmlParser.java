package ddwucom.mobile.ma02_20170931;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

public class BookXmlParser {

    public enum TagType { NONE, TITLE, AUTHOR, PUBLISHER, IMAGE, TOTAL, LINK, DESCRIPTION }

    public BookXmlParser() {
    }

    public ArrayList<NaverBookDTO> parse(String xml) {

        ArrayList<NaverBookDTO> resultList = new ArrayList();
        NaverBookDTO dto = null;

        TagType tagType = TagType.NONE;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("item")) {
                            dto = new NaverBookDTO();
                        } else if (parser.getName().equals("title")) {
                            if (dto != null) tagType = TagType.TITLE;
                        } else if (parser.getName().equals("author")) {
                            if (dto != null) tagType = TagType.AUTHOR;
                        } else if (parser.getName().equals("publisher")) {
                            if (dto != null) tagType = TagType.PUBLISHER;
                        }else if(parser.getName().equals("image")){
                            if (dto != null) tagType = TagType.IMAGE;
                        }else if(parser.getName().equals("total")){
                            if (dto != null) tagType = TagType.TOTAL;
                        }else if(parser.getName().equals("link")){
                            if (dto != null) tagType = TagType.LINK;
                        }else if(parser.getName().equals("description")){
                            if (dto != null) tagType = TagType.DESCRIPTION;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("item")) {
                            resultList.add(dto);
                            dto = null;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        switch(tagType) {
                            case TITLE:
                                String title = removeTag(parser.getText());
                                dto.setTitle(title);
                                break;
                            case AUTHOR:
                                String author = removeTag(parser.getText());
                                dto.setAuthor(author);
                                break;
                            case PUBLISHER:
                                String publisher = removeTag(parser.getText());
                                dto.setPublisher(publisher);
                                break;
                            case IMAGE:
                                String imgLink = removeTag(parser.getText());
                                dto.setImageLink(imgLink);
                                break;
                            case TOTAL:
                                String total = removeTag(parser.getText());
                                dto.setTotal(total);
                                break;
                            case LINK:
                                String link = removeTag(parser.getText());
                                dto.setLink(link);
                                break;
                            case DESCRIPTION:
                                String description = removeTag(parser.getText());
                                dto.setDescription(description);
                                break;
                        }
                        tagType = TagType.NONE;
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
    public String removeTag(String html) throws Exception {
        return html.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
    }
}

