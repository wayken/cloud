package cloud.apposs.logger.formatter;

import cloud.apposs.logger.FormatInfo;
import cloud.apposs.logger.Formatter;
import cloud.apposs.logger.LocationInfo;
import cloud.apposs.logger.LogInfo;

/**
 * 输输出所属的类目，通常就是所在类的路径简写+类名全写，示例：c.a.l.LogHandler，对应关键字[%c]
 */
public class AbbreviateClassNameFormatter extends Formatter {
	public AbbreviateClassNameFormatter(FormatInfo formatInfo) {
		super(formatInfo);
	}

	@Override
	public String convert(LogInfo info) {
		LocationInfo locationInfo = info.getLocationInfo();
		String fullClassName = locationInfo.getClassName();
		StringBuilder buffer = new StringBuilder();
		abbreviate(fullClassName, buffer);
		return buffer.toString();
	}

	private static void abbreviate(final String original, final StringBuilder destination) {
		int originalIndex = 0;
		int originalLength = original.length();
		while (originalIndex >= 0 && originalIndex < originalLength) {
			originalIndex = fragment(original, originalIndex, destination);
		}
	}

	private static int fragment(final String input, final int inputIndex, final StringBuilder buf) {
		int nextDot = input.indexOf('.', inputIndex);
		if (nextDot < 0) {
			buf.append(input, inputIndex, input.length());
			return nextDot;
		}
		if (nextDot - inputIndex > 1) {
			buf.append(input, inputIndex, inputIndex + 1);
			buf.append('.');
		} else {
			buf.append(input, inputIndex, nextDot + 1);
		}
		return nextDot + 1;
	}
}
