/**
 * 
 */
package com.matrixcos.intake.ecomm.mergepacket.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.matrix.intake.ecomm.packet.objects.EcommVO;

/**
 * @author kbobba
 * 
 */
public class MergePacketDao {

    private static final Logger log = Logger.getLogger(MergePacketDao.class);

    private static Properties appProps;

    private BoneCP connectionPool = null;

    public MergePacketDao(Properties appProps) {
        MergePacketDao.appProps = appProps;
        this.initConnectionPool();
    }

    /**
     * @throws SQLException
     * @throws InterruptedException
     * 
     */
    private void initConnectionPool() {
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (SQLException e) {
            log.error("Error Registering Driver", e);
        }
        String dbUrl = appProps.getProperty("database.url");
        String userName = appProps.getProperty("workdesk.user");
        String password = appProps.getProperty("workdesk.pass");

        // setup the connection pool
        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(userName);
        config.setPassword(password);

        config.setMinConnectionsPerPartition(2);
        config.setMaxConnectionsPerPartition(2);
        config.setPartitionCount(1);

        try {
            connectionPool = new BoneCP(config);
        } catch (SQLException e) {
            log.error("Cannot Create ConnectionPool", e);
        }
    }// initConnectionPool()

    public List<String> getPacketEcommDocs(long ecomm) {
        List<String> packetEcommDocs = new ArrayList<String>();
        String sql = " SELECT   ED.DOCUMENT_OUTPUT_LOCATION"
                + "   FROM   WORKDESK.WD_ECOMM_DOCUMENTS ED " + "   WHERE ED.ECOMM_NUMBER = "
                + ecomm + " AND ED.DOCUMENT_OUTPUT_LOCATION IS NOT NULL"
                + " ORDER BY ED.DOCUMENT_DISPLAY_SEQUENCE";
        Connection connection = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            connection = connectionPool.getConnection();
            rs = null;
            stmt = null;
            if (connection != null) {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
                if (log.isDebugEnabled()) {
                    log.debug("get Packet Docs. ecomm = " + ecomm);
                }
                while (rs.next()) {
                    packetEcommDocs.add(rs.getString(1));
                }
            }
            DbUtils.closeQuietly(connection, stmt, rs);
        } catch (SQLException e) {
            log.error("getPacketEcommDocs", e);
        }

