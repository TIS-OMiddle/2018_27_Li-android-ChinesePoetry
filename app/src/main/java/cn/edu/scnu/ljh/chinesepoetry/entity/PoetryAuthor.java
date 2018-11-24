package cn.edu.scnu.ljh.chinesepoetry.entity;

public class PoetryAuthor {
    private int id;
    private String name;
    private String intro;
    private String dynasty;

    public String getNameAndDynasty() {
        return name + "(" + dynasty + ")";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        if (intro == null | intro.length() == 0) {
            return "暂无简介";
        }
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getDynasty() {
        return dynasty;
    }

    public void setDynasty(String dynasty) {
        this.dynasty = dynasty;
    }
}
