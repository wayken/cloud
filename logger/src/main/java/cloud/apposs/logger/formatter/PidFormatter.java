package cloud.apposs.logger.formatter;

import cloud.apposs.logger.FormatInfo;
import cloud.apposs.logger.Formatter;
import cloud.apposs.logger.LogInfo;

import java.lang.management.ManagementFactory;

/**
 * 输出产生该日志事件的进程ID，对应关键字[%I]
 */
public class PidFormatter extends Formatter {
	public String pid;

	public PidFormatter(FormatInfo formatInfo) {
		super(formatInfo);
		try {
			String jvmName = ManagementFactory.getRuntimeMXBean().getName();
			pid = jvmName.split("@")[0];
		} catch (Throwable ex) {
		}
	}
	
	@Override
	public String convert(LogInfo info) {
		return pid;
	}
}
