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

    public void save(BoardDTO boardDTO) throws IOException {
        if (boardDTO.getBoardFile() == null || boardDTO.getBoardFile().isEmpty() || boardDTO.getBoardFile().get(0).isEmpty()) {
            System.out.println("파일 없다.");
            boardDTO.setFileAttached(Long.valueOf(0));
            boardRepository.save(boardDTO);
        } else {
            System.out.println(" ===================> 파일 있다.");
    
            boardDTO.setFileAttached(Long.valueOf(1));
            BoardDTO savedBoard = boardRepository.save(boardDTO);
    
            System.out.println(" ===================> savedBoard: " + savedBoard);
    
            for (MultipartFile boardFile : boardDTO.getBoardFile()) {
                String originalFilename = boardFile.getOriginalFilename();
                System.out.println("originalFilename = " + originalFilename);
    
                String storedFileName = System.currentTimeMillis() + "-" + originalFilename;
                System.out.println("storedFileName = " + storedFileName);
    
                BoardFileDTO boardFileDTO = new BoardFileDTO();
                boardFileDTO.setOriginalFileName(originalFilename);
                boardFileDTO.setStoredFileName(storedFileName);
                boardFileDTO.setBoardId(savedBoard.getId());
    
                // File save path, ensure this directory exists
                String savePath = "/home/silkwave/apps/boards/spring_upload_files/" + storedFileName;
                boardFile.transferTo(new File(savePath));
    
                // Save file record to the database
                boardRepository.saveFile(boardFileDTO);
            }
        }
    }
    

    public List<BoardDTO> findAll() {
        return boardRepository.findAll();
    }

    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }

    public BoardDTO findById(Long id) {
        return boardRepository.findById(id);
    }

    public void update(BoardDTO boardDTO) {
        boardRepository.update(boardDTO);
    }

    public void delete(Long id) {
        boardRepository.delete(id);
    }

    public List<BoardFileDTO> findFile(Long id) {
        return boardRepository.findFile(id);
    }
}
