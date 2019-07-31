package cf.rafl.applicationserver.util;

import java.util.logging.Logger;

public class Crash
{

    private static Logger logger = Logger.getLogger(Crash.class.getName());

    public static void crash()
    {
//        Thread.getAllStackTraces().get(Thread.currentThread());
        logger.severe("Application has encountered a fatal issue, exiting now...");
        System.exit(-1);
    }

}
