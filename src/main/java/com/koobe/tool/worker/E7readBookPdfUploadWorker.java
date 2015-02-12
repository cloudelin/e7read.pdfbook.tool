package com.koobe.tool.worker;

import java.io.File;
import java.util.concurrent.Callable;

import com.koobe.common.core.KoobeApplication;
import com.koobe.common.data.domain.Awss3file;
import com.koobe.common.data.domain.Book;
import com.koobe.common.data.repository.BookRepository;

public class E7readBookPdfUploadWorker implements Callable<Boolean> {

	private KoobeApplication koobeApplication;
	private BookRepository bookRepository;
	private File file;
	private String bucket;
	private String filekey;
	private Book book;
	
	private MetadataCreationWorker parent;
	
	public E7readBookPdfUploadWorker(KoobeApplication koobeApplication, BookRepository bookRepository,
			File file, String bucket, String filekey, Book book, MetadataCreationWorker parent) {
		
		this.koobeApplication = koobeApplication;
		this.bookRepository = bookRepository;
		this.file = file;
		this.bucket = bucket;
		this.filekey = filekey;
		this.book = book;
		this.parent = parent;
	}
	
	public Boolean call() {
		
		Boolean result = false;

		E7readS3FileUploadWorker fileUploadWorker = new E7readS3FileUploadWorker(koobeApplication, 
				"application/pdf", file, bucket, filekey);
		
		Awss3file s3PdfFileDomain = fileUploadWorker.call();
		
		if (s3PdfFileDomain != null) {
			book = saveBookPdfFileInfo(book, s3PdfFileDomain);
			result = true;
		}
		
		if (this.parent != null) {
			this.parent.increaseProgress();
		}
		
		return result;
	}
	
	protected Book saveBookPdfFileInfo(Book book, Awss3file awss3file) {
		book.setPdfFile(awss3file);
		book.setBucket(awss3file.getBucket());
		book.setPdfFileKey(awss3file.getObjectKey());
		bookRepository.save(book);
		return book;
	}

}
