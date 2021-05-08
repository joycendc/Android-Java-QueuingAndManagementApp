package com.giligans.queueapp.models;

public class FoodModel {
    String name;
    String description;
    String price;
    int imageUrl;
    int bigimageurl;

    public FoodModel(String name, String description, String price, int
            imageUrl, int bigimageurl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.bigimageurl = bigimageurl;
    }

    public int getBigimageurl() {
        return bigimageurl;
    }

    public void setBigimageurl(int bigimageurl) {
        this.bigimageurl = bigimageurl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(int imageUrl) {
        this.imageUrl = imageUrl;
    }
}