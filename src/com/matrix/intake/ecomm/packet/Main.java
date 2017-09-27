package com.matrix.intake.ecomm.packet;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Main {

    public static Logger log = Logger.getLogger(Main.class);

    /**
     * Packet Generation Main.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        initLog("log4j.xml");

        log.info("BEGIN");

        PacketsBatchGeneration processPacketeComms = new PacketsBatchGeneration();
        processPacketeComms.processPacketDetails();

        log.info("END");
        log.info("");
        log.info("");
    }

    public static void initLog(String configFile) {
        DOMConfigurator.configure(configFile);
    }

}
