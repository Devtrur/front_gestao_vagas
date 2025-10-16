package br.com.guilherme.front_gestao_vagas.modules.company.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.com.guilherme.front_gestao_vagas.modules.company.dto.CreateCompanyDTO;

@Service
public class CreateCompanyService {
    public void execute(CreateCompanyDTO companyDTO) {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateCompanyDTO> request = new HttpEntity<>(companyDTO, headers);

        rt.postForObject("http://localhost:8080/company/", request, String.class);
    }
}
