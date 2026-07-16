package com.rancho.api.pix;

/**
 * Metodo de pagamento online oferecido ao cliente. Cada metodo tem uma taxa
 * (repassada ao cliente) e um billingType correspondente no Asaas.
 */
public enum MetodoCobranca {
    PIX("PIX"),
    CARTAO("CREDIT_CARD");

    private final String asaasBillingType;

    MetodoCobranca(String asaasBillingType) {
        this.asaasBillingType = asaasBillingType;
    }

    public String asaasBillingType() {
        return asaasBillingType;
    }
}
