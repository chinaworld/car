package com.vnd.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/1/24.
 */
public class TrainData {
    public HashMap<String, String> getAllData() {
        return allData;
    }

    HashMap<String, String> allData = new HashMap<>();

    public TrainData(InputStream in) throws IOException {
        load(in);
    }

    private void load(InputStream in) throws IOException{
        ZipArchiveInputStream zin = new ZipArchiveInputStream(in, "GBK");
        ZipArchiveEntry entry;
        while((entry = zin.getNextZipEntry())!=null) {
            if(entry.getName().endsWith(".dat")){
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                IOUtils.copy(zin, os);
                allData.put(entry.getName(), os.toString());
            }
        }
        zin.close();
    }

    public List<String> findKeys(String prefix){
        List<String> result = new ArrayList<>();
        for(String key : allData.keySet()){
            if(key.startsWith(prefix)){
                result.add(key);
            }
        }
        return result;
    }
}
