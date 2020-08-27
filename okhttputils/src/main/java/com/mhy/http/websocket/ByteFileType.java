package com.mhy.http.websocket;

/**
 * Created By Mahongyin
 * Date    2020/8/27 9:50
 */

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ByteFileType {

    public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();
    private ByteFileType() {
    }

    static {
        getAllFileType();  //初始化文件类型信息

    }

    private static void getAllFileType() {
        // images
        FILE_TYPE_MAP.put("jpg", "FFD8FF"); //JPEG (jpg)
        FILE_TYPE_MAP.put("png", "89504E47");  //PNG (png)
        FILE_TYPE_MAP.put("gif", "47494638");  //GIF (gif)
        FILE_TYPE_MAP.put("tif", "49492A00");  //TIFF (tif)
        FILE_TYPE_MAP.put("bmp", "424D"); //Windows Bitmap (bmp)

        FILE_TYPE_MAP.put("dwg", "41433130"); //CAD (dwg)
        FILE_TYPE_MAP.put("html", "68746D6C3E");  //HTML (html)
        FILE_TYPE_MAP.put("rtf", "7B5C727466");  // 日记本Rich Text Format (rtf)
        FILE_TYPE_MAP.put("xml", "3C3F786D6C");
        FILE_TYPE_MAP.put("zip", "504B0304");
        FILE_TYPE_MAP.put("rar", "52617221");
        FILE_TYPE_MAP.put("psd", "38425053");  //Photoshop (psd)
        FILE_TYPE_MAP.put("eml", "44656C69766572792D646174653A");  //邮件Email [thorough only] (eml)
        FILE_TYPE_MAP.put("gz","1F8B08");

        FILE_TYPE_MAP.put("dbx", "CFAD12FEC5FD746F");  //Outlook Express (dbx)
        FILE_TYPE_MAP.put("pst", "2142444E");  //Outlook (pst)
        FILE_TYPE_MAP.put("xls", "D0CF11E0");  //MS Word
        FILE_TYPE_MAP.put("doc", "D0CF11E0");  //MS Excel 注意：word 和 excel的文件头一样
        FILE_TYPE_MAP.put("docx","504B0304");
        FILE_TYPE_MAP.put("xlsx","504B0304");//excel2007以上版本文件
        FILE_TYPE_MAP.put("pptx","5265644c");//ppt、pptx
        FILE_TYPE_MAP.put("mdb", "5374616E64617264204A");  //MS Access (mdb)
        FILE_TYPE_MAP.put("wpd", "FF575043"); //WordPerfect (wpd)
        FILE_TYPE_MAP.put("eps", "252150532D41646F6265");
        FILE_TYPE_MAP.put("ps", "252150532D41646F6265");
        FILE_TYPE_MAP.put("pdf", "255044462D312E");  //Adobe Acrobat (pdf)
        FILE_TYPE_MAP.put("qdf", "AC9EBD8F");  //Quicken (qdf)
        FILE_TYPE_MAP.put("pwl", "E3828596");  //Windows Password (pwl)
        FILE_TYPE_MAP.put("wav", "57415645");  //Wave (wav)
        FILE_TYPE_MAP.put("avi", "41564920");
        FILE_TYPE_MAP.put("ram", "2E7261FD");  //Real Audio (ram)
        FILE_TYPE_MAP.put("rm", "2E524D46");  //Real Media (rm)
        FILE_TYPE_MAP.put("mpg", "000001BA");
        FILE_TYPE_MAP.put("mpeg", "000001B3");
        FILE_TYPE_MAP.put("mov", "6D6F6F76");  //Quicktime (mov)
        FILE_TYPE_MAP.put("asf", "3026B2758E66CF11"); //Windows Media (asf)
        FILE_TYPE_MAP.put("mid", "4D546864");  //MIDI (mid)
        FILE_TYPE_MAP.put("wps","d0cf11e0a1b11ae10000");//WPS文字wps、表格et、演示dps都是一样的

    }

    public static String getFileType(File file) {
        String filetype = null;
        byte[] b = new byte[10];
        try {
            InputStream is = new FileInputStream(file);
            is.read(b);
            filetype = getFileTypeByStream(b);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filetype;
    }

    public static String getFileTypeByStream(byte[] b) {
        String filetypeHex = getFileHexString(b);
Log.i("mhyLog",filetypeHex);
        Iterator<Entry<String, String>> entryiterator = FILE_TYPE_MAP.entrySet().iterator();
        while (entryiterator.hasNext()) {
            Entry<String, String> entry = entryiterator.next();
            String fileTypeHexValue = entry.getValue();
            if (null!=filetypeHex&&filetypeHex.toUpperCase().startsWith(fileTypeHexValue.toUpperCase())||fileTypeHexValue.toUpperCase().startsWith(filetypeHex.toUpperCase())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 得到上传文件的文件头
     * @param b
     * @return
     */
    public static String getFileHexString(byte[] b) {
        StringBuilder stringBuilder = new StringBuilder();
        if (b == null || b.length <= 0) {
            return null;
        }
        for (byte value : b) {
            int v = value & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}