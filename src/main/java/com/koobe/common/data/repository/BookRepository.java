package com.koobe.common.data.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.koobe.common.data.domain.Book;

public interface BookRepository extends CrudRepository<Book, String> {

	public List<Book> findByOriginalFileName(String originalFileName);
}
