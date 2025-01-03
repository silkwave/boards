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

    public BoardDTO save(BoardDTO boardDTO) {
        sql.insert("Board.save", boardDTO); // The id is populated automatically
        return boardDTO;
    }

    public List<BoardDTO> findAll() {
        return sql.selectList("Board.findAll");
    }

    public void updateHits(Long id) {
        sql.update("Board.updateHits", id);
    }

    public BoardDTO findById(Long id) {
        return sql.selectOne("Board.findById", id);
    }

    public void update(BoardDTO boardDTO) {
        sql.update("Board.update", boardDTO);
    }

    public void delete(Long id) {
        sql.delete("Board.delete", id);
    }

    public void saveFile(BoardFileDTO boardFileDTO) {
        sql.insert("Board.saveFile", boardFileDTO);
    }

    public List<BoardFileDTO> findFile(Long id) {
        return sql.selectList("Board.findFile", id);
    }

    // Get the current value of the sequence DOCKER.BOARD_TABLE_SEQ
    public Long getCurrentBoardSeq() {
        return sql.selectOne("Board.getCurrentBoardSeq");
    }

    // Get the next value of the sequence DOCKER.BOARD_TABLE_SEQ (NEWLY ADDED)
    public Long getNextBoardSeq() {
        return sql.selectOne("Board.getNextBoardSeq");
    }
}
