package com.study.springcore.service;

import com.study.springcore.domain.Member;
import com.study.springcore.domain.MemberRepository;
import com.study.springcore.domain.MemoryMemberRepository;

public class MemberServiceImpl implements MemberService {

    private final MemberRepository repository = new MemoryMemberRepository();

    @Override
    public void join(Member member) {
        repository.save(member);
    }

    @Override
    public Member findMember(Long memberId) {
        return repository.findById(memberId);
    }
}
