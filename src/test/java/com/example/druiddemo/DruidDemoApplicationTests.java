package com.example.druiddemo;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DruidDemoApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(dataSource).isInstanceOf(DruidDataSource.class);
    }

    @Test
    void druidMonitorLoginPageIsAvailable() {
        ResponseEntity<String> response = restTemplate.getForEntity("/druid/login.html", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsIgnoringCase("druid monitor");
    }

    @Test
    void databaseHealthEndpointWorks() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/system/health", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("\"success\":true", "\"database\":\"UP\"");
    }

    @Test
    void userCrudWorkflowWorks() {
        String email = "resume-" + System.nanoTime() + "@example.com";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"name\":\"简历测试用户\",\"email\":\"" + email + "\",\"status\":\"ACTIVE\"}";

        ResponseEntity<String> created = restTemplate.postForEntity(
                "/api/users", new HttpEntity<>(body, headers), String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).contains(email, "\"success\":true");

        ResponseEntity<String> page = restTemplate.getForEntity(
                "/api/users?keyword=" + email + "&page=0&size=10", String.class);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains(email, "\"total\":1");

        String id = created.getBody().replaceFirst(".*\\\"id\\\":(\\d+).*", "$1");
        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/users/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void invalidUserReturnsBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users",
                new HttpEntity<>("{\"name\":\"\",\"email\":\"wrong\"}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"success\":false");
    }
}
