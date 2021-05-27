package com.lh.linking.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


/**
 * 文件操作
 *
 * @author lh
 * @date 2018年3月7日 下午3:44:53
 */
public class FileOperation {
    private static final Logger logger = LoggerFactory.getLogger(FileOperation.class);

    /**
     * 保存文件
     *
     * @return
     * @author lh
     * @date 2018年3月7日 下午3:45:34
     */
    public static boolean save(String descPath, byte[] bytes, String key) {
        return save(new File(descPath), bytes, key);
    }

    /**
     * 保存文件
     *
     * @return
     * @author lh
     * @date 2018年3月7日 下午3:45:34
     */
    public static boolean save(File descFile, byte[] bytes, String key) {
        try {
            checkFile(descFile);
            return byteToOutput(bytes, new FileOutputStream(descFile), key, true);
        } catch (FileNotFoundException e) {
            logger.error("文件保存失败:" + e.getMessage());
            return false;
        }
    }

    /**
     * 保存文件
     *
     * @return
     * @author lh
     * @date 2018年3月7日 下午3:45:34
     */
    public static boolean save(String dir, String fileName, byte[] bytes, String key) {
        return save(new File(dir, fileName), bytes, key);
    }

    /**
     * 保存文件
     *
     * @return
     * @author lh
     * @date 2018年3月7日 下午3:45:34
     */
    public static boolean save(File descFile, InputStream input, String key) {
        checkFile(descFile);
        try {
            return inputToOutput(input, new FileOutputStream(descFile), key, true);
        } catch (Exception e) {
            logger.error("文件保存失败:" + e.getMessage());
            return false;
        }
    }

    /**
     * @do 检查文件
     * @author lh
     * @date 2020/11/2 16:22
     */
    private static File checkFile(File descFile) {
        if (descFile.exists()) {
            descFile.setExecutable(false);
            return descFile;
        }
        if (!descFile.getParentFile().exists()) {
            descFile.getParentFile().mkdirs();
        }
        return descFile;
    }

    public static boolean save(String descPath, InputStream input, String key) {
        try {
            return inputToOutput(input, new FileOutputStream(checkFile(new File(descPath))), key, true);
        } catch (FileNotFoundException e) {
            logger.error("文件保存失败:" + e.getMessage());
            return false;
        }
    }

    public static boolean save(OutputStream output, InputStream input, String key) {
        try {
            return inputToOutput(input, output, key, true);
        } catch (Exception e) {
            logger.error("文件保存失败:" + e.getMessage());
            return false;
        }
    }

    public static boolean save(OutputStream output, byte[] bytes, String key) {
        try {
            return byteToOutput(bytes, output, key, true);
        } catch (Exception e) {
            logger.error("文件保存失败:" + e.getMessage());
            return false;
        }
    }

    /**
     * @do 获取保存文件夹
     * @author lh
     * @date 2020/11/2 16:18
     */
    public static String getSaveDir(String basePath) {
        return getSaveDir(basePath, null, null);
    }

    public static String getSaveDir(String basePath, String uuid) {
        return getSaveDir(basePath, uuid, null);
    }

