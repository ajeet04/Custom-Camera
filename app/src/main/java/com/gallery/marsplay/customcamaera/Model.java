package com.gallery.marsplay.customcamaera;

public class Model {
    private String thumb_image,original_image,date;
    public Model(){}
    public Model(String thumb_image,String original_image,String date ){
        this.thumb_image=thumb_image;
        this.original_image=original_image;
        this.date=date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOriginal_image() {
        return original_image;
    }

    public void setOriginal_image(String original_image) {
        this.original_image = original_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getThumb_image() {
        return thumb_image;
    }
}
