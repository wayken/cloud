package cloud.apposs.logger;

import cloud.apposs.logger.Configuration.Prefix;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class TestLogger {
	private static final String LOG_CONFIG = "log.properties";
	private static final String LOG_FILE_CONFIG = "log_file.properties";
	private static final String LOG_DAILY_FILE_CONFIG = "log_daily_file.properties";
	private static final String LOG_SECONDLY_FILE_CONFIG = "log_secondly_file.properties";
	private static final String LOG_DB_CONFIG = "log_db.properties";
    private static final String LOG_FORMAT_CONFIG = "log_format.properties";

	static class T extends Thread {
		public T(String name) {
			super(name);
		}

		@Override
		public void run() {
			Logger.config(LOG_CONFIG);
			Logger.info("a log thread");
		}
	}

	public static void main(String[] args) throws Exception {
        testLogProperties();
	}

	public static void testLog() {
		Log log = new Log();
		log.info("this is a log instance");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
	}

	public static void testLogMessage() {
		Logger.error("this is a log");
	}

	public static void testLogThrowable() {
		try {
			int i = 1 / 0;
			System.out.println(i);
		} catch (Exception e) {
			Logger.info(e, "exception caught");
		}
	}

	@Test
    public void testLogThrowable2() throws Exception {
        Properties prop = new Properties();
        prop.setProperty(Prefix.FORMAT, "[LogProperties] %d{yyyy-MM-dd HH:mm:ss} %P: %m%n%E");
        Logger.config(prop);
        try {
            int i = 1 / 0;
            System.out.println(i);
        } catch (Exception e) {
            Logger.info(new IOException("i am error", e), "exception caught");
        }
        Thread.sleep(2000000);
    }

	public static void testLogProperties() {
		Properties prop = new Properties();
		prop.setProperty(Prefix.LEVEL, "info");
		prop.setProperty(Prefix.APPENDER, "console");
		prop.setProperty(Prefix.FORMAT, "[LogProperties] %d{yyyy-MM-dd HH:mm:ss} %P: %c %m%n%e");
		Logger.config(prop);
		Logger.info("this is a log properties");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
	}

	public static void testLogConfigFile() {
		Logger.config(LOG_CONFIG);
		Logger.info("this is a log");
		Logger.info("this is a log2");
    }

	public static void testLogThread() {
		T t = new T("MyThread1");
		t.start();
		T t2 = new T("MyThread2");
		t2.start();
	}

    public static void testLogFormat() {
        Logger.config(LOG_FORMAT_CONFIG);
        Logger.info("a fomat log");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        Logger.info("a fomat log2");
    }

	public static void testLogFile() {
		Logger.config(LOG_FILE_CONFIG);
		Logger.info("this is a log");
		try {
			int i = 1 / 0;
			System.out.println(i);
		} catch (Exception e) {
			Logger.info(e, "error calculate");
		}
	}

	public static void testLogDailyFile() {
		Logger.config(LOG_DAILY_FILE_CONFIG);
		Logger.info("this is a log");
		try {
			int i = 1 / 0;
			System.out.println(i);
		} catch (Exception e) {
			Logger.info(e, "error calculate");
		}
	}

	public static void testLogSecondlyFile() {
		Logger.config(LOG_SECONDLY_FILE_CONFIG);
		Logger.info("this is a log");
		try {
			int i = 1 / 0;
			System.out.println(i);
		} catch (Exception e) {
			Logger.info(e, "error calculate");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Logger.info("a mul log");
	}

	public static void testLogDb() {
		Logger.config(LOG_DB_CONFIG);
		Logger.info("a dataabase log");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Logger.info("a dataabase log2");
	}
}
