package com.mediwise.payment.service;

import com.razorpay.RazorpayException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adapter interface that wraps the Razorpay SDK's order creation.
 *
 * WHY THIS EXISTS:
 * The Razorpay SDK exposes sub-resources as public fields (client.orders),
 * not as injectable beans. You cannot @Mock a public field in unit tests.
 *
 * By introducing this interface, RazorpayService depends on an abstraction
 * (not the SDK directly), so unit tests can mock it cleanly.
 *
 * INTERVIEW POINT: This is the Adapter Pattern — wrapping a concrete
 * third-party dependency behind an interface you control.
 */
public interface RazorpayOrderGateway {

    /**
     * Creates an order on Razorpay's servers.
     *
     * @param appointmentId  used as the receipt reference
     * @param amountInRupees server-authoritative fee (e.g. 500.00)
     * @return the Razorpay order ID (e.g. "order_Mx2ABCDEF")
     * @throws RazorpayException if the API call fails
     */
    String createOrder(UUID appointmentId, BigDecimal amountInRupees) throws RazorpayException;
}
