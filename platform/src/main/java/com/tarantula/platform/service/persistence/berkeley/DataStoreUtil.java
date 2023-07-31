package com.tarantula.platform.service.persistence.berkeley;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataStoreUtil {

    public static void createLastDataFile(File f) throws IOException {
        f.createNewFile();
        DataOutputStream fo = new DataOutputStream(new FileOutputStream(f));
        fo.writeLong(0);
        fo.writeLong(0);
        fo.writeUTF(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        fo.close();
    }
}