        return packetEcommDocs;
    }// getPacketEcommDocs

    /**
     * @param string
     * @param ecomm
     */
    public boolean updateMergePacketEcomm(String status, Long ecomm) {
        String sql = "UPDATE WORKDESK.WD_MERGE_PACKET_ECOMM SET STATUS = '" + status
                + "',updated_date=sysdate WHERE ECOMM_NUMBER = " + ecomm;
        Connection connection = null;
        int result = -100;
        Statement stmt = null;
        try {
            connection = connectionPool.getConnection();
            stmt = null;
            if (connection != null) {
                stmt = connection.createStatement();
                result = stmt.executeUpdate(sql);
                if (log.isDebugEnabled()) {
                    log.debug("MERGE_PACKET_ECOMM Updated = " + result + " ecomm=" + ecomm
                            + " status=" + status);
                }
            }
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
            if (result != 1) {
                log.error("Did not update WD_MERGE_PACKET_ECOMM ecomm=" + ecomm);
            }
        } catch (SQLException e) {
            log.error("updateMergePacketEcomm ecomm=" + ecomm, e);
            return false;
        }
        return true;
    }// updateMergePacketEcomm

    /**
     * 
     */
    private void setupPacketEcommsCreatedTodayForMerge() {
        String sql = "INSERT INTO WORKDESK.WD_MERGE_PACKET_ECOMM "
                + " (ECOMM_NUMBER,STATUS,ECOMM_CREATED_DATE,CREATED_BY) "
                + " SELECT EH.ECOMM_NUMBER,'N',EH.CREATED_DATETIME,'MERGE_PACKET' "
                + " FROM   WORKDESK.WD_ECOMM_HEADER EH   WHERE   EH.EVENT_ID = 46 "
                + " AND TRUNC (EH.CREATED_DATETIME) = TRUNC (SYSDATE) "
                + " AND EH.ECOMM_STATUS = 'COMPLETE' AND EH.ECOMM_NUMBER NOT IN "
                + " (SELECT ECOMM_NUMBER FROM WORKDESK.WD_MERGE_PACKET_ECOMM)";

        Connection connection = null;
        int result = -100;
        Statement stmt = null;
        try {
            connection = connectionPool.getConnection();
            stmt = null;
            if (connection != null) {
                stmt = connection.createStatement();
                result = stmt.executeUpdate(sql);
                log.info("PacketEcomms Setup to Merge = " + result);
            }
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        } catch (SQLException e) {
            log.error("bringPacketEcommsCreatedTodayForMerge", e);
        }
    }// bringPacketEcommsCreatedTodayForMerge

    /**
     * 
     */
    public List<EcommVO> getPacketEcommsCreatedTodayForMerge() {
        this.setupPacketEcommsCreatedTodayForMerge();
        List<EcommVO> ecommList = new ArrayList<EcommVO>();
        String sql = "SELECT  MP.ECOMM_NUMBER, ECOMM_CREATED_DATE "
                + " FROM  WORKDESK.WD_MERGE_PACKET_ECOMM MP WHERE  MP.STATUS = 'N' "
                + " AND TRUNC (MP.ECOMM_CREATED_DATE) = TRUNC (SYSDATE)";

        Connection connection = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            connection = connectionPool.getConnection();
            if (connection != null) {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
                // if (log.isDebugEnabled()) {
                // log.debug("get Packet Docs. ecomm = " + ecomm);
                // }
                while (rs.next()) {
                    ecommList.add(new EcommVO(rs.getLong(1), rs.getString(2)));
                }
            }
            DbUtils.closeQuietly(connection, stmt, rs);
        } catch (SQLException e) {
            log.error("getPacketEcommsCreatedTodayForMerge", e);
        }

        log.info("PacketEcomms to Merge = " + ecommList.size());
        return ecommList;
    }// getPacketEcommsCreatedTodayForMerge

    /**
     * 
     */
    private void setupPacketEcommsCreatedPastForMerge() {
        String sql = "INSERT INTO WORKDESK.WD_MERGE_PACKET_ECOMM "
                + " (ECOMM_NUMBER,STATUS,ECOMM_CREATED_DATE,CREATED_BY) "
                + " SELECT EH.ECOMM_NUMBER,'N',EH.CREATED_DATETIME,'MERGE_PACKET' "
                + " FROM   WORKDESK.WD_ECOMM_HEADER EH "
                + " WHERE EH.EVENT_ID = 46 "
                + " AND EH.ECOMM_NUMBER > 2000000 "
                + " AND TRUNC (EH.CREATED_DATETIME) < TRUNC (SYSDATE) "
                // + " AND eh.ecomm_number in (5012935,5021468,5023150) "
                // + " AND eh.ecomm_number in
                // (5022668,5022671,5022629,5022648,5022732,5022735, "
                // + " 5022738,5023039,5023048,5023051,5023054,5023057,"
                // + " 5023111,5023114,5023150,5023368) "
                // = 5022036"// TODO remove line
                // + " AND eh.ecomm_number in (5023400) "
                + " AND EH.ECOMM_STATUS = 'COMPLETE' AND EH.ECOMM_NUMBER NOT IN "
                + " (SELECT ECOMM_NUMBER FROM WORKDESK.WD_MERGE_PACKET_ECOMM)";

        Connection connection = null;
        int result = -100;
        Statement stmt = null;
        try {
            connection = connectionPool.getConnection();
            stmt = null;
            if (connection != null) {
                stmt = connection.createStatement();
                result = stmt.executeUpdate(sql);
                log.info("SETUP Past PacketEcomms to Merge = " + result);
            }
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        } catch (SQLException e) {
            log.error("setupPacketEcommsCreatedPastForMerge", e);
        }
    }// setupPacketEcommsCreatedPastForMerge

    /**
     * @return
     */
    public List<EcommVO> getPacketEcommsCreatedPastForMerge() {
        this.setupPacketEcommsCreatedPastForMerge();
        int pastLimit = Integer.valueOf(appProps.getProperty("mergepacket.pastlimit")) + 1;
        List<EcommVO> ecommList = new ArrayList<EcommVO>();

        String sql = "SELECT   ECOMM_NUMBER, ECOMM_CREATED_DATE FROM"
                + " (SELECT   MP.ECOMM_NUMBER, ECOMM_CREATED_DATE"
                + "  FROM   WORKDESK.WD_MERGE_PACKET_ECOMM MP  WHERE   MP.STATUS = 'N'"
                + "  AND TRUNC (MP.ECOMM_CREATED_DATE) < TRUNC (SYSDATE)"
                + "  ORDER BY   MP.ECOMM_NUMBER DESC) WHERE   ROWNUM < " + pastLimit;

        Connection connection = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            connection = connectionPool.getConnection();
            if (connection != null) {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    ecommList.add(new EcommVO(rs.getLong(1), rs.getString(2)));
                }
            }
            DbUtils.closeQuietly(connection, stmt, rs);
        } catch (SQLException e) {
            log.error("getPacketEcommsCreatedPastForMerge", e);
        }
        return ecommList;
    }// getPacketEcommsCreatedPastForMerge

    /**
     * @return
     */
    public List<EcommVO> getPacketEcommsForMerge() {
        this.setupPacketEcommsForMerge();
        List<EcommVO> ecommList = new ArrayList<EcommVO>();
        int limit = Integer.valueOf(appProps.getProperty("mergepacket.limit")) + 1;
        String sql = "SELECT  MP.ECOMM_NUMBER, ECOMM_CREATED_DATE "
                + " FROM  WORKDESK.WD_MERGE_PACKET_ECOMM MP WHERE  MP.STATUS = 'N' "
                + " AND ROWNUM < " + limit;

        Connection connection = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            connection = connectionPool.getConnection();
            if (connection != null) {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
                // if (log.isDebugEnabled()) {
                // log.debug("get Packet Docs. ecomm = " + ecomm);
                // }
                while (rs.next()) {
                    ecommList.add(new EcommVO(rs.getLong(1), rs.getString(2)));
                }
            }
            DbUtils.closeQuietly(connection, stmt, rs);
        } catch (SQLException e) {
            log.error("getPacketEcommsForMerge", e);
        }

        log.info("PacketEcomms to Merge = " + ecommList.size());
        return ecommList;
    }

    /**
     * 
     */
    private void setupPacketEcommsForMerge() {
        String sql = "INSERT INTO WORKDESK.WD_MERGE_PACKET_ECOMM "
                + " (ECOMM_NUMBER,STATUS,ECOMM_CREATED_DATE,CREATED_BY) "
                + " SELECT EH.ECOMM_NUMBER,'N',EH.CREATED_DATETIME,'MERGE_PACKET' "
                + " FROM   WORKDESK.WD_ECOMM_HEADER EH  WHERE EH.EVENT_ID = 46 "
                + " AND EH.ECOMM_NUMBER > 2000000 "
                + " AND EH.ECOMM_STATUS = 'COMPLETE' AND EH.ECOMM_NUMBER NOT IN "
                + " (SELECT ECOMM_NUMBER FROM WORKDESK.WD_MERGE_PACKET_ECOMM)";

        Connection connection = null;
        int result = -100;
        Statement stmt = null;
        try {
            connection = connectionPool.getConnection();
            stmt = null;
            if (connection != null) {
                stmt = connection.createStatement();
                result = stmt.executeUpdate(sql);
                log.info("SETUP PacketEcomms to Merge = " + result);
            }
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
        } catch (SQLException e) {
            log.error("setupPacketEcommsForMerge", e);
        }
    }// setupPacketEcommsForMerge

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (connectionPool != null) {
            connectionPool.shutdown();
        }
    }

}// main()
