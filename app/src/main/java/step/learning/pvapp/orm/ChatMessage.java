package step.learning.pvapp.orm;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ChatMessage {
    private String id;
    private String author;
    private String text;
    private Date moment;
    private Object tag;

    private static final SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static ChatMessage fromJson(JSONObject jsonObject) throws JSONException, ParseException {
        return new ChatMessage(
                jsonObject.getString("id"),
                jsonObject.getString("author"),
                jsonObject.getString("text"),
                sqlFormat.parse(jsonObject.getString("moment"))
        );
    }

    public ChatMessage() {
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public ChatMessage(String id, String author, String text, Date moment) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.moment = moment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getMoment() {
        return moment;
    }

    public void setMoment(Date moment) {
        this.moment = moment;
    }
}
