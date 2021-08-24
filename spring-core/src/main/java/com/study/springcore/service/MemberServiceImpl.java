package com.study.springcore.service;

import com.study.springcore.domain.Member;
import com.study.springcore.domain.MemberRepository;
import com.study.springcore.domain.MemoryMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberServiceImpl implements MemberService {

    private final MemberRepository repository;

    @Autowired
    public MemberServiceImpl(MemberRepository repository) {
        this.repository = repository;
    }

    @Override
    public void join(Member member) {
        repository.save(member);
    }

    @Override
    public Member findMember(Long memberId) {
        return repository.findById(memberId);
    }
}
