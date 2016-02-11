package com.ericsson;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

class DefaultTrustManager implements X509TrustManager{
    @Override
	public
    void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
	public
    void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
	public
    X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
