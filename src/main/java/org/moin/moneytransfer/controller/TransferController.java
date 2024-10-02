package org.moin.moneytransfer.controller;

import org.moin.moneytransfer.dto.ApiResponse;
import org.moin.moneytransfer.dto.TransferHistoryResponse;
import org.moin.moneytransfer.dto.TransferRequest;
import org.moin.moneytransfer.dto.TransferResponse;
import org.moin.moneytransfer.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    @Autowired
    private TransferService transferService;

    // 거래 이력 조회 API
    @GetMapping("/list")
    public ResponseEntity<?> getTransferHistory(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<TransferHistoryResponse> history = transferService.getTransferHistory(userId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "거래 이력을 가져왔습니다.", history));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>("FAIL", "UNKNOWN_ERROR", null));
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestTransfer(@RequestBody TransferRequest request) {
        try {
            TransferResponse response = transferService.requestTransfer(request);
            return ResponseEntity.ok().body(
                    Map.of(
                            "status", "SUCCESS",
                            "message", "송금이 접수되었습니다.",
                            "data", response
                    )
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", "FAIL",
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of(
                            "status", "FAIL",
                            "message", "UNKNOWN_ERROR"
                    )
            );
        }
    }
}
