package cn.edu.scnu.ljh.chinesepoetry.entity;

public class Poetry {
    private int id;
    private int author_id;
    private String title;
    private String content;
    private String yunlv_rule;
    private String author;
    private String dynasty;

    public String getAuthorAndTitle() {
        return title + "(" + author + ")";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(int author_id) {
        this.author_id = author_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getYunlv_rule() {
        return yunlv_rule;
    }

    public void setYunlv_rule(String yunlv_rule) {
        this.yunlv_rule = yunlv_rule;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDynasty() {
        return dynasty;
    }

    public void setDynasty(String dynasty) {
        this.dynasty = dynasty;
    }
}

