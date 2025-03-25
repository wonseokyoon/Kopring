package com.example.upload.domain.member.member.dto

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.post.post.entity.Post
import lombok.Getter
import org.springframework.lang.NonNull

class MemberDto(
    val id:Long,
    val nickname:String,
    val profileImgUrl:String
) {

    constructor(member: Member) : this(
        id = member.id!!,
        nickname=member.nickname,
        profileImgUrl=member.profileImgUrlOrDefault
    )
}
