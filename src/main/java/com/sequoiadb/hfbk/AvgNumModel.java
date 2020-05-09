package com.sequoiadb.hfbk;

public class AvgNumModel {
    //总连接数
    private int totalNumConnects;
    //每秒数据读次数
    private double averageDataRead;
    //每秒索引读次数
    private double averageIndexRead;
    //每秒数据写次数
    private double averageDataWrite;
    //每秒索引写次数
    private double averageIndexWrite;
    //每秒更新记录条数
    private double averageUpdate;
    //每秒删除记录条数
    private double averageDelete;
    //每秒插入记录条数
    private double averageInsert;
    //每秒复制更新记录条数
    private double averageReplUpdate;
    //每秒复制删除记录条数
    private double averageReplDelete;
    //每秒复制插入记录条数
    private double averageReplInsert;
    //每秒查询命中记录条数
    private double averageSelect;
    //每秒读记录条数
    private double averageRead;


    public int getTotalNumConnects() {
        return totalNumConnects;
    }

    public void setTotalNumConnects(int totalNumConnects) {
        this.totalNumConnects = totalNumConnects;
    }

    public double getAverageDataRead() {
        return averageDataRead;
    }

    public void setAverageDataRead(double averageDataRead) {
        this.averageDataRead = averageDataRead;
    }

    public double getAverageIndexRead() {
        return averageIndexRead;
    }

    public void setAverageIndexRead(double averageIndexRead) {
        this.averageIndexRead = averageIndexRead;
    }

    public double getAverageDataWrite() {
        return averageDataWrite;
    }

    public void setAverageDataWrite(double averageDataWrite) {
        this.averageDataWrite = averageDataWrite;
    }

    public double getAverageIndexWrite() {
        return averageIndexWrite;
    }

    public void setAverageIndexWrite(double averageIndexWrite) {
        this.averageIndexWrite = averageIndexWrite;
    }

    public double getAverageUpdate() {
        return averageUpdate;
    }

    public void setAverageUpdate(double averageUpdate) {
        this.averageUpdate = averageUpdate;
    }

    public double getAverageDelete() {
        return averageDelete;
    }

    public void setAverageDelete(double averageDelete) {
        this.averageDelete = averageDelete;
    }

    public double getAverageInsert() {
        return averageInsert;
    }

    public void setAverageInsert(double averageInsert) {
        this.averageInsert = averageInsert;
    }

    public double getAverageReplUpdate() {
        return averageReplUpdate;
    }

    public void setAverageReplUpdate(double averageReplUpdate) {
        this.averageReplUpdate = averageReplUpdate;
    }

    public double getAverageReplDelete() {
        return averageReplDelete;
    }

    public void setAverageReplDelete(double averageReplDelete) {
        this.averageReplDelete = averageReplDelete;
    }

    public double getAverageReplInsert() {
        return averageReplInsert;
    }

    public void setAverageReplInsert(double averageReplInsert) {
        this.averageReplInsert = averageReplInsert;
    }

    public double getAverageSelect() {
        return averageSelect;
    }

    public void setAverageSelect(double averageSelect) {
        this.averageSelect = averageSelect;
    }

    public double getAverageRead() {
        return averageRead;
    }

    public void setAverageRead(double averageRead) {
        this.averageRead = averageRead;
    }
}
