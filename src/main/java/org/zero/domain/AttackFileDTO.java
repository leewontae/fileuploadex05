package org.zero.domain;

import lombok.Data;

@Data
public class AttackFileDTO {
// 첨부 파일 의 정보들을 저장하는 class
    private String fileName;
    private String uploadPath;
    private String uuid ;
    private boolean image;
}
