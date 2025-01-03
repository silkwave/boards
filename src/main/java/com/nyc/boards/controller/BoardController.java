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
@Slf4j  // Add this for logging
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/save")
    public String save() {
        return "save";
    }

    @PostMapping("/save")
    public String save(@Validated BoardDTO boardDTO) throws IOException {

        boardService.save(boardDTO);
        log.info("Board saved with title: {}", boardDTO.getBoardTitle());
        return "redirect:/list";
    }

    @GetMapping("/list")
    public String findAll(Model model) {
        List<BoardDTO> boardDTOList = boardService.findAll();
        model.addAttribute("boardList", boardDTOList);
        return "list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Long id, Model model) {
        if (id == null || id <= 0) {
            log.error("Invalid board ID: {}", id);
            return "redirect:/list";  // Handle invalid ID
        }
    
        BoardDTO boardDTO = boardService.findById(id);
        if (boardDTO == null) {
            log.error("Board with id {} not found", id);
            return "redirect:/list";  // Handle board not found
        }
        boardService.updateHits(id);
        model.addAttribute("board", boardDTO);
        if (boardDTO.getFileAttached() == 1) {
            List<BoardFileDTO> boardFileDTOList = boardService.findFile(id);
            model.addAttribute("boardFileList", boardFileDTOList);
        }
        return "detail";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, Model model) {
        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        return "update";
    }

    @PostMapping("/update/{id}")
    public String update(BoardDTO boardDTO) {
        boardService.update(boardDTO);
        return "redirect:/" + boardDTO.getId();  // Redirect to the updated board's detail
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        boardService.delete(id);
        return "redirect:/list";
    }
}
