package org.moin.moneytransfer.service;

import org.moin.moneytransfer.dto.*;
import org.moin.moneytransfer.entity.Transfer;
import org.moin.moneytransfer.entity.TransferQuote;
import org.moin.moneytransfer.entity.User;
import org.moin.moneytransfer.repository.TransferQuoteRepository;
import org.moin.moneytransfer.repository.TransferRepository;
import org.moin.moneytransfer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransferService {
    private final TransferQuoteRepository transferQuoteRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public TransferService(
            TransferQuoteRepository transferQuoteRepository,
            TransferRepository transferRepository,
            UserRepository userRepository,
            RestTemplate restTemplate
    ) {
        this.transferQuoteRepository = transferQuoteRepository;
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    // **추가된 getTransferQuote 메서드**
    public TransferQuoteResponse getTransferQuote(TransferQuoteRequest request) {
        // 1. 금액 유효성 검사
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("NEGATIVE_NUMBER");
        }

        // 2. 수수료 계산
        long fee = calculateFee(request.getAmount(), request.getTargetCurrency());

        // 3. 환율 정보 가져오기
        double exchangeRate = getExchangeRate(request.getTargetCurrency());

        // 4. 받는 금액 계산
        double receiveAmount = calculateReceiveAmount(request.getAmount(), fee, exchangeRate, request.getTargetCurrency());

        if (receiveAmount <= 0) {
            throw new IllegalArgumentException("NEGATIVE_NUMBER");
        }

        // 5. 견적서 만료 시간 설정
        ZonedDateTime expiredAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(10);

        // 6. 견적서 ID 생성
        String quoteId = UUID.randomUUID().toString();

        // 7. 견적서 정보 저장
        TransferQuote quote = new TransferQuote();
        quote.setQuoteId(quoteId);
        quote.setAmount(request.getAmount());
        quote.setFee(fee);
        quote.setExchangeRate(exchangeRate);
        quote.setReceiveAmount(receiveAmount);
        quote.setExpiredAt(expiredAt);
        quote.setTargetCurrency(request.getTargetCurrency());
        transferQuoteRepository.save(quote);

        // 8. 응답 생성
        TransferQuoteResponse response = new TransferQuoteResponse();
        response.setQuoteId(quoteId);
        response.setAmount(request.getAmount());
        response.setFee(fee);
        response.setExchangeRate(exchangeRate);
        response.setReceiveAmount(receiveAmount);
        response.setExpiredAt(expiredAt);

        return response;
    }

    public TransferResponse requestTransfer(TransferRequest request) {
        // 1. 견적서 조회 및 유효성 검사
        TransferQuote quote = transferQuoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> new IllegalStateException("QUOTE_EXPIRED"));

        if (quote.getExpiredAt().isBefore(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))) {
            throw new IllegalStateException("QUOTE_EXPIRED");
        }

        // 2. 회원 정보 조회
        String userId = getCurrentUserId();
        User user = userRepository.findByUserId((userId))
                .orElseThrow(() -> new IllegalStateException("UNKNOWN_ERROR"));

        // 3. 송금 한도 체크
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime startOfDay = today.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.of("Asia/Seoul"));

        List<Transfer> transfersToday = transferRepository.findAllByUserIdAndTransferredAtBetween(
                userId, startOfDay, endOfDay);

        int totalAmountToday = transfersToday.stream()
                .mapToInt(Transfer::getAmount)
                .sum();

        int newTotalAmount = totalAmountToday + quote.getAmount();

        int limit = getDailyLimit(user);

        if (newTotalAmount > limit) {
            throw new IllegalStateException("LIMIT_EXCESS");
        }

        // 4. 송금 요청 저장
        String transferId = UUID.randomUUID().toString();
        ZonedDateTime transferredAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        Transfer transfer = new Transfer();
        transfer.setTransferId(transferId);
        transfer.setQuoteId(quote.getQuoteId());
        transfer.setUserId(userId);
        transfer.setAmount(quote.getAmount());
        transfer.setTargetCurrency(quote.getTargetCurrency());
        transfer.setReceiveAmount(quote.getReceiveAmount());
        transfer.setFee(quote.getFee());
        transfer.setExchangeRate(quote.getExchangeRate());
        transfer.setTransferredAt(transferredAt);

        transferRepository.save(transfer);

        // 5. 응답 생성
        TransferResponse response = new TransferResponse();
        response.setTransferId(transferId);
        response.setQuoteId(quote.getQuoteId());
        response.setAmount(quote.getAmount());
        response.setTargetCurrency(quote.getTargetCurrency());
        response.setReceiveAmount(quote.getReceiveAmount());
        response.setFee(quote.getFee());
        response.setExchangeRate(quote.getExchangeRate());
        response.setTransferredAt(transferredAt);

        return response;
    }

    private String getCurrentUserId() {
        // 실제 구현에서는 SecurityContextHolder에서 인증된 사용자 ID를 가져옵니다.
        // 여기서는 예시로 "testUser"를 반환합니다.
        return "testUser";
    }

    private int getDailyLimit(User user) {
        if ("PERSONAL".equals(user.getUserType())) {
            return 1_000_000; // $1000에 해당하는 원화 금액으로 설정
        } else if ("BUSINESS".equals(user.getUserType())) {
            return 5_000_000; // $5000에 해당하는 원화 금액으로 설정
        } else {
            throw new IllegalStateException("UNKNOWN_ERROR");
        }
    }

    private long calculateFee(int amount, String targetCurrency) {
        long fixedFee;
        double feeRate;

        if ("USD".equals(targetCurrency)) {
            if (amount <= 1_000_000) {
                fixedFee = 1000;
                feeRate = 0.002;
            } else {
                fixedFee = 3000;
                feeRate = 0.001;
            }
        } else if ("JPY".equals(targetCurrency)) {
            fixedFee = 3000;
            feeRate = 0.005;
        } else {
            throw new IllegalArgumentException("지원하지 않는 통화입니다.");
        }

        long fee = Math.round(amount * feeRate) + fixedFee;
        return fee;
    }

    private double getExchangeRate(String targetCurrency) {
        String apiUrl = "https://crix-api-cdn.upbit.com/v1/forex/recent?codes=FRX.KRW" + targetCurrency;
        ExchangeRateResponse[] response = restTemplate.getForObject(apiUrl, ExchangeRateResponse[].class);

        if (response != null && response.length > 0) {
            double basePrice = response[0].getBasePrice();
            double currencyUnit = response[0].getCurrencyUnit();
            return basePrice / currencyUnit;
        } else {
            throw new IllegalArgumentException("환율 정보를 가져올 수 없습니다.");
        }
    }

    private double calculateReceiveAmount(int amount, long fee, double exchangeRate, String targetCurrency) {
        double sendAmountAfterFee = amount - fee;
        double receiveAmount = sendAmountAfterFee / exchangeRate;

        // 소수점 자리수 처리
        Currency currency = Currency.getInstance(targetCurrency);
        int fractionDigits = currency.getDefaultFractionDigits();

        BigDecimal bd = new BigDecimal(receiveAmount);
        bd = bd.setScale(fractionDigits, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public List<TransferHistoryResponse> getTransferHistory(String userId) {
        List<Transfer> transfers = transferRepository.findAllByUserId(userId);

        // Transfer 엔티티를 TransferHistoryResponse DTO로 변환
        List<TransferHistoryResponse> history = transfers.stream()
                .map(transfer -> {
                    TransferHistoryResponse response = new TransferHistoryResponse();
                    response.setTransferId(transfer.getTransferId());
                    response.setAmount(transfer.getAmount());
                    response.setTargetCurrency(transfer.getTargetCurrency());
                    response.setReceiveAmount(transfer.getReceiveAmount());
                    response.setFee(transfer.getFee());
                    response.setExchangeRate(transfer.getExchangeRate());
                    response.setTransferredAt(transfer.getTransferredAt());
                    // 필요한 필드 추가
                    return response;
                })
                .collect(Collectors.toList());

        return history;
    }

}
