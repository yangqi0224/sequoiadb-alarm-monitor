package com.sequoiadb.hfbk;

import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Sequoiadb;
import org.bson.BSONObject;

/**
 * @author yangqi
 * @version 1.0
 * @class com.sequoiadb.hfbk.GetAvgNum
 * @date 2020-04-14 11:45
 */
public class GetAvgNum {

    private TotalNumModel totalNumModel;
    private static int MAX_RUNNING_SESSION_NUM = 200;
    private static int MAX_READ_RATIO = 0;
    /**
     *
     * @param db
     */
    public void getAVG(Sequoiadb db){
        BSONObject curr;
        long sleepTime = 10*1000;
        try{
            curr = db.getSnapshot(Sequoiadb.SDB_SNAP_DATABASE, "", "", "").getCurrent();
            double dataRead = (double)curr.get("TotalDataRead");
            double indexRead = (double)curr.get("TotalIndexRead");
            double dataWrite = (double)curr.get("TotalDateWrite");
            double indexWrite = (double)curr.get("TotalIndexWrite");
            double update = (double)curr.get("TotalUpdate");
            double delete = (double)curr.get("TotalDelete");
            double insert = (double)curr.get("TotalInsert");
            double replUpdate = (double)curr.get("ReplUpdate");
            double replDelete = (double)curr.get("ReplDelete");
            double replInsert = (double)curr.get("ReplInsert");
            double select = (double)curr.get("TotalSelect");
            double read = (double)curr.get("TotalRead");
            System.out.println("sleep");
            Thread.sleep(sleepTime);
            System.out.println("wake up");
            BSONObject curr1 = db.getSnapshot(Sequoiadb.SDB_SNAP_DATABASE, "", "", "").getCurrent();
            double dataRead2 = (double)curr1.get("TotalDataRead");
            double indexRead2 = (double)curr1.get("TotalIndexRead");
            double dataWrite2 = (double)curr1.get("TotalDateWrite");
            double indexWrite2 = (double)curr1.get("TotalIndexWrite");
            double update2 = (double)curr1.get("TotalUpdate");
            double delete2 = (double)curr1.get("TotalDelete");
            double insert2 = (double)curr1.get("TotalInsert");
            double replUpdate2 = (double)curr1.get("ReplUpdate");
            double replDelete2 = (double)curr1.get("ReplDelete");
            double replInsert2 = (double)curr1.get("ReplInsert");
            double select2 = (double)curr1.get("TotalSelect");
            double read2 = (double)curr1.get("TotalRead");

            double dataSpeed = (dataRead2-dataRead)/sleepTime;
            double indexSpeed = (indexRead2-indexRead)/sleepTime;
            if (indexSpeed == 0)
                indexSpeed = 1;
            double ratio = dataSpeed/indexSpeed*100;
            if (ratio>=MAX_READ_RATIO){
                //数据读与索引读比例大于1000，告警，输出以下信息
                System.out.println("当前每秒数据读次数："+dataSpeed +"，当前每秒索引读次数："+indexSpeed+
                        "，数据读与索引读比例"+ratio);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setTotalNum(BSONObject curr){

        totalNumModel = new TotalNumModel();
        totalNumModel.setTotalNumConnects(Integer.valueOf(curr.get("TotalConnectNum").toString()));
        totalNumModel.setDataRead((double)curr.get("TotalDataRead"));
        totalNumModel.setIndexRead((double)curr.get("TotalIndexRead"));
        totalNumModel.setDataWrite((double)curr.get("TotalDateWrite"));
        totalNumModel.setIndexWrite((double)curr.get("TotalIndexWrite"));
        totalNumModel.setUpdate((double)curr.get("TotalUpdate"));
        totalNumModel.setDelete((double)curr.get("TotalDelete"));
        totalNumModel.setInsert((double)curr.get("TotalInsert"));
        totalNumModel.setReplUpdate((double)curr.get("ReplUpdate"));
        totalNumModel.setReplDelete((double)curr.get("ReplDelete"));
        totalNumModel.setReplInsert((double)curr.get("ReplInsert"));
        totalNumModel.setSelect((double)curr.get("TotalSelect"));
        totalNumModel.setRead((double)curr.get("TotalRead"));
    }
}
