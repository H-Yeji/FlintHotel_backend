package com.hotel.flint.user.member.service;

import com.hotel.flint.common.dto.UserLoginDto;
import com.hotel.flint.common.enumdir.Option;
import com.hotel.flint.user.employee.repository.EmployeeRepository;
import com.hotel.flint.user.member.domain.Member;
import com.hotel.flint.user.member.dto.MemberSignUpDto;
import com.hotel.flint.user.member.dto.MemberDetResDto;
import com.hotel.flint.user.member.dto.MemberModResDto;
import com.hotel.flint.user.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Member memberSignUp(MemberSignUpDto dto) {
        if (memberRepository.findByEmailAndDelYN(dto.getEmail(), Option.N).isPresent() ||
                employeeRepository.findByEmailAndDelYN(dto.getEmail(), Option.N).isPresent()) {
            throw new IllegalArgumentException("해당 이메일로 이미 가입한 계정이 존재합니다.");
        }
        if (memberRepository.findByPhoneNumberAndDelYN(dto.getPhoneNumber(), Option.N).isPresent() ||
                employeeRepository.findByPhoneNumberAndDelYN(dto.getPhoneNumber(), Option.N).isPresent()) {
            throw new IllegalArgumentException("해당 번호로 이미 가입한 계정이 존재합니다");
        }
        return memberRepository.save(dto.toEntity(passwordEncoder.encode(dto.getPassword())));
    }

    public String findEmail(String phoneNumber) {
        Member member = memberRepository.findByPhoneNumberAndDelYN(phoneNumber, Option.N).orElseThrow(
                () -> new EntityNotFoundException("해당 번호로 가입한 아이디가 없습니다."));
        return member.getEmail();
    }

    public void updatePassword(String email) {
        Member member = memberRepository.findByEmailAndDelYN(email, Option.N).orElseThrow(
                () -> new EntityNotFoundException("해당 이메일로 가입한 아이디가 없습니다."));
        // 비밀번호 업데이트 로직 필요
    }

    public Member login(UserLoginDto dto) {
        Member member = memberRepository.findByEmailAndDelYN(dto.getEmail(), Option.N).orElseThrow(
                () -> new EntityNotFoundException("해당 이메일로 가입한 아이디가 없습니다."));
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public MemberDetResDto memberDetail(Long id) {
        Member member = findByUserId(id);
        return member.detUserEntity();
    }


//    멤버 삭제 로직
    public void memberDelete(Long id){
        Member member = memberRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("해당 id가 존재하지 않습니다."));
        member.deleteUser();
        memberRepository.save(member);
    }
    
//    멤버 비밀번호 수정 로직
    public void memberModify(MemberModResDto dto){

        Member member = this.findByUserId(dto.getId());
        member.modifyUser(dto.getPassword());
        memberRepository.save(member);
    }
    /*
    * 멤버 id로 member 객체 찾는 로직
    * */
    public Member findByUserId(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 id가 존재하지 않습니다."));
        return member;
    }
}
