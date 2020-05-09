package com.sequoiadb.hfbk;

import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Node;
import com.sequoiadb.base.Sequoiadb;
import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.types.BasicBSONList;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;

/**
 * @author yangqi
 * @date
 * @name
 * @desc 节点监控
 */
public class NodeMonitor {

    //sdb监控用户名和密码
    private static String sdbUser = "";
    private static String sdbPwd = "";


    //告警阈值
    private static long MAX_LSN_DIFFER = 1024*1024;
    private static double MAX_SLOW_QUERY = 60;
    private static long MAX_TRANS_DIFFER = 100*1024*1024;
    private static double LONG_SESSION = 10;
    private static double LONG_SESSION_NUM = 10;
    private static int MAX_SESSION_NUM = 2000%1;
    private static int MAX_RUNNING_SESSION_NUM = 200%1;
    private static int MAX_NODE_SESSION_NUM = 200%1;
    private static int MAX_NODE_RUNNING_SESSION_NUM = 20%1;
    private static double MAX_SPACE_USE = 0.1;
    private static int MAX_READ_RATIO = 1000;
    private static int MAX_COORD_CONN = 200;

    //在监控会话时，标志是否直连数据节点
    private static  boolean flag = false;

    //主节点情况
    private static ConcurrentHashMap<String,String> primaryNodes = new ConcurrentHashMap<>();

