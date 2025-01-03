package com.nyc.boards.service;

import com.nyc.boards.dto.BoardDTO;
import com.nyc.boards.dto.BoardFileDTO;
import com.nyc.boards.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    // 게시물 저장 메서드
    public void save(BoardDTO boardDTO) throws IOException {
        // 파일이 첨부되지 않은 경우
        if (boardDTO.getBoardFile() == null || boardDTO.getBoardFile().isEmpty() || boardDTO.getBoardFile().get(0).isEmpty()) {
            System.out.println("파일 없다.");
            boardDTO.setFileAttached(Long.valueOf(0));  // 파일이 없으므로 fileAttached를 0으로 설정
            boardRepository.save(boardDTO);  // 게시물 저장
        } else {
            System.out.println(" ===================> 파일 있다.");
    
            // 현재 게시물 시퀀스 가져오기
            Long nextSeq = boardRepository.getNextBoardSeq();
            boardDTO.setId(nextSeq);  

            boardDTO.setFileAttached(Long.valueOf(1));
            
            // 게시물 저장 후, 저장된 게시물 정보를 반환받음
            BoardDTO savedBoard = boardRepository.save(boardDTO);
    
            System.out.println(" ===================> savedBoard: " + savedBoard);
    
            // 첨부된 파일이 있을 경우 파일 저장 처리
            for (MultipartFile boardFile : boardDTO.getBoardFile()) {
                String originalFilename = boardFile.getOriginalFilename();  // 원본 파일명
                System.out.println("originalFilename = " + originalFilename);
    
                // 저장할 파일명 생성 (현재 시간 + 원본 파일명)
                String storedFileName = System.currentTimeMillis() + "-" + originalFilename;
                System.out.println("storedFileName = " + storedFileName);
    
                // 파일 정보를 담을 BoardFileDTO 객체 생성
                BoardFileDTO boardFileDTO = new BoardFileDTO();
                boardFileDTO.setOriginalFileName(originalFilename);  // 원본 파일명 설정
                boardFileDTO.setStoredFileName(storedFileName);      // 저장된 파일명 설정
                boardFileDTO.setBoardId(savedBoard.getId());         // 게시물 ID 설정
    
                // 파일 저장 경로 지정 (파일이 실제로 저장될 디렉토리)
                String savePath = "/home/silkwave/apps/boards/spring_upload_files/" + storedFileName;
                
                // 파일을 해당 경로에 저장
                boardFile.transferTo(new File(savePath));
    
                // 데이터베이스에 파일 정보 저장
                boardRepository.saveFile(boardFileDTO);
            }
        }
    }
    
    // 모든 게시물 조회
    public List<BoardDTO> findAll() {
        return boardRepository.findAll();
    }

    // 게시물 조회수 증가
    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }

    // 게시물 ID로 조회
    public BoardDTO findById(Long id) {
        return boardRepository.findById(id);
    }

    // 게시물 수정
    public void update(BoardDTO boardDTO) {
        boardRepository.update(boardDTO);
    }

    // 게시물 삭제
    public void delete(Long id) {
        boardRepository.delete(id);
    }

    // 게시물에 첨부된 파일 조회
    public List<BoardFileDTO> findFile(Long id) {
        return boardRepository.findFile(id);
    }
}
