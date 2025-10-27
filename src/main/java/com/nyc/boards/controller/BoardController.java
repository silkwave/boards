package com.nyc.boards.controller;

import com.nyc.boards.dto.BoardDTO;
import com.nyc.boards.dto.BoardFileDTO;
import com.nyc.boards.service.BoardService;
import com.nyc.boards.util.GuidQueueUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final GuidQueueUtil guidQueueUtil;


    // --- Page Serving Endpoints (Return HTML files) ---

    @GetMapping("/save")
    public String save() {
        return "save";
    }

    @GetMapping("/list")
    public String list() {
        return "list";
    }

    @GetMapping("/{id}")
    public String detail() {
        return "detail";
    }

    @GetMapping("/update/{id}")
    public String update() {
        return "update";
    }

    // --- API Endpoints for Client-Side Rendering ---

    /**
     * (API) ID로 게시글 상세 정보를 JSON으로 반환합니다.
     * 조회수 증가 로직을 포함합니다.
     * @param id 게시글 ID
     * @return 성공 시 BoardDTO, 실패 시 404 Not Found
     */
    @GetMapping("/api/boards/{id}")
    @ResponseBody
    public ResponseEntity<BoardDTO> getBoardById(@PathVariable("id") Long id) {
        boardService.updateHits(id);
        BoardDTO boardDTO = boardService.findById(id);
        return boardDTO != null ? ResponseEntity.ok(boardDTO) : ResponseEntity.notFound().build();
    }

    /**
     * (API) 모든 게시글 목록을 JSON으로 반환합니다.
     * @return 게시글 목록
     */
    @GetMapping("/api/boards")
    @ResponseBody
    public ResponseEntity<List<BoardDTO>> getAllBoards() throws InterruptedException {
        String guid = guidQueueUtil.getGUID();
        log.info("Request GUID for getAllBoards: [{}]", guid);
        List<BoardDTO> boardDTOList = boardService.findAll();
        return ResponseEntity.ok(boardDTOList);
    }

    /**
     * (API) 새로운 게시글을 저장합니다. (파일 포함)
     */
    @PostMapping("/api/boards")
    public ResponseEntity<Long> save(@Validated BoardDTO boardDTO) throws IOException {
        BoardDTO savedBoard = boardService.save(boardDTO);
        log.info("@PostMapping(\"/api/boards\") : {}", savedBoard.getBoardTitle());
        return ResponseEntity.ok(savedBoard.getId());
    }

    /**
     * (API) 게시글 정보를 수정합니다.
     */
    @PutMapping("/api/boards/{id}")
    public ResponseEntity<Void> update(@RequestBody BoardDTO boardDTO) {
        log.info(" @PutMapping(\"/api/boards/{id}\") : {}", boardDTO);
        boardService.update(boardDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * (API) 게시글을 삭제합니다.
     */
    @DeleteMapping("/api/boards/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        log.info(" @DeleteMapping(\"/api/boards/{id}\") : {}", id);
        boardService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * (API) 특정 게시글의 파일 목록을 JSON으로 반환합니다.
     */
    @GetMapping("/api/boards/{id}/files")
    @ResponseBody
    public ResponseEntity<List<BoardFileDTO>> getBoardFiles(@PathVariable("id") Long id) {
        List<BoardFileDTO> boardFileDTOList = boardService.findFile(id);
        return ResponseEntity.ok(boardFileDTOList);
    }
}
