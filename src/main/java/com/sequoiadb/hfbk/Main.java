package com.sequoiadb.hfbk;

import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Main {
    public static void main(String[] args)  {
        NodeMonitor nodeMonitor = new NodeMonitor();

        Sequoiadb db = null;
        try {
            db = new Sequoiadb("192.168.232.136", 11810, "", "");
        } catch (BaseException e) {
            //e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println();
        }
        //nodeMonitor.checkLSN(db);
        //nodeMonitor.checkSlowQuery(db);
        //nodeMonitor.checkTrans(db);
        //nodeMonitor.checkLongSession(db);
        //nodeMonitor.checkRead(db);
        nodeMonitor.checkCoordConn(db);
        nodeMonitor.checkNodeSession(db);
        nodeMonitor.checkLogSpace(db);

        /*for (int i = 0;i<10;i++){
            NodeMonitor.checkPrimary(db);
            System.out.println("监控中，主节点是否切换。。。");
            Thread.sleep(10*1000);
        }*/

        /*//代码实现：
        //1. 导入驱动jar包
        //2.注册驱动
        Class.forName("com.mysql.jdbc.Driver");
        //3.获取数据库连接对象
        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.232.137:3306/", "sdbadmin", "sdbadmin");
        //4.定义sql语句
        String sql = "update test.test set id = 100 where name = 'tom_2'";
        //5.获取执行sql的对象 Statement
        Statement stmt = conn.createStatement();

        MySQLMonitor.checkEngine(stmt);
        //6.执行sql
        int count = stmt.executeUpdate(sql);
        //7.处理结果
        System.out.println(count);
        //8.释放资源
        stmt.close();
        conn.close();*/

        //Sequoiadb db = new Sequoiadb("192.168.232.136", 11900, , )
    }
}
