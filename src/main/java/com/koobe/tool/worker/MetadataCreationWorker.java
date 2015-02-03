package com.koobe.tool.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.koobe.common.core.KoobeApplication;
import com.koobe.common.data.KoobeDataService;
import com.koobe.common.data.domain.Awss3file;
import com.koobe.common.data.domain.Book;
import com.koobe.common.data.repository.BookRepository;
import com.koobe.common.data.repository.PageRepository;
import com.koobe.tool.enums.ConvertedResultsKeyEnum;
import com.koobe.tool.ui.shell.KoobePDFBookToolShell;

public class MetadataCreationWorker implements Callable<Map<String, Boolean>> {
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	private ExecutorService executor;
	
	private KoobeApplication koobeApplication;
	private BookRepository bookRepository;
	private PageRepository pageRepository;
	
	private List<Map<ConvertedResultsKeyEnum, String>> convertedResultMapList;
	private Map<String, Boolean> results = new LinkedHashMap<String, Boolean>();
	private String bucket;
	
	private KoobePDFBookToolShell shell;
	
	private int maxProgress;
	private AtomicInteger currProgress = new AtomicInteger(0);
	
	public MetadataCreationWorker(
			List<Map<ConvertedResultsKeyEnum, String>> convertedResultMapList, 
			KoobeApplication koobeApplication,
			String bucket, int uploadThread, KoobePDFBookToolShell shell) {
		
		this.convertedResultMapList = convertedResultMapList;
		this.bucket = bucket;
		
		this.koobeApplication = koobeApplication;
		
		KoobeDataService koobeDataService = (KoobeDataService) koobeApplication.getService(KoobeDataService.class);
		
		this.bookRepository = (BookRepository) koobeDataService.getRepository(BookRepository.class);
		this.pageRepository = (PageRepository) koobeDataService.getRepository(PageRepository.class);
		
		executor = Executors.newFixedThreadPool(uploadThread);
		
		this.shell = shell;
	}
	
	public Map<String, Boolean> call() {
		
		for (Map<ConvertedResultsKeyEnum, String> convertedResultMap : convertedResultMapList) {
			
			long start = System.currentTimeMillis();
			
			String pdfFilePath = convertedResultMap.get(ConvertedResultsKeyEnum.PDF_FILE_PATH);
			String pagesFolderPath = convertedResultMap.get(ConvertedResultsKeyEnum.IMAGES_FOLDER_PATH);
			String pagesFullSizeFolderPath = pagesFolderPath + File.separator + "page_fullsize";
			String pagesThumbnailFolderPath = pagesFolderPath + File.separator + "page_thumbnail";
			String pagesTextFolderPath = pagesFolderPath + File.separator + "page_text";
			
			final File pdfFile = new File(pdfFilePath);
			File fullsizeFolderFile = new File(pagesFullSizeFolderPath);
			
			if (shell != null) {
				shell.executeOnUIThread(new Runnable() {
					public void run() {
						shell.getListFileResult().add("正在上傳檔案：" + pdfFile.getName());
					}
				});
			}
			
			log.info("PDF file path: {}", pdfFilePath);
			
			Book book = getOrCreateBook(pdfFile.getName(), pdfFile.length());
			
			String key_prefix = "book" +  "/" +  book.getId().substring(0, 1) +  "/" + book.getId().substring(1, 2) + 
					"/" + book.getId().substring(2, 3) +  "/" + book.getId();
			
			String filePdfKey = key_prefix + "/pdf/" + pdfFile.getName();
			
			List<File> files = (List<File>) FileUtils.listFiles(fullsizeFolderFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			
			boolean isFinishedUpload = true;
			int index = 0;
			maxProgress = files.size() + 1;
			
			List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
			
			E7readBookPdfUploadWorker pdfUploadWorker = new E7readBookPdfUploadWorker(koobeApplication, bookRepository, pdfFile, bucket, filePdfKey, book, this);
			tasks.add(pdfUploadWorker);
			
			for (File filePage : files) {
				
				index++;

				File fileThumbnail = new File(pagesThumbnailFolderPath + File.separator + filePage.getName());
				
				String filePageKey = key_prefix + "/page_fullsize/" + filePage.getName();
				String fileThumbnailKey = key_prefix + "/page_thumbnail/" + fileThumbnail.getName();
				
				E7readS3FileUploadWorker fileUploadWorker = 
						new E7readS3FileUploadWorker(koobeApplication, "image/jpeg", filePage, bucket, filePageKey);
				
				E7readS3FileUploadWorker thumbnailUploadWorker = 
						new E7readS3FileUploadWorker(koobeApplication, "image/jpeg", fileThumbnail, bucket, fileThumbnailKey);
				
				String pageTextFilePath = pagesTextFolderPath + File.separator + "page_" + String.format("%04d", index) + ".txt";
				
				E7readPageUploadWorker pageUploadWorker = 
						new E7readPageUploadWorker(book, index, fileUploadWorker, thumbnailUploadWorker, pageRepository, pageTextFilePath, this);
				
				tasks.add(pageUploadWorker);
			}
			
			try {
				List<Future<Boolean>> futures = executor.invokeAll(tasks);
				for (Future<Boolean> future : futures) {
					if (!future.get()) {
						isFinishedUpload = false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			final double elapsed = (System.currentTimeMillis() - start) / 1000D;
			
			if (isFinishedUpload) {
				book.setFinishedUpload(true);
				bookRepository.save(book);
				results.put(pagesFolderPath, true);
				if (shell != null) {
					shell.executeOnUIThread(new Runnable() {
						public void run() {
							shell.getListFileResult().add("上傳檔案完成：" + pdfFile.getName() + " (" + elapsed + "s)");
						}
					});
				}
			} else {
				results.put(pagesFolderPath, false);
				if (shell != null) {
					shell.executeOnUIThread(new Runnable() {
						public void run() {
							shell.getListFileResult().add("上傳檔案失敗：" + pdfFile.getName() + " (" + elapsed + "s)");
						}
					});
				}
			}
			
			currProgress.set(0);
		}
				
		return results;
	}
	
	public void increaseProgress() {
		final int curr = currProgress.incrementAndGet();
		if (this.shell != null) {
			shell.executeOnUIThread(new Runnable() {
				public void run() {
					shell.getProgressBar().setMaximum(maxProgress);
					shell.getProgressBar().setSelection(curr);
					shell.getLblSrvStatus().setText("執行中 (" + curr + "/" + maxProgress + ")");
				}
			});
		}
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
}
