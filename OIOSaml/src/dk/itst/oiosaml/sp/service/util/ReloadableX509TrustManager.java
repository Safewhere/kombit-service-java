/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.itst.oiosaml.sp.service.util;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author LXP <lxp@globeteam.com>
 */
public class ReloadableX509TrustManager
        implements X509TrustManager {

    private final String trustStorePath;
    private X509TrustManager trustManager;
    private final List<X509Certificate> tempCertList;

    public ReloadableX509TrustManager(String tspath)
            throws Exception {
        this.tempCertList = new ArrayList() {};
        this.trustStorePath = tspath;
        reloadTrustManager();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain,
            String authType) throws CertificateException {
        trustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain,
            String authType) throws CertificateException {
        try {
            trustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException cx) {
            addServerCertAndReload(chain[0], true);
            trustManager.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] issuers
                = trustManager.getAcceptedIssuers();
        return issuers;
    }

    private void reloadTrustManager() throws Exception {

        // load keystore from specified cert store (or default)
        KeyStore ts = KeyStore.getInstance(
                KeyStore.getDefaultType());
        InputStream in = new FileInputStream(trustStorePath);
        try {
            ts.load(in, null);
        } finally {
            in.close();
        }
        // add all temporary certs to KeyStore (ts)
        for (X509Certificate cert : tempCertList) {
            ts.setCertificateEntry(UUID.randomUUID().toString(), cert);
        }
        // initialize a new TMF with the ts we just loaded
        TrustManagerFactory tmf
                = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        // acquire X509 trust manager from factory
        TrustManager tms[] = tmf.getTrustManagers();
        for (TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                trustManager = (X509TrustManager) tm;
                return;
            }
        }

        throw new NoSuchAlgorithmException(
                "No X509TrustManager in TrustManagerFactory");
    }

    private void addServerCertAndReload(X509Certificate cert,
            boolean permanent) {
        try {
            if (permanent) {
        // import the cert into file trust store
                // Google "java keytool source" or just ...
                Runtime.getRuntime().exec("keytool -importcert ...");
            } else {
                tempCertList.add(cert);
            }
            reloadTrustManager();
        } catch (Exception ex) { /* ... */ }
    }
}
