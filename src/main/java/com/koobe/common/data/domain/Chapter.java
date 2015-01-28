package com.koobe.common.data.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the chapter database table.
 * 
 */
@Entity(name="chapter")
public class Chapter implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name="data_index")
	private Integer dataIndex;

	private String description;

	private String title;

	private Long version;

	//bi-directional many-to-one association to Book
	@ManyToOne
	private Book book;

	public Chapter() {
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

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getVersion() {
		return this.version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Book getBook() {
		return this.book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

}