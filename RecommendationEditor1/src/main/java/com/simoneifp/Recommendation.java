package com.simoneifp;

import com.opencsv.bean.CsvBindByPosition;
// TODO, change this to name to make code a little bit more readable.

enum Category {
    MEDICAL,
    EDUCATION,
    OTHER
}

public class Recommendation {
    @CsvBindByPosition(position = 0)
    private String title;
    @CsvBindByPosition(position = 1)
    private String secondary_title;

    @CsvBindByPosition(position = 2)
    private String _archived;

    @CsvBindByPosition(position = 3)
    private String _category;

    @CsvBindByPosition(position = 4)
    private String body;


    public Boolean isArchived() {
        return Boolean.valueOf(_archived);
    }

    public String getStringArchived() {return this._archived; }

    public Category getCategory() {
        return Category.valueOf(this._category);
    }

    public String getStringCategory() {return this._category;}

    public void setBody(String body) {
        this.body = body;
    }

    public void setCategory(Category category) {
        this._category = category.toString();
    }

    public void setArchived(Boolean bool) {
        this._archived = bool.toString();
    }

    public void setSecondary(String s) {
        this.secondary_title = s;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public String getTitle() { return this.title;}
    public String getSecondaryTitle() { return this.secondary_title; }
    public String getBody() {return this.body; }

    @Override
    public String toString() {
        String text = (isArchived() ? "[ARCHIVED]\n" : "") + getTitle() + "\n" + getSecondaryTitle() + "\n" + getStringCategory() + "\n";
        if (getBody().startsWith("\"") && getBody().endsWith("\"")) {
            text += getBody().substring(1, getBody().length() - 1);
        } else {
            text += getBody();
        }
        return text;
    }
    
    public void init(String title, String secondary_title, boolean archived, Category category, String body) {
        // This is the manual init function. This can't be the standard because it interferes with the CSV data model
        // black magic :sparkles:
        // This can be used to set all fields at once inline, if needed.

        this.setTitle(title);
        this.setSecondary(secondary_title);
        this.setArchived(archived);
        this.setCategory(category);
        this.setBody(body);
    }




}


