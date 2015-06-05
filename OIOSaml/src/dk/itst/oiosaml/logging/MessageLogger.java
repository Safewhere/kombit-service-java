package dk.itst.oiosaml.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;

/**
 * This class is to log soap message sent and received to file
 */
public class MessageLogger {
    //Log files is saved on c:\temp folder
    public static void LogToFile(String filePath, String fileName, String msg) throws IOException {
//        Date d = new Date();
//        SimpleDateFormat sdfDate = new SimpleDateFormat("HH_mm_ss.SSS");
//        String strDate = sdfDate.format(d);
        
        String fullFileName = "C:/temp/" + fileName;
        if (!"".equals(filePath)){
            fullFileName = filePath + fileName;
        }
        //File logFile = new File(fullFileName + strDate + ".txt");
        File logFile = new File(fullFileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write(msg);
        }
    }
}
