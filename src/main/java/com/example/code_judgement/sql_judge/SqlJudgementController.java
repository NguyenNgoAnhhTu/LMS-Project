package com.example.code_judgement.sql_judge;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/judgement/sql_judge")
@RequiredArgsConstructor
public class SqlJudgementController {
    private final SqlJudgementService sqlJudgementService;

}
