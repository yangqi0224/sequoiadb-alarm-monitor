package com.sequoiadb.hfbk;

/**
 * @author yangqi
 * @version 1.0
 * @class com.sequoiadb.hfbk.TotalNumModel
 * @date 2020-04-14 11:42
 */
public class TotalNumModel {


    //总连接数
    private int totalNumConnects;
    private double dataRead;
    private double indexRead;
    private double dataWrite;
    private double indexWrite;
    private double update;
    private double delete;
    private double insert;
    private double replUpdate;
    private double replDelete;
    private double replInsert;
    private double select;
    private double read;


    public int getTotalNumConnects() {
        return totalNumConnects;
    }

    public void setTotalNumConnects(int totalNumConnects) {
        this.totalNumConnects = totalNumConnects;
    }
    public double getDataRead() {
        return dataRead;
    }

    public void setDataRead(double dataRead) {
        this.dataRead = dataRead;
    }

    public double getIndexRead() {
        return indexRead;
    }

    public void setIndexRead(double indexRead) {
        this.indexRead = indexRead;
    }

    public double getDataWrite() {
        return dataWrite;
    }

    public void setDataWrite(double dataWrite) {
        this.dataWrite = dataWrite;
    }

    public double getIndexWrite() {
        return indexWrite;
    }

    public void setIndexWrite(double indexWrite) {
        this.indexWrite = indexWrite;
    }

    public double getUpdate() {
        return update;
    }

    public void setUpdate(double update) {
        this.update = update;
    }

    public double getDelete() {
        return delete;
    }

    public void setDelete(double delete) {
        this.delete = delete;
    }

    public double getInsert() {
        return insert;
    }

    public void setInsert(double insert) {
        this.insert = insert;
    }

    public double getReplUpdate() {
        return replUpdate;
    }

    public void setReplUpdate(double replUpdate) {
        this.replUpdate = replUpdate;
    }

    public double getReplDelete() {
        return replDelete;
    }

    public void setReplDelete(double replDelete) {
        this.replDelete = replDelete;
    }

    public double getReplInsert() {
        return replInsert;
    }

    public void setReplInsert(double replInsert) {
        this.replInsert = replInsert;
    }

    public double getSelect() {
        return select;
    }

    public void setSelect(double select) {
        this.select = select;
    }

    public double getRead() {
        return read;
    }

    public void setRead(double read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return "TotalNumModel{" +
                "totalNumConnects=" + totalNumConnects +
                ", dataRead=" + dataRead +
                ", indexRead=" + indexRead +
                ", dataWrite=" + dataWrite +
                ", indexWrite=" + indexWrite +
                ", update=" + update +
                ", delete=" + delete +
                ", insert=" + insert +
                ", replUpdate=" + replUpdate +
                ", replDelete=" + replDelete +
                ", replInsert=" + replInsert +
                ", select=" + select +
                ", read=" + read +
                '}';
    }
}
