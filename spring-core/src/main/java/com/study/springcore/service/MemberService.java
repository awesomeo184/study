package com.study.springcore.service;

import com.study.springcore.domain.Member;

public interface MemberService {

    void join(Member member);

    Member findMember(Long memberId);
}
