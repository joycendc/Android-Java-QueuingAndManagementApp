package com.giligans.queueapp.models;

public class QueueModel implements Comparable, Cloneable {
    public String queue_id, id, name, status;

    public QueueModel(String queue_id, String id, String name, String status) {
        this.queue_id = queue_id;
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public String getQueueId() {
        return queue_id;
    }

    public void setQueueId(String queue_id) {
        this.queue_id = queue_id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int compareTo(Object o) {
        QueueModel compare = (QueueModel) o;
        if (compare.id == this.id && compare.name.equals(this.name)) {
            return 0;
        }
        return 1;
    }

    @Override
    public QueueModel clone() {
        QueueModel clone;
        try {
            clone = (QueueModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); //should not happen
        }
        return clone;
    }
}
