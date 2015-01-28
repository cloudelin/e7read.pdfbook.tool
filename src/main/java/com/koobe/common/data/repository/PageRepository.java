package com.koobe.common.data.repository;

import org.springframework.data.repository.CrudRepository;

import com.koobe.common.data.domain.Book;
import com.koobe.common.data.domain.Page;

public interface PageRepository extends CrudRepository<Page, String> {

	public Page findByBookAndDataIndex(Book book, Integer dataIndex);
}
