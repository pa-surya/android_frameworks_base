/*
 * Copyright (C) 2023 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.internal.util;

import android.app.Application;
import android.util.Log;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

class ProxyX509Certificate extends X509Certificate {

    private static final String TAG = "ProxyX509Certificate";
    private static final boolean DEBUG = PropImitationHooks.DEBUG;

    private X509Certificate mOrigCert;

    ProxyX509Certificate(X509Certificate origCert) {
        mOrigCert = origCert;
        dlog(origCert.getSubjectX500Principal().getName());
    }

    @Override
    public void verify(PublicKey key) {
        dlog("bypass verify");
    }

    @Override
    public void verify(PublicKey key, String sigProvider) {
        dlog("bypass verify");
    }

    @Override
    public void verify(PublicKey key, Provider sigProvider) {
        dlog("bypass verify");
    }

    @Override
    public void checkValidity() throws CertificateExpiredException,
            CertificateNotYetValidException {
        mOrigCert.checkValidity();
    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException,
            CertificateNotYetValidException {
        mOrigCert.checkValidity(date);
    }

    @Override
    public int getBasicConstraints() {
        return mOrigCert.getBasicConstraints();
    }

    @Override
    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
        return mOrigCert.getExtendedKeyUsage();
    }

    @Override
    public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        return mOrigCert.getIssuerAlternativeNames();
    }

    @Override
    public Principal getIssuerDN() {
        return mOrigCert.getIssuerDN();
    }

    @Override
    public boolean[] getIssuerUniqueID() {
        return mOrigCert.getIssuerUniqueID();
    }

    @Override
    public X500Principal getIssuerX500Principal() {
        return mOrigCert.getIssuerX500Principal();
    }

    @Override
    public boolean[] getKeyUsage() {
        return mOrigCert.getKeyUsage();
    }

    @Override
    public Date getNotAfter() {
        return mOrigCert.getNotAfter();
    }

    @Override
    public Date getNotBefore() {
        return mOrigCert.getNotBefore();
    }

    @Override
    public BigInteger getSerialNumber() {
        return mOrigCert.getSerialNumber();
    }

    @Override
    public String getSigAlgName() {
        return mOrigCert.getSigAlgName();
    }

    @Override
    public String getSigAlgOID() {
        return mOrigCert.getSigAlgOID();
    }

    @Override
    public byte[] getSigAlgParams() {
        return mOrigCert.getSigAlgParams();
    }

    @Override
    public byte[] getSignature() {
        return mOrigCert.getSignature();
    }

    @Override
    public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        return mOrigCert.getSubjectAlternativeNames();
    }

    @Override
    public Principal getSubjectDN() {
        return mOrigCert.getSubjectDN();
    }

    @Override
    public boolean[] getSubjectUniqueID() {
        return mOrigCert.getSubjectUniqueID();
    }

    @Override
    public X500Principal getSubjectX500Principal() {
        return mOrigCert.getSubjectX500Principal();
    }

    @Override
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return mOrigCert.getTBSCertificate();
    }

    @Override
    public int getVersion() {
        return mOrigCert.getVersion();
    }

    @Override
    public boolean equals(Object other) {
        return mOrigCert.equals(other);
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return mOrigCert.getEncoded();
    }

    @Override
    public PublicKey getPublicKey() {
        return mOrigCert.getPublicKey();
    }

    @Override
    public int hashCode() {
        return mOrigCert.hashCode();
    }

    @Override
    public String toString() {
        return mOrigCert.toString();
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
        return mOrigCert.getCriticalExtensionOIDs();
    }

    @Override
    public byte[] getExtensionValue(String oid) {
        return mOrigCert.getExtensionValue(oid);
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        return mOrigCert.getNonCriticalExtensionOIDs();
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        return mOrigCert.hasUnsupportedCriticalExtension();
    }

    private static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, "[" + Application.getProcessName() + "] " + msg);
    }

}
