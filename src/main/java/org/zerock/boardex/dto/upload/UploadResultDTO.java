package org.zerock.boardex.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResultDTO {
    private String uuid;
    private String fileName;
    private boolean img;
    public String getLink() {
        // 만약 img 가 있으면
        if(img) {
            // "s_" : 썸네일이라는 뜻
            // 만약 이미지라면, s를 적은 파일 네임을 반환해주고
            return "s_" + uuid + "_" + fileName;
        } else {
            // 아니라면, s가 붙지않은 파일네임 반환
            return uuid + "_" + fileName;
        }
    }
}
