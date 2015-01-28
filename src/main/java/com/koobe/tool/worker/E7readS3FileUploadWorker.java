package com.koobe.tool.worker;

import java.io.File;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.koobe.common.core.KoobeApplication;
import com.koobe.common.data.KoobeDataService;
import com.koobe.common.data.domain.Awss3file;
import com.koobe.common.data.repository.Awss3fileRepository;
import com.koobe.common.storage.AmazonS3Storage;
import com.koobe.common.storage.KoobeStorageService;

/**
 * Upload a associated s3 file with e7read, if domain class exists it will not be uploaded
 * @author cloude
 *
 */
public class E7readS3FileUploadWorker implements Callable<Awss3file> {
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	private KoobeApplication koobeApplication;
	private Awss3fileRepository awss3fileRepository;
	private AmazonS3Storage storage;
	
	private String fileType;
	private File file;
	private String bucket;
	private String objectKey;
	
	public E7readS3FileUploadWorker(KoobeApplication koobeApplication, 
			String fileType, File file, String bucket, String objectKey) {
		
		this.koobeApplication = koobeApplication;
		
		this.fileType = fileType;
		this.file = file;
		this.bucket = bucket;
		this.objectKey = objectKey;
		
		KoobeDataService koobeDataService = (KoobeDataService) koobeApplication.getService(KoobeDataService.class);
		
		this.awss3fileRepository = (Awss3fileRepository) koobeDataService.getRepository(Awss3fileRepository.class);
		
		KoobeStorageService storageService = (KoobeStorageService) koobeApplication.getService(KoobeStorageService.class);
		storage = storageService.getAmazonS3Storage();
	}

	public Awss3file call() {
		
		Awss3file awss3file = null;
		
		awss3file = awss3fileRepository.findByBucketAndObjectKey(bucket, objectKey);
		
		if (awss3file == null) {
			FileUploadingWorker worker = new FileUploadingWorker(koobeApplication, file, bucket, objectKey);
			Boolean result = worker.call();
			
			if (!result) {
				log.warn("Fail to upload file {}, retrying 1", file.getName());
				result = worker.call();
				if (!result) {
					log.warn("Fail to upload file {}, retrying 2", file.getName());
					result = worker.call();
				}
			}
			
			if (result) {
				awss3file = createAwss3file(fileType, file.length(), objectKey, 
						storage.getUrl(bucket, objectKey)); 
			}
		} else {
			log.info("S3 file {} has already exists", objectKey);
		}
		
		return awss3file;
	}
	
	private Awss3file createAwss3file(String fileType, Long fileLength, String objectKey, String url) {
		Awss3file awss3file = new Awss3file();
		awss3file.setVersion(0L);
		awss3file.setBucket(bucket);
		awss3file.setContentLength(fileLength);
		awss3file.setContentType(fileType);
		awss3file.setObjectKey(objectKey);
		awss3file.setResourceUrl(url);
		awss3fileRepository.save(awss3file);
		return awss3file;
	}
}
