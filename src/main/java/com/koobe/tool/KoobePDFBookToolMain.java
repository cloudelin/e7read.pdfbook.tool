package com.koobe.tool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;
import com.koobe.common.core.KoobeApplication;
import com.koobe.common.data.KoobeDataService;
import com.koobe.common.data.domain.Awss3file;
import com.koobe.common.data.domain.Book;
import com.koobe.common.data.domain.Page;
import com.koobe.common.data.repository.Awss3fileRepository;
import com.koobe.common.data.repository.BookRepository;
import com.koobe.common.data.repository.PageRepository;
import com.koobe.common.storage.AmazonS3Storage;
import com.koobe.common.storage.KoobeStorageService;
import com.koobe.tool.enums.ConvertedResultsKeyEnum;
import com.koobe.tool.worker.MetadataCreationWorker;
import com.koobe.tool.worker.PdfBatchConversionWorker;
import com.koobe.tool.worker.PdfConversionWorker;

public class KoobePDFBookToolMain {
	
	protected static Logger log = LoggerFactory.getLogger(KoobePDFBookToolMain.class);

	static KoobeApplication koobeApplication;
	static AmazonS3Storage storage;
	
	static KoobeDataService koobeDataService;
	static BookRepository bookRepository;
	static PageRepository pageRepository;
	static Awss3fileRepository awss3fileRepository;
	
	static {
		koobeApplication = KoobeApplication.getInstance();
		
		KoobeStorageService storageService = (KoobeStorageService) koobeApplication.getService(KoobeStorageService.class);
		storage = storageService.getAmazonS3Storage();
		
		koobeDataService = (KoobeDataService) koobeApplication.getService(KoobeDataService.class);
		
		bookRepository = (BookRepository) koobeDataService.getRepository(BookRepository.class);
		pageRepository = (PageRepository) koobeDataService.getRepository(PageRepository.class);
		awss3fileRepository = (Awss3fileRepository) koobeDataService.getRepository(Awss3fileRepository.class);
	}
	
	static String pdfFolderPath = "F:\\pdf";
	static String bookBucket = "koobecloudbook";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PdfBatchConversionWorker conversionWorker = new PdfBatchConversionWorker(pdfFolderPath, 3F, 0.4F, 4);
		List<Map<ConvertedResultsKeyEnum, String>> results = conversionWorker.call();
		log.info(results.toString());
		
//		MetadataCreationWorker creationWorker = new MetadataCreationWorker(results, koobeApplication, "koobecloudbook");
//		Map<String, Boolean> resultsMap = creationWorker.call();
//		log.info(resultsMap.toString());
		
		System.exit(0);
	}

}
