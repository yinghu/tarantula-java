package com.icodesoftware.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class TarantulaFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        //use string builder to format record data
        StringBuilder builder = new StringBuilder(record.getLevel().getName());
        builder.append(" [").append(record.getLoggerName()).append("/").append(Thread.currentThread().getName()).append("]");
        builder.append(" [").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("]");
        if(record.getThrown()==null){
            builder.append(" ").append(record.getMessage()).append(System.getProperty("line.separator"));
        }else{
            builder.append(" ").append(record.getMessage()).append(" ");
            StringWriter out = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(out));
            builder.append(out).append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }
}
