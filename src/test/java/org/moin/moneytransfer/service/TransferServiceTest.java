package org.moin.moneytransfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.moin.moneytransfer.dto.*;
import org.moin.moneytransfer.entity.Transfer;
import org.moin.moneytransfer.entity.TransferQuote;
import org.moin.moneytransfer.entity.User;
import org.moin.moneytransfer.repository.TransferQuoteRepository;
import org.moin.moneytransfer.repository.TransferRepository;
import org.moin.moneytransfer.repository.UserRepository;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class TransferServiceTest {

    private TransferService transferService;
    private TransferQuoteRepository transferQuoteRepository;
    private TransferRepository transferRepository;
    private UserRepository userRepository;
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        transferQuoteRepository = Mockito.mock(TransferQuoteRepository.class);
        transferRepository = Mockito.mock(TransferRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        restTemplate = Mockito.mock(RestTemplate.class);

        transferService = new TransferService(
                transferQuoteRepository,
                transferRepository,
                userRepository,
                restTemplate
        );
    }

    @Test
    public void testGetTransferQuoteSuccessUSD() {
        // Given
        TransferQuoteRequest request = new TransferQuoteRequest();
        request.setAmount(500000);
        request.setTargetCurrency("USD");

        ExchangeRateResponse[] exchangeRateResponses = new ExchangeRateResponse[1];
        ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse();
        exchangeRateResponse.setBasePrice(1300.00);
        exchangeRateResponse.setCurrencyUnit(1);
        exchangeRateResponses[0] = exchangeRateResponse;

        when(restTemplate.getForObject(anyString(), eq(ExchangeRateResponse[].class)))
                .thenReturn(exchangeRateResponses);

        // When
        TransferQuoteResponse response = transferService.getTransferQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(500000, response.getAmount());
        assertEquals(1000 + Math.round(500000 * 0.002), response.getFee());
        assertEquals(1300.00, response.getExchangeRate(), 0.0001);
        assertTrue(response.getReceiveAmount() > 0);
        assertTrue(response.getExpiredAt().isAfter(ZonedDateTime.now()));
    }

    @Test
    public void testRequestTransferSuccess() {
        // Given
        String quoteId = UUID.randomUUID().toString();
        TransferQuote quote = new TransferQuote();
        quote.setQuoteId(quoteId);
        quote.setAmount(500000);
        quote.setFee(2000);
        quote.setExchangeRate(1300.00);
        quote.setReceiveAmount(382.83);
        quote.setExpiredAt(ZonedDateTime.now().plusMinutes(10));
        quote.setTargetCurrency("USD"); // 여기에서 targetCurrency를 설정

        when(transferQuoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));

        User user = new User();
        user.setUserId("testUser");
        user.setUserType("PERSONAL");

        when(userRepository.findByUserId(user.getUserId())).thenReturn(Optional.of(user));

        when(transferRepository.findAllByUserIdAndTransferredAtBetween(
                eq("testUser"),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(java.util.Collections.emptyList());

        // When
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setQuoteId(quoteId);

        TransferResponse response = transferService.requestTransfer(transferRequest);

        // Then
        assertNotNull(response);
        assertEquals(quoteId, response.getQuoteId());
        assertEquals(500000, response.getAmount());
        assertEquals("USD", response.getTargetCurrency());
        assertEquals(382.83, response.getReceiveAmount(), 0.01);
        assertEquals(2000, response.getFee());
        assertEquals(1300.00, response.getExchangeRate(), 0.0001);
        assertNotNull(response.getTransferredAt());
    }

    @Test
    public void testRequestTransferQuoteExpired() {
        // Given
        String quoteId = UUID.randomUUID().toString();
        TransferQuote quote = new TransferQuote();
        quote.setQuoteId(quoteId);
        quote.setExpiredAt(ZonedDateTime.now().minusMinutes(1));

        when(transferQuoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));

        // When & Then
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setQuoteId(quoteId);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            transferService.requestTransfer(transferRequest);
        });

        assertEquals("QUOTE_EXPIRED", exception.getMessage());
    }

    @Test
    public void testRequestTransferLimitExcess() {
        // Given
        String quoteId = UUID.randomUUID().toString();
        TransferQuote quote = new TransferQuote();
        quote.setQuoteId(quoteId);
        quote.setAmount(1_000_001); // 한도 초과 금액
        quote.setExpiredAt(ZonedDateTime.now().plusMinutes(10));
        quote.setTargetCurrency("USD");

        when(transferQuoteRepository.findById(eq(quoteId))).thenReturn(Optional.of(quote));

        User user = new User();
        user.setUserId("testUser");
        user.setUserType("PERSONAL");

        when(userRepository.findByUserId((eq("testUser")))).thenReturn(Optional.of(user));

        // 당일 이미 송금된 금액을 설정
        Transfer existingTransfer = new Transfer();
        existingTransfer.setAmount(500000);

        when(transferRepository.findAllByUserIdAndTransferredAtBetween(
                eq("testUser"),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(java.util.Collections.singletonList(existingTransfer));

        // When & Then
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setQuoteId(quoteId);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            transferService.requestTransfer(transferRequest);
        });

        assertEquals("LIMIT_EXCESS", exception.getMessage());
    }

    @Test
    public void testGetTransferHistory() {
        // Given
        String userId = "testUser";

        Transfer transfer1 = new Transfer();
        transfer1.setTransferId("transferId1");
        transfer1.setUserId(userId);
        transfer1.setAmount(500000);
        transfer1.setTargetCurrency("USD");
        transfer1.setReceiveAmount(382.83);
        transfer1.setFee(2000);
        transfer1.setExchangeRate(1300.00);
        transfer1.setTransferredAt(ZonedDateTime.now().minusDays(1));

        Transfer transfer2 = new Transfer();
        transfer2.setTransferId("transferId2");
        transfer2.setUserId(userId);
        transfer2.setAmount(1000000);
        transfer2.setTargetCurrency("JPY");
        transfer2.setReceiveAmount(85000);
        transfer2.setFee(3000);
        transfer2.setExchangeRate(10.00);
        transfer2.setTransferredAt(ZonedDateTime.now().minusDays(2));

        List<Transfer> transfers = Arrays.asList(transfer1, transfer2);

        when(transferRepository.findAllByUserId(userId)).thenReturn(transfers);

        // When
        List<TransferHistoryResponse> history = transferService.getTransferHistory(userId);

        // Then
        assertNotNull(history);
        assertEquals(2, history.size());

        TransferHistoryResponse response1 = history.get(0);
        assertEquals("transferId1", response1.getTransferId());
        assertEquals(500000, response1.getAmount());
        assertEquals("USD", response1.getTargetCurrency());
        assertEquals(382.83, response1.getReceiveAmount());
        assertEquals(2000, response1.getFee());
        assertEquals(1300.00, response1.getExchangeRate());
        assertNotNull(response1.getTransferredAt());

        TransferHistoryResponse response2 = history.get(1);
        assertEquals("transferId2", response2.getTransferId());
        assertEquals(1000000, response2.getAmount());
        assertEquals("JPY", response2.getTargetCurrency());
        assertEquals(85000, response2.getReceiveAmount());
        assertEquals(3000, response2.getFee());
        assertEquals(10.00, response2.getExchangeRate());
        assertNotNull(response2.getTransferredAt());
    }
}
