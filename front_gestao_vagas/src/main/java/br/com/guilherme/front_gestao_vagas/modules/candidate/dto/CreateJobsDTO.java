package br.com.guilherme.front_gestao_vagas.modules.candidate.dto;

import lombok.Data;

@Data
public class CreateJobsDTO {
    private String description;
    private String level;
    private String benefits;
}
