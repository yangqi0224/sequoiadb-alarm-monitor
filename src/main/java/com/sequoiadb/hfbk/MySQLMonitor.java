package com.sequoiadb.hfbk;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author yangqi
 * @version 1.0
 * @class com.sequoiadb.hfbk.MySQLMonitor
 * @date 2020-04-14 16:33
 */
public class MySQLMonitor {

    private static String user = "sdbadmin";
    private static String pwd = "sdbadmin";

    //告警阈值
    private static int MAX_SLOW_QUERY_NUM = 0;
    private static int MAX_ENGINE_NOT_SEQUOIADB_NUM = 0;
    private static int MAX_CONNECT_NUM = 0;

    //sql监控语句
    private static String ENGINE = "select count(*) as num " +
            "from  information_schema.TABLES " +
            "where TABLE_SCHEMA not in " +
            "('mysql','sys','performance_schema','information_schema') and ENGINE not in ('SequoiaDB');";
    private static String SLOW_QUERY = "select count(1) cnt " +
            "from information_schema.processlist " +
            "where db is not null and command !='Sleep' and time > 120 having count(1) >5;";
    private static String CONNECT_NUM = "select round(100*threads.value/max_con.value) result from \n" +
            "(select variable_value value from performance_schema.global_status where variable_name='THREADS_CONNECTED') threads,\n" +
            "(select variable_value value from performance_schema.global_variables where variable_name='max_connections') max_con \n" +
            "where threads.value/max_con.value > 0.9;";

    public static void checkEngine(Statement statement){
        try {
            ResultSet engineQuery = statement.executeQuery(ENGINE);
            int noSdb = 0;
            int slowNum = 0;
            int connNum = 0;
            if (engineQuery.next())
                noSdb = engineQuery.getInt("num");

            if (noSdb>MAX_ENGINE_NOT_SEQUOIADB_NUM){
                //发现存储引擎不是SequoiaDB的表，告警
                System.out.println("结构化集群MySQL实例（此处填充MySQL的ip:port）出现非SequoiaDB的表，数量："+noSdb);
            }
            ResultSet slowQuery = statement.executeQuery(SLOW_QUERY);

            if (slowQuery.next())
                slowNum = slowQuery.getInt("cnt");

            if (slowNum>MAX_SLOW_QUERY_NUM){
                //慢查询超过指定数量，告警
                System.out.println("结构化集群MySQL实例（此处填充MySQL的ip:port）慢查询sql条数为："+slowNum);
            }

            ResultSet connNumQuery = statement.executeQuery(CONNECT_NUM);
            if (connNumQuery.next())
                connNum = connNumQuery.getInt("result");

            if (connNum>MAX_CONNECT_NUM){
                //连接数超过阈值，告警
                System.out.println("结构化集群MySQL实例（此处填充MySQL的ip:port）连接数为："+connNum);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
