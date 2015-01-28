package com.koobe.common.data.domain;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the awss3file database table.
 * 
 */
@Entity(name="awss3file")
public class Awss3file implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	private String bucket;

	@Column(name="content_length")
	private Long contentLength;

	@Column(name="content_type")
	private String contentType;

	@Column(name="object_key")
	private String objectKey;

	private String remark;

	@Column(name="resource_url")
	private String resourceUrl;

	private Long version;

	//bi-directional many-to-one association to Page
	@OneToMany(mappedBy="awss3file1")
	private List<Page> pages1;

	//bi-directional many-to-one association to Page
	@OneToMany(mappedBy="awss3file2")
	private List<Page> pages2;

	public Awss3file() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBucket() {
		return this.bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public Long getContentLength() {
		return this.contentLength;
	}

	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getObjectKey() {
		return this.objectKey;
	}

	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getResourceUrl() {
		return this.resourceUrl;
	}

	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	public Long getVersion() {
		return this.version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public List<Page> getPages1() {
		return this.pages1;
	}

	public void setPages1(List<Page> pages1) {
		this.pages1 = pages1;
	}

	public Page addPages1(Page pages1) {
		getPages1().add(pages1);
		pages1.setAwss3file1(this);

		return pages1;
	}

	public Page removePages1(Page pages1) {
		getPages1().remove(pages1);
		pages1.setAwss3file1(null);

		return pages1;
	}

	public List<Page> getPages2() {
		return this.pages2;
	}

	public void setPages2(List<Page> pages2) {
		this.pages2 = pages2;
	}

	public Page addPages2(Page pages2) {
		getPages2().add(pages2);
		pages2.setAwss3file2(this);

		return pages2;
	}

	public Page removePages2(Page pages2) {
		getPages2().remove(pages2);
		pages2.setAwss3file2(null);

		return pages2;
	}

}