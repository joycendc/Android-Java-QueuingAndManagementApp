package com.giligans.queueapp.models;

public class CustomerModel implements Comparable, Cloneable {
    public String id, name;
    public CustomerModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Object o) {
        CustomerModel compare = (CustomerModel) o;
        if (compare.id == this.id && compare.name.equals(this.name)) {
            return 0;
        }
        return 1;
    }

    @Override
    public CustomerModel clone() {
        CustomerModel clone;
        try {
            clone = (CustomerModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); //should not happen
        }
        return clone;
    }
}
