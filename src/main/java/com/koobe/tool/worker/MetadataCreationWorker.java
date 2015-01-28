package com.koobe.tool.worker;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.koobe.common.core.KoobeApplication;
import com.koobe.common.data.KoobeDataService;
import com.koobe.common.data.domain.Awss3file;
import com.koobe.common.data.domain.Book;
import com.koobe.common.data.domain.Page;
import com.koobe.common.data.repository.BookRepository;
import com.koobe.common.data.repository.PageRepository;
import com.koobe.tool.enums.ConvertedResultsKeyEnum;

public class MetadataCreationWorker implements Callable<Map<String, Boolean>> {
	
	protected Logger log = LoggerFactory.getLogger(getClass());

	private KoobeApplication koobeApplication;
	private BookRepository bookRepository;
	private PageRepository pageRepository;
	
	private List<Map<ConvertedResultsKeyEnum, String>> convertedResultMapList;
	private Map<String, Boolean> results = new LinkedHashMap<String, Boolean>();
	private String bucket;
	
	public MetadataCreationWorker(
			List<Map<ConvertedResultsKeyEnum, String>> convertedResultMapList, 
			KoobeApplication koobeApplication,
			String bucket) {
		
		this.convertedResultMapList = convertedResultMapList;
		this.bucket = bucket;
		
		this.koobeApplication = koobeApplication;
		
		KoobeDataService koobeDataService = (KoobeDataService) koobeApplication.getService(KoobeDataService.class);
		
		this.bookRepository = (BookRepository) koobeDataService.getRepository(BookRepository.class);
		this.pageRepository = (PageRepository) koobeDataService.getRepository(PageRepository.class);
	}
	
	public Map<String, Boolean> call() {
		
		for (Map<ConvertedResultsKeyEnum, String> convertedResultMap : convertedResultMapList) {
			
			String pdfFilePath = convertedResultMap.get(ConvertedResultsKeyEnum.PDF_FILE_PATH);
			String pagesFolderPath = convertedResultMap.get(ConvertedResultsKeyEnum.IMAGES_FOLDER_PATH);
			String pagesFullSizeFolderPath = pagesFolderPath + File.separator + "page_fullsize";
			String pagesThumbnailFolderPath = pagesFolderPath + File.separator + "page_thumbnail";
			
			File pdfFile = new File(pdfFilePath);
			File fullsizeFolderFile = new File(pagesFullSizeFolderPath);
			
			log.info("PDF file path: {}", pdfFilePath);
			
			Book book = getOrCreateBook(pdfFile.getName(), pdfFile.length());
			
			String key_prefix = "book" +  "/" +  book.getId().substring(0, 1) +  "/" + book.getId().substring(1, 2) + 
					"/" + book.getId().substring(2, 3) +  "/" + book.getId();
			
			List<File> files = (List<File>) FileUtils.listFiles(fullsizeFolderFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			
			boolean isFinishedUpload = true;
			int index = 0;
			
			for (File filePage : files) {
				
				index++;
				Page page = getOrCreatePage(book, index);
				
				File fileThumbnail = new File(pagesThumbnailFolderPath + File.separator + filePage.getName());
				String filePageKey = key_prefix + "/page_fullsize/" + filePage.getName();
				String fileThumbnailKey = key_prefix + "/page_thumbnail/" + fileThumbnail.getName();
				
				Awss3file s3ImageFileDomain = null;
				Awss3file s3ThumbnailFileDomain = null;
				
				if (page.getImageFileUrl() == null) {
					E7readS3FileUploadWorker fileUploadWorker = new E7readS3FileUploadWorker(koobeApplication, "image/jpeg", 
							filePage, bucket, filePageKey);
					s3ImageFileDomain = fileUploadWorker.call();
				}
				
				if (page.getThumbnailFileUrl() == null) {
					E7readS3FileUploadWorker fileUploadWorker = new E7readS3FileUploadWorker(koobeApplication, "image/jpeg", 
							fileThumbnail, bucket, fileThumbnailKey);
					s3ThumbnailFileDomain = fileUploadWorker.call();
				}
				
				if (s3ImageFileDomain != null && s3ThumbnailFileDomain != null) {
					savePageInfo(page, s3ImageFileDomain, s3ThumbnailFileDomain);
				} else {
					isFinishedUpload = false;
				}
			}
			
			// Upload PDF file
			String filePdfKey = key_prefix + "/pdf/" + pdfFile.getName();
			E7readS3FileUploadWorker fileUploadWorker = new E7readS3FileUploadWorker(koobeApplication, "application/pdf", 
						pdfFile, bucket, filePdfKey);
			Awss3file s3PdfFileDomain = fileUploadWorker.call();
			if (s3PdfFileDomain != null) {
				book = saveBookPdfFileInfo(book, s3PdfFileDomain);
			} else {
				isFinishedUpload = false;
			}
			
			if (isFinishedUpload) {
				book.setFinishedUpload(true);
				bookRepository.save(book);
				results.put(pagesFolderPath, true);
			} else {
				results.put(pagesFolderPath, false);
			}
		}
				
		return results;
	}
	
	private Book getOrCreateBook(String fileName, Long fileLength) {
		
		Book book = null;
		
		List<Book> books = bookRepository.findByOriginalFileName(fileName);
		for (Book bookObj : books) {
			if (bookObj.getPdfFile() != null && 
					bookObj.getPdfFile().getContentLength() != null &&
					bookObj.getPdfFile().getContentLength().equals(fileLength)) {
				book = bookObj;
				break;
			}
		}
		
		if (book != null) {
			book.setFinishedUpload(false);
		} else {
			book = new Book();
			book.setName("[新上傳未編輯]");
			book.setOriginalFileName(fileName);
			book.setUnedited(true);
			book.setFinishedUpload(false);
		}
				
		bookRepository.save(book);
		return book;
	}
	
	private Book saveBookPdfFileInfo(Book book, Awss3file awss3file) {
		book.setPdfFile(awss3file);
		book.setPdfFileUrl(awss3file.getResourceUrl());
		bookRepository.save(book);
		return book;
	}
	
	private Page getOrCreatePage(Book book, Integer index) {
		
		Page page = null;
		page = pageRepository.findByBookAndDataIndex(book, index);
		
		if (page == null) {
			page = new Page();
			page.setBook(book);
			page.setDataIndex(index);
			pageRepository.save(page);
		}
		return page;
	}
	
	private Page savePageInfo(Page page, Awss3file awss3file1, Awss3file awss3file2) {
		page.setAwss3file1(awss3file2);
		page.setAwss3file2(awss3file1);
		page.setImageFileUrl(awss3file1.getResourceUrl());
		page.setThumbnailFileUrl(awss3file2.getResourceUrl());
		pageRepository.save(page);
		return page;
	}
}
