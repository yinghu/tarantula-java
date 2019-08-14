package com.tarantula.logging;

import com.tarantula.TarantulaLogger;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JDKLogger implements TarantulaLogger{
		
		private static final boolean __DEV__ = true;
		
		private Logger _log;

		static {
			try {
				LogManager.getLogManager().readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
			}catch (Exception ex){
				ex.printStackTrace();
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
