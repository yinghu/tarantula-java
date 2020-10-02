package com.icodesoftware.logging;

import com.icodesoftware.TarantulaLogger;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JDKLogger implements TarantulaLogger {
		
		private static final boolean __DEV__ = true;
		
		private Logger _log;

		static {
			Properties _props = new Properties();
			try {
				_props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
				String[] fid = _props.getProperty("java.util.logging.FileHandler.pattern").split("/");
				StringBuilder fs = new StringBuilder();
				fs.append(System.getProperty("user.home")).append(FileSystems.getDefault().getSeparator());
				for(int i=1;i<fid.length-1;i++){
					fs.append(fid[i]).append(FileSystems.getDefault().getSeparator());
				}
				Path _path = Paths.get(fs.substring(0,fs.length()-1));
				if(!Files.exists(_path)){
					Files.createDirectories(_path);
				}
				LogManager.getLogManager().readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
			}catch (Exception ex){
				throw new RuntimeException(ex);
			}

		}
		private JDKLogger(Class<?> t){
			_log = Logger.getLogger(t.getName());
		}
		public static JDKLogger getLogger(Class<?> t){
			return new JDKLogger(t);
		}
		public  void debug(String message){
			if(__DEV__){
				_log.log(Level.FINE,message);
			}
		}
		public  void info(String message){
			if(__DEV__){
				_log.log(Level.INFO,message);
			}
		}
		public  void warn(String message){
			_log.log(Level.WARNING,message);
		}
		public  void warn(String message,Throwable t){
			_log.log(Level.WARNING,message,t);
		}
		public  void error(String message,Throwable t){
			_log.log(Level.SEVERE,message,t);
		}

}
