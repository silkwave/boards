package com.nyc.boards.controller;

import com.nyc.boards.dto.BoardDTO;
import com.nyc.boards.dto.BoardFileDTO;
import com.nyc.boards.service.BoardService;
import com.nyc.boards.util.GuidQueueUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;

    // favicon.ico 요청을 처리하지 않도록 처리
    @RequestMapping("favicon.ico")
    public void returnNoFavicon() {
        log.info("returnNoFavicon {}");
    }    

    @GetMapping("/save")
    public String save() {
        return "save";
    }

    @PostMapping("/save")
    public String save(@Validated BoardDTO boardDTO) throws IOException {
        boardService.save(boardDTO);
        log.info("@PostMapping(\"/save\") : {}", boardDTO.getBoardTitle());
        return "redirect:/list";
    }

    @GetMapping("/list")
    public String findAll(Model model) throws InterruptedException {

        String GUID = GuidQueueUtil.processGUIDs(); // 3개의 GUID를 생성하여 출력

        System.out.println(Thread.currentThread().getName() + " GUID: " + GUID); // 현재 스레드 이름과 함께 생성된 GUID 출력        

        List<BoardDTO> boardDTOList = boardService.findAll();
        log.info(" @GetMapping(\"/list\") : {}", boardDTOList);

        model.addAttribute("boardList", boardDTOList);
        return "list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") String idString, Model model) {
        try {
            log.info(" @GetMapping(\"/{id}\") : {}", idString);
            Long id = Long.valueOf(idString);
            BoardDTO boardDTO = boardService.findById(id);

            if (boardDTO == null) {
                log.error("Board with id {} not found", id);
                return "redirect:/list";
            }

            boardService.updateHits(id);
            model.addAttribute("board", boardDTO);

            if (boardDTO.getFileAttached() == 1) {
                List<BoardFileDTO> boardFileDTOList = boardService.findFile(id);
                model.addAttribute("boardFileList", boardFileDTOList);
            }

            return "detail";
        } catch (NumberFormatException e) {
            log.error("Invalid board ID: {}", idString);
            return "redirect:/list";
        }
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, Model model) {
        log.info(" @GetMapping(\"/update/{id}\") : {}", id);
        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        return "update";
    }

    @PostMapping("/update/{id}")
    public String update(BoardDTO boardDTO) {
        log.info(" @PostMapping(\"/update/{id}\") : {}", boardDTO);
        boardService.update(boardDTO);
        return "redirect:/" + boardDTO.getId();
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        log.info(" @GetMapping(\"/delete/{id}\") : {}", id);
        boardService.delete(id);
        return "redirect:/list";
    }
}
