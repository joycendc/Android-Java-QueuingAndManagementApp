package com.giligans.queueapp.models;

public class FoodModel {
    String name, description, price, imageUrl, bigimageurl;

    public FoodModel(String name, String description, String price, String
            imageUrl, String bigimageurl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.bigimageurl = bigimageurl;
    }

    public String getBigimageurl() {
        return bigimageurl;
    }

    public void setBigimageurl(String bigimageurl) {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}