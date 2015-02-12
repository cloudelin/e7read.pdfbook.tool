package com.koobe.tool.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.koobe.common.core.KoobeApplication;
import com.koobe.common.storage.AmazonS3Storage;
import com.koobe.common.storage.KoobeStorageService;

public class FileUploadingWorker implements Callable<Boolean> {
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	private AmazonS3Storage storage;
	
	private File file;
	private String bucket;
	private String objectKey;
	
	public FileUploadingWorker(KoobeApplication koobeApplication, File file, String bucket, String objectKey) {
		
		KoobeStorageService storageService = (KoobeStorageService) koobeApplication.getService(KoobeStorageService.class);
		storage = storageService.getAmazonS3Storage();
		
		this.file = file;
		this.bucket = bucket;
		this.objectKey = objectKey;
	}

	public Boolean call() {
		boolean result = false;
		FileInputStream fis = null;
		try {
			log.info("Uploading file {}", file.getAbsolutePath());
			fis = new FileInputStream(file);
			storage.putObject(bucket, objectKey, fis, file.length());
//			storage.putObjectPublicRead(bucket, objectKey, fis, file.length());
			log.info("Success to upload file {}", file.getAbsolutePath());
			result = true;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
