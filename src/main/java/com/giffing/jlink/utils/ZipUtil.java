package com.giffing.jlink.utils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ZipUtil {

	public static void unzipFile(String jarFilePath, String targetDirectory) {
		 try {
	         ZipFile zipFile = new ZipFile(jarFilePath);
	         zipFile.extractAll(targetDirectory);
	    } catch (ZipException e) {
	        throw new IllegalStateException(e);
	    }
	}
	
}
