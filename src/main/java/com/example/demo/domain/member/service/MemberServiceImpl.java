package com.example.demo.domain.member.service;

import com.example.demo.domain.member.entity.Address;
import com.example.demo.domain.member.entity.AdminCode;
import com.example.demo.domain.member.entity.MemberProfile;
import com.example.demo.domain.member.repository.AdminCodeRepository;
import com.example.demo.domain.member.repository.MemberRepository;
import com.example.demo.domain.member.repository.MemberProfileRepository;
import com.example.demo.domain.member.service.request.MemberSignInRequest;
import com.example.demo.domain.member.service.request.MemberSignUpRequest;
import com.example.demo.domain.member.service.request.MemberProfileRequest;
import com.example.demo.domain.security.entity.Authentication;
import com.example.demo.domain.security.entity.BasicAuthentication;
import com.example.demo.domain.security.repository.AuthenticationRepository;
import com.example.demo.domain.security.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.demo.domain.member.entity.Member;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    final private MemberRepository memberRepository;
    final private MemberProfileRepository memberProfileRepository;

    final private AdminCodeRepository adminCodeRepository;

    final private AuthenticationRepository authenticationRepository;

    final private RedisService redisService;

    //이메일 중복 확인
    @Override
    public Boolean emailValidation(String email) {
        Optional<Member> maybeMember = memberRepository.findByEmail(email);

        if(maybeMember.isPresent()) {
            return false;
        }
        return true;
    }

    //닉네임 중복 확인
    @Override
    public Boolean nicknameValidation(String nickname) {
        Optional<Member> maybeNickName = memberRepository.findByNickName(nickname);

        if(maybeNickName.isPresent()) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean adminCodeValidation(String adminCode) {
        Optional<AdminCode> maybeAdmin = adminCodeRepository.findByCode(adminCode);
        if(maybeAdmin.isPresent()) {
            return true;
        }
        return false;
    }

    //회원가입
    @Override
    public Boolean signUp(MemberSignUpRequest memberSignUpRequest) {
        log.info("관리자코드: " + memberSignUpRequest.getAdminCode());

        if(memberSignUpRequest.getAdminCode() == null || memberSignUpRequest.getAdminCode().isEmpty()) {
            log.info("일반회원가입");
            final Member member = memberSignUpRequest.toMember();
            memberRepository.save(member);

            final BasicAuthentication authentication = new BasicAuthentication(
                    member,
                    Authentication.BASIC_AUTH,
                    memberSignUpRequest.getPassword()
            );
            authenticationRepository.save(authentication);

        } else {
            log.info("관리자가입");
            final Member member = memberSignUpRequest.toMember();
            memberRepository.save(member);

            final BasicAuthentication authentication = new BasicAuthentication(
                    member,
                    Authentication.BASIC_AUTH,
                    memberSignUpRequest.getPassword()
            );
            authenticationRepository.save(authentication);
        }

        return true;
    }

    //로그인
    @Override
    public Map<String, String> signIn(MemberSignInRequest signInRequest) {
        String email = signInRequest.getEmail();
//        String authorityCode = signInRequest.getAuthorityCode();

        Optional<Member> maybeMember = memberRepository.findByEmail(email);

        if (maybeMember.isPresent()) {
            Member memberInfo = maybeMember.get();

            if (!memberInfo.isRightPassword(signInRequest.getPassword())) {
                throw new RuntimeException("비밀번호 잘못 입력");
            }

            UUID userToken = UUID.randomUUID();

            redisService.deleteByKey(userToken.toString());
            redisService.setKeyAndValue(userToken.toString(), memberInfo.getMemberId());

            Map<String, String> userInfo = new HashMap<>();

            userInfo.put("userToken", userToken.toString());
            userInfo.put("userEmail", memberInfo.getEmail());
            userInfo.put("userNickName", memberInfo.getNickname());
            userInfo.put("userId", memberInfo.getMemberId().toString());
//            userInfo.put("authorityType", memberInfo.getAdminCheck());

            log.info("userProfile()" + userInfo);

            return userInfo;
        }

        throw new RuntimeException("가입 되어있지 않은 회원입니다. ");
    }

    //회원탈퇴
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    //정보변경



    @Override
    @Transactional
    public Boolean updateMemberInfo(Long memberId, MemberProfileRequest memberProfileRequest) {
        Optional<Member> maybeMember = memberRepository.findByMemberId(memberId);
        Optional<MemberProfile> maybeMemberUpdate = memberProfileRepository.findByMemberProfileId(memberId);
        Optional<Authentication> maybeAuthentication = authenticationRepository.findByMemberId(memberId);

        if(maybeMember.isEmpty()) {
            return false;
        }

        Member member = maybeMember.get();

        memberRepository.save(member);

        if (maybeMemberUpdate.isPresent()) {
            // 기존 MemberUpdate 객체를 덮어쓰는 방식으로 업데이트
            MemberProfile newMemberProfile = maybeMemberUpdate.get();
            newMemberProfile.setPhoneNumber(memberProfileRequest.getNewPhoneNumber());
            newMemberProfile.getAddresses().clear();
            if (memberProfileRequest.getNewAddresses() != null && !memberProfileRequest.getNewAddresses().isEmpty()) {
                for (Address address : memberProfileRequest.getNewAddresses()) {
                    newMemberProfile.addAddress(address);
                }
            }
            memberProfileRepository.save(newMemberProfile);
        } else {
            // 새로운 MemberUpdate 레코드를 생성
            MemberProfile memberProfile = memberProfileRequest.toMemberUpdate(member);
            memberProfileRepository.save(memberProfile);
        }

        if(!memberProfileRequest.getNewPassword().equals("")) {
            final BasicAuthentication authentication = new BasicAuthentication(
                    member,
                    Authentication.BASIC_AUTH,
                    memberProfileRequest.getNewPassword()
            );

            authenticationRepository.save(authentication);
        }

        return true;
    }







}
