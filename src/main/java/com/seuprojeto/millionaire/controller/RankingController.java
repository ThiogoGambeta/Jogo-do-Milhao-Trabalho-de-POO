package com.seuprojeto.millionaire.controller;

import com.seuprojeto.millionaire.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;
    
    @GetMapping("/ranking")
    public String ranking(Model model) {
        model.addAttribute("topGames", rankingService.getTop10Ranking());
        return "ranking";
    }
}

