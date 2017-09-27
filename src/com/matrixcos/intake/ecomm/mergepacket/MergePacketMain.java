package com.matrixcos.intake.ecomm.mergepacket;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class MergePacketMain {

    public static Logger log = Logger.getLogger(MergePacketMain.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        initLog("log4j-MergePacket.xml");

        log.info("BEGIN");
        long t1 = System.currentTimeMillis();

        MergePacket mp = new MergePacket();
        mp.mergePackets();

        log.info("END TimeTaken=" + (System.currentTimeMillis() - t1) / (1000 * 60) + " minutes.");
        log.info("");
        log.info("");
    }

    static void initLog(String configFile) {
        DOMConfigurator.configure(configFile);
    }

}
