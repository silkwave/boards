package com.nyc.boards.controller;

import com.nyc.boards.dto.BoardDTO;
import com.nyc.boards.dto.BoardFileDTO;
import com.nyc.boards.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j  // 로깅을 위한 어노테이션
public class BoardController {
    private final BoardService boardService;

    // 게시글 작성 화면으로 이동
    @GetMapping("/save")
    public String save() {
        return "save";
    }

    // 게시글 작성 처리
    @PostMapping("/save")
    public String save(@Validated BoardDTO boardDTO) throws IOException {

        // 게시글 저장 서비스 호출
        boardService.save(boardDTO);
        log.info("@PostMapping(\"/save\") : {}", boardDTO.getBoardTitle());
        return "redirect:/list";  // 목록 페이지로 리다이렉트
    }

    // 게시글 목록 조회
    @GetMapping("/list")
    public String findAll(Model model) {
        List<BoardDTO> boardDTOList = boardService.findAll();

        log.info(" @GetMapping(\"/list\") : {}", boardDTOList);

        model.addAttribute("boardList", boardDTOList);
        return "list";  // 목록 페이지로 반환
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public String findById(@PathVariable("id") String idString, Model model) {
        try {

            log.error("==================Invalid board ID 0: {}", idString);

            Long id = Long.valueOf(idString);  // Try converting the id to Long
            
            // Proceed if the id is valid
            log.info(" @GetMapping(\"/{id}\") : {}", id); 
            
            BoardDTO boardDTO = boardService.findById(id);

            log.error("(\"==================Invalid board ID 1: {}", idString);

            if (boardDTO == null) {
                log.error("Board with id {} not found", id);
                return "redirect:/list";  // Board not found, redirect to list
            }


            boardService.updateHits(id);
            model.addAttribute("board", boardDTO);

            log.error("(\"================== Invalid board ID 2: {}", idString);

            if (boardDTO.getFileAttached() == 1) {
                List<BoardFileDTO> boardFileDTOList = boardService.findFile(id);
                model.addAttribute("boardFileList", boardFileDTOList);
            }


            log.error("(\"================== Invalid board ID 3: {}", idString);

            return "detail";
        } catch (NumberFormatException e) {
            // Handle invalid ID (e.g., "favicon.ico" or non-numeric IDs)
            log.error("================== Invalid board ID 4: {}", idString);
            return "redirect:/list";  // Redirect to list
        }
    }
    

    // 게시글 수정 화면으로 이동
    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, Model model) {

        log.info(" @GetMapping(\"/update/{id}\") : {}", id);

        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        return "update";  // 수정 페이지로 이동
    }

    // 게시글 수정 처리
    @PostMapping("/update/{id}")
    public String update(BoardDTO boardDTO) {

        log.info(" @PostMapping(\"/update/{id}\") : {}", boardDTO);

        boardService.update(boardDTO);  // 수정된 내용 저장
        return "redirect:/" + boardDTO.getId();  // 수정된 게시글의 상세 페이지로 리다이렉트
    }

    // 게시글 삭제 처리
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {

        log.info(" @GetMapping(\"/delete/{id}\") : {}", id);

        boardService.delete(id);  // 게시글 삭제
        return "redirect:/list";  // 목록 페이지로 리다이렉트
    }
}