    /**
     * @param basePath
     * @param uuid
     * @param suffix
     * @return
     * @author lh
     * @date 2018年3月7日 下午4:05:28
     */
    public static String getSaveDir(String basePath, String uuid, String suffix) {
        if (StringUtils.isBlank(uuid)) {
            uuid = UUID.randomUUID().toString().replaceAll("-", "");
        }
        basePath = StringUtils.isBlank(basePath) ? File.separatorChar + "" : basePath + File.separatorChar;
        int hashcode = uuid.hashCode();
        int dir1 = hashcode & 0xf;
        int dir2 = (hashcode >> 4) & 0xf;
        String finalPath;
        finalPath = basePath + dir1 + File.separatorChar + dir2 + File.separatorChar;
        File file = new File(finalPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        file.setExecutable(false);
        finalPath += uuid;
        if (StringUtils.isNotBlank(suffix)) {
            finalPath += "." + suffix;
        }
        return finalPath;
    }

    /**
     * 获取文件并输出到流
     *
     * @param file
     * @param securityKey
     * @param outputStream
     * @author lh
     * @date 2018年3月10日 下午1:27:52
     */
    public static void getFile(File file, String securityKey, OutputStream outputStream) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            inputToOutput(new FileInputStream(file), outputStream, securityKey, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * @do 删除文件
     * @author lh
     * @date 2020/11/3 11:49
     */
    public static boolean delete(String path, String... suffix) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        if (suffix != null && suffix.length > 0) {
            //清除后缀文件
            for (String s : suffix) {
                delete(path.concat(".").concat(s));
            }
        }
        return true;
    }

    /**
     * @do 读取文件内容
     * @author lh
     * @date 2019-10-12 15:50
     */
    public static String readFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * @do 获取后缀
     * @author lh
     * @date 2020/11/2 15:03
     */
    public static String getSuffix(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        filename = filename.substring(filename.lastIndexOf(".") + 1);
        if (StringUtils.isBlank(filename)) {
            return "unknown";
        }
        if (filename.length() > 10) {
            filename = filename.substring(0, 8).concat("_");
        }
        return filename;
    }

    /**
     * @do 获取基础文件夹
     * @author lh
     * @date 2020/11/2 15:09
     */
    public static String getBaseDir(String basePath, String suffix) {
        if (StringUtils.isBlank(suffix)) {
            suffix = "unknown";
        }
        return basePath.concat("/").concat(suffix);
    }

    /**
     * @do 文件加密处理
     * @author lh
     * @date 2020/11/2 15:38
     */
    public static void encode(byte[] bytes, String key) {
        byte[] keys = key.getBytes(StandardCharsets.UTF_8);
        int index = 0;
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] += (index % 2 == 0 ? 1 : -1) * keys[index >= keys.length ? 0 : index++];
        }
    }

    /**
     * @do 文件解密处理
     * @author lh
     * @date 2020/11/2 15:38
     */
    public static void decode(byte[] bytes, String key) {
        byte[] keys = key.getBytes(StandardCharsets.UTF_8);
        int index = 0;
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] -= (index % 2 == 0 ? 1 : -1) * keys[index >= keys.length ? 0 : index++];
        }
    }

    /**
     * @param encode true为加密,false为解密
     * @do input流转output流, 并加解密
     * @author lh
     * @date 2020/11/2 16:02
     */
    public static boolean inputToOutput(InputStream inputStream, OutputStream outputStream, String key, boolean encode) {
        byte[] keys = key==null?null:key.getBytes(StandardCharsets.UTF_8);
        int index = 0;
        try {
            byte[] buff = new byte[10240];
            int len = -1;
            while ((len = inputStream.read(buff)) > 0) {
                if(key!=null) {
                    for (int i = 0; i < len; i++) {
                        if (encode) {
                            buff[i] += (index % 2 == 0 ? 1 : -1) * keys[index >= keys.length ? 0 : index++];
                        } else {
                            buff[i] -= (index % 2 == 0 ? 1 : -1) * keys[index >= keys.length ? 0 : index++];
                        }
                    }
                }
                outputStream.write(buff, 0, len);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SystemUtil.closeStream(outputStream, inputStream);
        }
        return false;
    }

    /**
     * @param encode true为加密,false为解密
     * @do input流转output流, 并加解密
     * @author lh
     * @date 2020/11/2 16:02
     */
    private static boolean byteToOutput(byte[] bytes, OutputStream outputStream, String key, boolean encode) {
        byte[] keys = key == null ? null : key.getBytes(StandardCharsets.UTF_8);
        int index = 0;
        try {
            if (keys != null) {
                for (int i = 0; i < bytes.length; i++) {
                    if (encode) {
                        bytes[i] += (index % 2 == 0 ? 1 : -1) * keys[index >= keys.length ? 0 : index++];
                    } else {
                        bytes[i] -= (index % 2 == 0 ? 1 : -1) * keys[index >= keys.length ? 0 : index++];
                    }
                }
            }
            outputStream.write(bytes);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SystemUtil.closeStream(outputStream);
        }
        return false;
    }
}
