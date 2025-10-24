package cloud.apposs.logger.formatter;

import cloud.apposs.logger.FormatInfo;
import cloud.apposs.logger.Formatter;
import cloud.apposs.logger.LogInfo;

/**
 * 输出优先级，即DEBUG，INFO，WARN，ERROR，FATAL，对应关键字[%p]
 */
public class LevelFormatter extends Formatter {
	public LevelFormatter(FormatInfo formatInfo) {
		super(formatInfo);
	}

	@Override
	public String convert(LogInfo info) {
		return info.getLevel().getString();
	}
}
