package com.koobe.common.data.domain;

import java.io.Serializable;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * The persistent class for the book database table.
 * 
 */
@Entity(name="book")
public class Book implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String description;

	private String ean;

	@Column(name="finished_upload")
	private Boolean finishedUpload;

	private String isbn;

	private String issn;

	private String name;

	private Boolean unedited;

	private Long version;
	
	@Column(name="is_checked")
	private Boolean isChecked;
	
	@Column(name="is_delete")
	private Boolean isDelete;
	
	@ManyToOne
	@JoinColumn(name="pdf_file_id")
	private Awss3file pdfFile;
	
	private String bucket;
	
	@Column(name="pdf_file_key")
	private String pdfFileKey;
	
	@Column(name="original_file_name")
	private String originalFileName;

	//bi-directional many-to-one association to Publisher
	@ManyToOne
	private Publisher publisher;

	//bi-directional many-to-one association to Chapter
	@OneToMany(mappedBy="book")
	private List<Chapter> chapters;

	//bi-directional many-to-one association to Page
	@OneToMany(mappedBy="book")
	private List<Page> pages = new ArrayList<Page>();

	public Book() {
		id = UUID.randomUUID().toString();
		version = 0L;
		isDelete = false;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEan() {
		return this.ean;
	}

	public void setEan(String ean) {
		this.ean = ean;
	}

	public Boolean getFinishedUpload() {
		return this.finishedUpload;
	}

	public void setFinishedUpload(Boolean finishedUpload) {
		this.finishedUpload = finishedUpload;
	}

	public String getIsbn() {
		return this.isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getIssn() {
		return this.issn;
	}

	public void setIssn(String issn) {
		this.issn = issn;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Awss3file getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(Awss3file pdfFile) {
		this.pdfFile = pdfFile;
	}
	
	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public Boolean getUnedited() {
		return this.unedited;
	}

	public void setUnedited(Boolean unedited) {
		this.unedited = unedited;
	}

	public Long getVersion() {
		return this.version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Boolean getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Boolean isDelete) {
		this.isDelete = isDelete;
	}

	
	public Boolean getIsChecked() {
		return isChecked;
	}

	public void setIsChecked(Boolean isChecked) {
		this.isChecked = isChecked;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getPdfFileKey() {
		return pdfFileKey;
	}

	public void setPdfFileKey(String pdfFileKey) {
		this.pdfFileKey = pdfFileKey;
	}

	public Publisher getPublisher() {
		return this.publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public List<Chapter> getChapters() {
		return this.chapters;
	}

	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

	public Chapter addChapter(Chapter chapter) {
		getChapters().add(chapter);
		chapter.setBook(this);

		return chapter;
	}

	public Chapter removeChapter(Chapter chapter) {
		getChapters().remove(chapter);
		chapter.setBook(null);

		return chapter;
	}

	public List<Page> getPages() {
		return this.pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	public Page addPage(Page page) {
		getPages().add(page);
		page.setBook(this);

		return page;
	}

	public Page removePage(Page page) {
		getPages().remove(page);
		page.setBook(null);

		return page;
	}

}