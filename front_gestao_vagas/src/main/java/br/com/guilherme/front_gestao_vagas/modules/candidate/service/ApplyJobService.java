package br.com.guilherme.front_gestao_vagas.modules.candidate.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApplyJobService {

    public String execute(String token, UUID idJob) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, String> data = new HashMap<>();
        data.put("idJob", idJob.toString());

        HttpEntity<UUID> request = new HttpEntity<>(idJob, headers);

        var result = rt.postForObject("http://localhost:8080/candidate/job/apply", request, String.class);

        return result;
    }
}
