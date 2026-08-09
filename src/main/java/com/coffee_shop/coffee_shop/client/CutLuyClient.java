package com.coffee_shop.coffee_shop.client;

import com.coffee_shop.coffee_shop.dto.request.CreateCutLuyPaymentRequest;
import com.coffee_shop.coffee_shop.dto.response.CutLuyPaymentResponse;
import com.coffee_shop.coffee_shop.exception.CutLuyApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Component
public class CutLuyClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public CutLuyClient(RestTemplate restTemplate,
                        @Value("${cutluy.base-url}") String baseUrl,
                        @Value("${cutluy.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    /**
     * Creates a KHQR payment with CutLuy.
     * The idempotency key is derived from referenceId (not randomly generated per call),
     * so retries of the same logical payment reuse the same key instead of creating duplicates.
     */

    //This method is the actual part that sends the payment request from your Spring Boot backend to CutLuy.
    public CutLuyPaymentResponse createPayment(BigDecimal amount, String referenceId) {
        HttpHeaders headers = buildHeaders();
        //Important: the key should be unique for each payment operation,
        // but if you retry the same payment, you should reuse the same key rather than generating a new one.
        headers.set("Idempotency-Key", referenceId);

        //Headers
        //+
        //JSON Body = request
        HttpEntity<CreateCutLuyPaymentRequest> request =
                new HttpEntity<>(new CreateCutLuyPaymentRequest(amount, referenceId), headers);

        try {
            CutLuyPaymentResponse response = restTemplate.postForObject(
                    baseUrl + "/payments",
                    request,
                    CutLuyPaymentResponse.class
            );
            log.info("Created CutLuy payment for referenceId={}, cutluyPaymentId={}",
                    referenceId, response != null ? response.id() : null);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("CutLuy createPayment failed for referenceId={}: {} - {}",
                    referenceId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new CutLuyApiException("Failed to create CutLuy payment: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            log.error("CutLuy createPayment network error for referenceId={}", referenceId, e);
            throw new CutLuyApiException("Could not reach CutLuy API", e);
        }
    }

    public CutLuyPaymentResponse getPayment(String cutluyPaymentId) {
        HttpHeaders headers = buildHeaders();
        //Notice that there is no body here
        //That's because you're doing a GET request.
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    baseUrl + "/payments/{id}",
                    HttpMethod.GET,
                    request,
                    CutLuyPaymentResponse.class,
                    cutluyPaymentId
            ).getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("CutLuy getPayment failed for id={}: {} - {}",
                    cutluyPaymentId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new CutLuyApiException("Failed to fetch CutLuy payment: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            log.error("CutLuy getPayment network error for id={}", cutluyPaymentId, e);
            throw new CutLuyApiException("Could not reach CutLuy API", e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }
}