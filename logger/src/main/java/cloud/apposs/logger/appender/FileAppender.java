package cloud.apposs.logger.appender;

import cloud.apposs.logger.Appender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 文件输出
 */
public class FileAppender extends Appender {
    private String filename;

    private String datepattern;

    private String filesurfix;

    private String lastFile;

    private OutputStreamWriter fw;

    public FileAppender(String file) {
        lastFile = parseFile(file);
        doCloseFw();
        fw = doInitFileWriter();
    }

    private OutputStreamWriter doInitFileWriter() {
        File file = new File(lastFile);
        if (file.isDirectory()) {
            System.err.println("File [" + lastFile + "] is a directory. Using System.out instead");
            return new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
        }
        // 目录不存在，尝试创建目录
        String parentName = file.getParent();
        File parentDir = new File(parentName);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        try {
            FileOutputStream out = new FileOutputStream(file, true);
            return new OutputStreamWriter(out, Charset.forName("UTF-8"));
        } catch (Exception e) {
            System.err.println("File [" + lastFile + "] init error cause by '" + e.getMessage() + "'. Using System.out instead");
            return new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
        }
    }

    @Override
    public void append(List<String> msgList) {
        try {
            String file = getFile();
            if (!file.equals(lastFile)) {
                doCloseFw();
                lastFile = file;
                fw = doInitFileWriter();
            }
            if (fw == null) {
                return;
            }
            for (String msg : msgList) {
                fw.write(msg);
            }
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parseFile(String file) {
        if (file == null) {
            throw new IllegalArgumentException("file");
        }

        int i = 0;
        i = file.lastIndexOf(".");
        if (i != -1) {
            filename = file.substring(0, i);
            filesurfix = file.substring(i + 1, file.length());
        } else {
            filename = file;
        }

        i = filename.indexOf("{");
        if (i != -1) {
            int end = filename.indexOf('}', i);
            if (end > i) {
                filename = filename.substring(0, i);
                datepattern = file.substring(i + 1, end);
            }
        }

        return getFile();
    }

    public String getFile() {
        String file = filename;
        if (datepattern != null) {
            SimpleDateFormat df = new SimpleDateFormat(datepattern);
            String date = df.format(new Date());
            file += date;
        }
        if (filesurfix != null) {
            file += "." + filesurfix;
        }
        return file;
    }

    @Override
    public void close() {
        doCloseFw();
    }

    private void doCloseFw() {
        try {
            if (fw != null) {
                fw.close();
                fw = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
