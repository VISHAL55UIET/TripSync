package com.TripSync.settlement.service;

import com.TripSync.settlement.dto.RazorpayOrderRequest;
import com.TripSync.settlement.dto.RazorpayOrderResponse;
import com.TripSync.settlement.dto.RazorpayVerifyRequest;
import com.TripSync.settlement.entity.Settlement;
import com.TripSync.settlement.repository.SettlementRepository;
import com.TripSync.trip.entity.Trip;
import com.TripSync.trip.repository.TripMemberRepository;
import com.TripSync.trip.repository.TripRepository;
import com.TripSync.user.entity.User;
import com.TripSync.user.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RazorpaySettlementService {

    private final SettlementRepository settlementRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public RazorpayOrderResponse createOrder(Long tripId, String payerEmail, RazorpayOrderRequest request) {
        User payer = getUser(payerEmail);
        User payee = validateMembers(tripId, payer.getId(), request.payeeId());

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            long amountInPaise = request.amount().multiply(BigDecimal.valueOf(100)).longValueExact();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "trip_" + tripId + "_" + System.currentTimeMillis());

            Order order = client.orders.create(orderRequest);

            return new RazorpayOrderResponse(
                    order.get("id"),
                    amountInPaise,
                    "INR",
                    razorpayKeyId,
                    payee.getId(),
                    payee.getFullName(),
                    request.amount(),
                    request.note()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create Razorpay order: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyAndRecordSettlement(Long tripId, String payerEmail, RazorpayVerifyRequest request) {
        User payer = getUser(payerEmail);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        User payee = validateMembers(tripId, payer.getId(), request.payeeId());

        if (settlementRepository.existsByRazorpayPaymentId(request.razorpayPaymentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This payment has already been recorded");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.razorpayOrderId());
            options.put("razorpay_payment_id", request.razorpayPaymentId());
            options.put("razorpay_signature", request.razorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            if (!isValid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Razorpay payment signature");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Signature verification failed");
        }

        Settlement settlement = new Settlement();
        settlement.setTrip(trip);
        settlement.setPayer(payer);
        settlement.setPayee(payee);
        settlement.setAmount(request.amount());
        String baseNote = (request.note() != null && !request.note().isBlank()) ? request.note().trim() : "Online Settlement";
        settlement.setNote(baseNote + " (Razorpay: " + request.razorpayPaymentId() + ")");
        settlement.setSettleAt(LocalDateTime.now());
        settlement.setRazorpayOrderId(request.razorpayOrderId());
        settlement.setRazorpayPaymentId(request.razorpayPaymentId());

        settlementRepository.save(settlement);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User validateMembers(Long tripId, Long payerId, Long payeeId) {
        if (payerId.equals(payeeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payer and payee cannot be the same user");
        }
        if (!tripMemberRepository.existsByTrip_IdAndUser_Id(tripId, payerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payer is not a member of this trip");
        }
        if (!tripMemberRepository.existsByTrip_IdAndUser_Id(tripId, payeeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payee is not a member of this trip");
        }
        return userRepository.findById(payeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payee not found"));
    }
}