    /**
     * 慢查询监控
     * @see com.sequoiadb.hfbk.NodeMonitor#slow_query
     * @param db
     */
    public void checkSlowQuery(Sequoiadb db){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            DBCursor cursor = db.getSnapshot(Sequoiadb.SDB_SNAP_SESSIONS, " {'Status':'Running','LastOpType':'Query'}", "", "");
            while (cursor.hasNext()){
                BSONObject obj = cursor.getNext();
                String begin = (String) obj.get("LastOpBegin");
                Date now = new Date();
                double queryTime = calculatetimeGapSecond(begin, now.toString(),dateFormat);
                System.out.println();
                if(queryTime > MAX_SLOW_QUERY){
                    //告警,输出以下内容
                    String nodeName = (String) obj.get("NodeName");
                    String sessionID = (String) obj.get("SessionId");
                    System.out.println("节点名称："+nodeName+" SessionID:"+sessionID +" 查询时长："+queryTime);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 监测主备节点的LSN差异，大于 LSN_Differ则报警
     * @see com.sequoiadb.hfbk.NodeMonitor#LSN_Differ
     * @param db
     */
    public void checkLSN(Sequoiadb db){
        DBCursor cursor;
        List<BSONObject> groupNames=getGroup(db);
        try{
            for (BSONObject group:groupNames){//遍历所有复制组
                String name = (String) group.get("GroupName");
                if (!(name.equals("SYSCoord")||name.equals("SYSCatalogGroup"))){//跳过编目和协调节点组
                    BasicBSONList list = (BasicBSONList) group.get("Group");
                    for (int i = 0;i<list.size();i++){//第一次遍历组内节点，获取主节点lsn
                        BSONObject priNode = (BSONObject) list.get(i);
                        BSONObject obj = getNodeSnap(priNode,Sequoiadb.SDB_SNAP_DATABASE);
                        if ((boolean)obj.get("IsPrimary")){
                            long priLSN = (long)obj.get("CompleteLSN");
                            for (int j = 0;j<list.size();j++){//第二次遍历组内节点，获取备节点lsn
                                BSONObject salveNode = (BSONObject) list.get(j);
                                BSONObject salveObj = getNodeSnap(salveNode,Sequoiadb.SDB_SNAP_DATABASE);
                                if (!((boolean)salveObj.get("IsPrimary"))){//获取备节点lsn，获取主备节点lsn差异
                                    long salLSN = (long) salveObj.get("CompleteLSN");
                                    long differ = Math.abs(priLSN-salLSN);
                                    if (differ>=MAX_LSN_DIFFER){//比较主备节点lsn差异，如果大于1MB，告警
                                        //lsn差异大于1MB，告警
                                        /**
                                         * groupName:name
                                         * primaryNode:(String)obj.get("NodeName")
                                         * salveNode:(String)salveObj.get("NodeName")
                                         * lsn differ:differ
                                         */
                                        System.out.println("数据组名称："+name+",主节点："+(String)obj.get("NodeName")+
                                                " 备节点："+(String)salveObj.get("NodeName")+" LSN差异为："+differ);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 监控协调节点对外连接数量
     * @param db
     */
    public void checkCoordConn(Sequoiadb db){
        BSONObject object;
        try{
            object = db.getSnapshot(Sequoiadb.SDB_SNAP_DATABASE, "", "", "").getCurrent();
            double totalConn = Double.valueOf(object.get("TotalNumConnects").toString());
            if (totalConn > MAX_COORD_CONN){
                //协调节点连接数量大于200，告警，输出以下内容
                System.out.println("协调节点当前对外连接数量："+totalConn);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 监控事务LSN，差异大于100MB告警。
     * @see com.sequoiadb.hfbk.NodeMonitor#TRANS_Differ
     * @param db
     */
    public void checkTrans(Sequoiadb db){
        DBCursor cursor;
        try {
            cursor = db.getSnapshot(Sequoiadb.SDB_SNAP_TRANSACTIONS, "", "", "");

            while (cursor.hasNext()){
                BSONObject trans = cursor.getNext();
                String nodeAddr = (String) trans.get("NodeName");
                long beginLSN = (long) trans.get("BeginTransLSN");
                String host = nodeAddr.split(":")[0];
                int port = Integer.valueOf(nodeAddr.split(":")[1]);
                Sequoiadb db2 = new Sequoiadb(SDBHOST.map.get(host), port, sdbUser, sdbPwd);
                DBCursor cursor1 = db2.getSnapshot(Sequoiadb.SDB_SNAP_DATABASE, "", "", "");
                BSONObject object = (BSONObject) cursor1.getCurrent();
                BSONObject currentLSN = (BSONObject) object.get("CurrentLSN");
                long nodeLSN = (long)currentLSN.get("Offset");
                long lsnDiffer = Math.abs(nodeLSN - beginLSN);
                if (lsnDiffer>=MAX_TRANS_DIFFER){
                    //事务lsn差异大于100MB，告警，输出以下内容
                    String groupName = (String)  object.get("GroupName");
                    System.out.println("当前节点："+nodeAddr+",数据组："+ groupName+" 事务ID:"+
                            trans.get("TransactionID").toString()+"，LSN差异为："+lsnDiffer);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 检查每个节点的会话数
     * @param db
     */
    public void checkNodeSession(Sequoiadb db){
        flag = true;
        try{
            List<BSONObject> list = getGroup(db);
            for (BSONObject gr:list){
                String gn = (String) gr.get("GroupName");
                if (gn.equals("SYSCoord")||gn.equals("SYSCatalogGroup"))
                    continue;
                BasicBSONList nl = (BasicBSONList) gr.get("Group");
                for (int i = 0;i<nl.size();i++){
                    BSONObject obj = (BSONObject) nl.get(i);
                    String host = (String) obj.get("HostName");
                    BSONObject node = (BSONObject) ((BasicBSONList)obj.get("Service")).get(0);
                    int port = Integer.valueOf(node.get("Name").toString());
                    Sequoiadb db2 = new Sequoiadb(SDBHOST.map.get(host), port, sdbUser, sdbPwd);
                    checkLongSession(db2);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            flag = false;
        }
    }
    /**
     *
     * @param db
     */
    public void checkRead(Sequoiadb db){
        BSONObject curr;
        long sleepTime = 10*1000;
        try{
            curr = db.getSnapshot(Sequoiadb.SDB_SNAP_DATABASE, "", "", "").getCurrent();
            double dataRead = (double)curr.get("TotalDataRead");
            double indexRead = (double)curr.get("TotalIndexRead");
            System.out.println("sleep");
            Thread.sleep(sleepTime);
            System.out.println("wake up");
            BSONObject curr1 = db.getSnapshot(Sequoiadb.SDB_SNAP_DATABASE, "", "", "").getCurrent();
            double dataRead2 = (double)curr1.get("TotalDataRead");
            double indexRead2 = (double)curr1.get("TotalIndexRead");

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

    /**
     * 监控数据组主节点是否发生切换
     * @param db
     */
    public static void checkPrimary(Sequoiadb db){
        try {
            List<BSONObject> list = getGroup(db);
            for (BSONObject gr:list){
                String name = (String) gr.get("GroupName");
                if (!(name.equals("SYSCoord")||name.equals("SYSCatalogGroup"))){
                    Node node = db.getReplicaGroup(name).getMaster();
                    String priNodeName = node.getNodeName();
                    if (!primaryNodes.containsKey(name)){
                        primaryNodes.put(name, priNodeName);
                    }else {
                        if (!primaryNodes.get(name).equals(priNodeName)){
                            //主节点与上次检查不一致，发生切换，告警
                            System.out.println("数据组："+name+"，主节点发生切换，当前主节点："+priNodeName+"，切换前主节点"+
                                    primaryNodes.get(name));
                            //更改主节点
                            primaryNodes.put(name, priNodeName);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void checkLogSpace(Sequoiadb db){
        try{
            List<BSONObject> list = getGroup(db);
            for (BSONObject gr:list){
                String gn = (String) gr.get("GroupName");
                if (gn.equals("SYSCoord")||gn.equals("SYSCatalogGroup"))
                    continue;
                BasicBSONList nl = (BasicBSONList) gr.get("Group");
                for (int i = 0;i<nl.size();i++){
                    BSONObject obj = (BSONObject) nl.get(i);
                    BSONObject database = getNodeSnap(obj, Sequoiadb.SDB_SNAP_DATABASE);
                    BSONObject config = getNodeSnap(obj, Sequoiadb.SDB_SNAP_CONFIGS);

                    long begin =(long) ((BSONObject)database.get("BeginLSN")).get("Offset");
                    long curr =(long) ((BSONObject)database.get("CurrentLSN")).get("Offset");

                    int size = (int) config.get("logfilesz");
                    int num = (int) config.get("logfilenum");

                    double logSpace = (double) (curr-begin)/(size*num*1024*1024);
                    if (logSpace>=MAX_SPACE_USE){
                        System.out.println("当前节点："+config.get("NodeName").toString()+"，数据组："+database.get("GroupName").toString()
                                +"，日志空间使用量："+(curr-begin)+
                                " 大于"+MAX_SPACE_USE*100+"%,logfilenum:"+num+",logfilesz:"+size);
                    }


                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /**
     * 监控超过指定时长的会话数量
     * @see com.sequoiadb.hfbk.NodeMonitor#LONG_SESSION
     * @see com.sequoiadb.hfbk.NodeMonitor#LONG_SESSION_NUM
     * @param db
     */
    public static void checkLongSession(Sequoiadb db){
        DBCursor cursor;
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
        int maxSessionNum = MAX_SESSION_NUM;
        int maxRunningSession = MAX_RUNNING_SESSION_NUM;
        if (flag){
            maxRunningSession = MAX_NODE_RUNNING_SESSION_NUM;
            maxSessionNum = MAX_NODE_SESSION_NUM;
        }
        try{
            cursor = db.getSnapshot(Sequoiadb.SDB_SNAP_SESSIONS, "", "","" );
            int longSessionCount = 0;
            int userRunningSession = 0;
            int userSession = 0;

            while (cursor.hasNext()){
                BSONObject session = cursor.getNext();
                String begin = (String) session.get("LastOpBegin");
                String type = (String) session.get("Type");
                String status = (String) session.get("Status");
                String now = df.format(new Date());
                double sessionTime = calculatetimeGapSecond(begin, now,df);
                if (sessionTime >= LONG_SESSION && status.equals("Running")&&!flag){
                    //会话时长超过10s，会话数＋1
                    //System.out.println("当前会话ID："+session.get("SessionID").toString()+"，数据节点："+session.get("NodeName").toString()+
                           // "，会话时长："+sessionTime+"s");
                    longSessionCount ++;
                }
                if (type.equals("ShardAgent") && status.equals("Running")){
                    userRunningSession++;
                }
                if (type.equals("ShardAgent")){
                    userSession++;
                }
            }
            if (flag){

            }
            if (longSessionCount >=LONG_SESSION_NUM&&!flag ){
                //当前超过10s的会话数超过指定量，告警，输出以下信息。
                System.out.println("当前节点："+db.getHost()+":"+db.getPort()+"，当前超过10s的会话数量："+longSessionCount);
            }
            if (userRunningSession>maxRunningSession){
                //活动会话数超过指定，告警
                System.out.println("当前节点："+db.getHost()+":"+db.getPort()+"，是否为数据节点："+flag+"，当前用户活动会话数量："+userRunningSession);
            }
            if (userSession>maxSessionNum){
                //所有会话数超过指定量，告警
                System.out.println("当前节点："+db.getHost()+":"+db.getPort()+"，是否为数据节点："+flag+"，当前用户总会话数量："+userSession);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /**
     * 获取复制组
     * @param db
     * @return
     */
    public static List<BSONObject> getGroup(Sequoiadb db){
        List<BSONObject> groupNames = new ArrayList<>();
        try {
            DBCursor cursor = db.listReplicaGroups();
            while (cursor.hasNext()){
                BSONObject curr = cursor.getNext();
                groupNames.add(curr);

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //if (db != null)
                //db.close();
        }
        return groupNames;
    }

    /**
     * 获取单个节点的指定类型的快照
     * @param obj
     * @param snapType
     * @return
     */
    public static BSONObject getNodeSnap(BSONObject obj,int snapType){
        BSONObject result = null;
        Sequoiadb db = null;
        try {
            String host = (String) obj.get("HostName");
            BasicBSONList services = (BasicBSONList) obj.get("Service");
            BSONObject service = (BSONObject) services.get(0);

            int port = Integer.valueOf(service.get("Name").toString());
            db = new Sequoiadb(SDBHOST.map.get(host), port, sdbUser, sdbPwd);
            DBCursor cursor = db.getSnapshot(snapType, "", "", "");
            result = cursor.getCurrent();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (db != null)
                db.close();
        }
        return result;
    }
    public static   double calculatetimeGapSecond(String time1, String time2,SimpleDateFormat df) {

        if (time1.equals("--")||time2.equals("--"))
            return 0;

        double second = 0;
        try {
            Date date1, date2;
            date1 = df.parse(time1);
            date2 = df.parse(time2);
            double millisecond = date2.getTime() - date1.getTime();
            second = millisecond / (1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return second;
    }

}
