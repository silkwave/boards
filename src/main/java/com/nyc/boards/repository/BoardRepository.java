package com.nyc.boards.repository;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.nyc.boards.dto.BoardDTO;
import com.nyc.boards.dto.BoardFileDTO;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BoardRepository {
    private final SqlSessionTemplate sql;

    // 게시물 저장 메서드
    public BoardDTO save(BoardDTO boardDTO) {
        sql.insert("Board.save", boardDTO); 
        return boardDTO;
    }

    // 모든 게시물 조회 메서드
    public List<BoardDTO> findAll() {
        return sql.selectList("Board.findAll");
    }

    // 게시물 조회수 증가 메서드
    public void updateHits(Long id) {
        sql.update("Board.updateHits", id);
    }

    // 게시물 ID로 조회 메서드
    public BoardDTO findById(Long id) {
        return sql.selectOne("Board.findById", id);
    }

    // 게시물 수정 메서드
    public void update(BoardDTO boardDTO) {
        sql.update("Board.update", boardDTO);
    }

    // 게시물 삭제 메서드
    public void delete(Long id) {
        sql.delete("Board.delete", id);
    }

    // 파일 저장 메서드
    public void saveFile(BoardFileDTO boardFileDTO) {
        sql.insert("Board.saveFile", boardFileDTO);
    }

    // 게시물에 첨부된 파일 조회 메서드
    public List<BoardFileDTO> findFile(Long id) {
        return sql.selectList("Board.findFile", id);
    }

    // 현재 시퀀스 값 조회 메서드 (DOCKER.BOARD_TABLE_SEQ)
    public Long getCurrentBoardSeq() {
        return sql.selectOne("Board.getCurrentBoardSeq");
    }

    // 다음 시퀀스 값 조회 메서드 (DOCKER.BOARD_TABLE_SEQ) (새로 추가됨)
    public Long getNextBoardSeq() {
        return sql.selectOne("Board.getNextBoardSeq");
    }
}
