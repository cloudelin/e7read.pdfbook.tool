package com.koobe.common.data.domain;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.*;


/**
 * The persistent class for the page database table.
 * 
 */
@Entity(name="page")
public class Page implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name="data_index")
	private Integer dataIndex;
	
	@Column(name="bucket")
	private String bucket;

	@Column(name="image_key")
	private String imageKey;

	@Column(name="thumbnail_key")
	private String thumbnailKey;

	private Long version;
	
	@Column(name="full_text")
	private String fullText;

	//bi-directional many-to-one association to Awss3file
	@ManyToOne
	@JoinColumn(name="thumbnail_file_id")
	private Awss3file awss3file1;

	//bi-directional many-to-one association to Book
	@ManyToOne
	private Book book;

	//bi-directional many-to-one association to Awss3file
	@ManyToOne
	@JoinColumn(name="image_file_id")
	private Awss3file awss3file2;

	public Page() {
		id = UUID.randomUUID().toString();
		version = 0L;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getDataIndex() {
		return this.dataIndex;
	}

	public void setDataIndex(Integer dataIndex) {
		this.dataIndex = dataIndex;
	}
	
	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getImageKey() {
		return imageKey;
	}

	public void setImageKey(String imageKey) {
		this.imageKey = imageKey;
	}

	public String getThumbnailKey() {
		return thumbnailKey;
	}

	public void setThumbnailKey(String thumbnailKey) {
		this.thumbnailKey = thumbnailKey;
	}

	public Long getVersion() {
		return this.version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getFullText() {
		return fullText;
	}

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	public Awss3file getAwss3file1() {
		return this.awss3file1;
	}

	public void setAwss3file1(Awss3file awss3file1) {
		this.awss3file1 = awss3file1;
	}

	public Book getBook() {
		return this.book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public Awss3file getAwss3file2() {
		return this.awss3file2;
	}

	public void setAwss3file2(Awss3file awss3file2) {
		this.awss3file2 = awss3file2;
	}

}