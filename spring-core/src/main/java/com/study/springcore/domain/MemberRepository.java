package com.study.springcore.domain;

public interface MemberRepository {

    void save(Member member);

    Member findById(Long id);

}
