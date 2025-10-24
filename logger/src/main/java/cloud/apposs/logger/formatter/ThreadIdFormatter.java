package cloud.apposs.logger.formatter;

import cloud.apposs.logger.FormatInfo;
import cloud.apposs.logger.Formatter;
import cloud.apposs.logger.LogInfo;

/**
 * 输出产生该日志事件的线程ID，对应关键字[%i]
 */
public class ThreadIdFormatter extends Formatter {
	public ThreadIdFormatter(FormatInfo formatInfo) {
		super(formatInfo);
	}

	@Override
	public String convert(LogInfo info) {
		return String.valueOf(info.getThreadId());
	}
}
