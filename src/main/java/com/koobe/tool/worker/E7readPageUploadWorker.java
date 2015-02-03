package com.koobe.tool.worker;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import com.koobe.common.data.domain.Awss3file;
import com.koobe.common.data.domain.Book;
import com.koobe.common.data.domain.Page;
import com.koobe.common.data.repository.PageRepository;

public class E7readPageUploadWorker implements Callable<Boolean> {

	private PageRepository pageRepository;
	
	private Book book;
	private Integer pageIdx;
	private E7readS3FileUploadWorker imageUploadWorker;
	private E7readS3FileUploadWorker thumbnailUploadWorker;
	private String pageTextFilePath;
	
	private MetadataCreationWorker parent;
	
	public E7readPageUploadWorker(Book book, Integer pageIdx, 
			E7readS3FileUploadWorker imageUploadWorker,
			E7readS3FileUploadWorker thumbnailUploadWorke,
			PageRepository pageRepository,
			String pageTextFilePath, MetadataCreationWorker parent) {
		
		this.book = book;
		this.pageIdx = pageIdx;
		this.imageUploadWorker = imageUploadWorker;
		this.thumbnailUploadWorker = thumbnailUploadWorke;
		this.pageRepository = pageRepository;
		this.pageTextFilePath = pageTextFilePath;
		this.parent = parent;
	}
	
	public Boolean call() {
		boolean result = true;
		
		Page page = getOrCreatePage(book, pageIdx);
		Awss3file s3ImageFileDomain = imageUploadWorker.call();
		Awss3file s3ThumbnailFileDomain = thumbnailUploadWorker.call();
		
		File textFile = new File(pageTextFilePath);
		String text = null;
		try {
			text = FileUtils.readFileToString(textFile);
			if (text == null || text.trim().equals("")) {
				text = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (s3ImageFileDomain != null && s3ThumbnailFileDomain != null) {
			page.setFullText(text);
			savePageInfo(page, s3ImageFileDomain, s3ThumbnailFileDomain);
		} else {
			result = false;
		}
		
		this.parent.increaseProgress();
		
		return result;
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
