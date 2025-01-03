package com.nyc.boards.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@ToString(exclude = "boardFile") // boardFile은 출력에서 제외
public class BoardDTO {
    private Long id;
    private String boardWriter;
    private String boardPass;
    private String boardTitle;
    private String boardContents;
    private Long boardHits;
    private String createdAt;
    private Long fileAttached;
    private List<MultipartFile> boardFile;
}
