package com.koobe.common.data.repository;

import org.springframework.data.repository.CrudRepository;

import com.koobe.common.data.domain.Awss3file;

public interface Awss3fileRepository extends CrudRepository<Awss3file, String>{

	public Awss3file findByBucketAndObjectKey(String bucket, String objectKey);
}
