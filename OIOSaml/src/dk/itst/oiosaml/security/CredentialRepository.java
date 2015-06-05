/*
 * The contents of this file are subject to the Mozilla Public 
 * License Version 1.1 (the "License"); you may not use this 
 * file except in compliance with the License. You may obtain 
 * a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express 
 * or implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 *
 * The Original Code is OIOSAML Java Service Provider.
 * 
 * The Initial Developer of the Original Code is Trifork A/S. Portions 
 * created by Trifork A/S are Copyright (C) 2008 Danish National IT 
 * and Telecom Agency (http://www.itst.dk). All Rights Reserved.
 * 
 * Contributor(s):
 *   Joakim Recht <jre@trifork.com>
 *   Rolf Njor Jensen <rolf@trifork.com>
 *   Aage Nielsen <ani@openminds.dk>
 *
 */
package dk.itst.oiosaml.security;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;

import dk.itst.oiosaml.error.Layer;
import dk.itst.oiosaml.error.WrappedException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Class for managing credentials.
 *
 * Credentials can be loaded from a stream. When loaded, credentials are cached,
 * so they are only loaded once.
 *
 * This class is thread-safe, and can be shared across threads.
 *
 * @author recht
 * @author Aage Nielsen <ani@openminds.dk>
 *
 */
public class CredentialRepository {

    private static final Logger log = Logger.getLogger(CredentialRepository.class);

    private final Map<Key, BasicX509Credential> credentials = new ConcurrentHashMap<Key, BasicX509Credential>();

    public BasicX509Credential getCredential(String fileName, String password, String alias) throws FileNotFoundException, KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        char[] passwordC = password.toCharArray();
        FileInputStream fIn = new FileInputStream(fileName);
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(fIn, passwordC);
        return createCredential(keystore, password, alias);
    }

    /**
     * Load credentials from a keystore
     *
     * The first private key is loaded from the keystore.
     *
     * @param keystore
     * @param password Keystore and private key password.
     * @param credentialsCacheKey if more keystores are available this parameter
     * is a must. Apply different keys for each keystore.
     * @return 
     *
     */
    public BasicX509Credential getCredential(KeyStore keystore, String password, String credentialsCacheKey) {
        Key key = new Key(credentialsCacheKey, password);
        BasicX509Credential credential = credentials.get(key);
        if (credential == null) {
            credential = createCredential(keystore, password);
            credentials.put(key, credential);
        }

        return credential;
    }

    public Collection<BasicX509Credential> getCredentials() {
        return credentials.values();
    }

    /**
     * Get a x509certificate from a keystore.
     *
     * @param fileName
     * @param password Password for the keystore.
     * @param alias Alias to retrieve. If <code>null</code>, the first
     * certificate in the keystore is retrieved.
     * @param credentialsCacheKey if more keystores are available this parameter
     * is a must. Apply different keys for each keystore.
     * @return The certificate.
     * @throws java.io.FileNotFoundException
     * @throws java.security.KeyStoreException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.cert.CertificateException
     */
    public X509Certificate getCertificate(String fileName, String password, String alias, String credentialsCacheKey) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        char[] passwordC = password.toCharArray();
        FileInputStream fIn = new FileInputStream(fileName);
        KeyStore keystore = KeyStore.getInstance("JKS");

        keystore.load(fIn, passwordC);
        return getCertificate(keystore, password, alias, credentialsCacheKey);
    }
    /**
     * Look up certificate in keystore by alias, keystore's password must be provided
     * @param keystorePath
     * @param password
     * @param alias
     * @return
     * @throws FileNotFoundException
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException 
     */
    public X509Certificate getCertificate(String keystorePath, String password, String alias) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        char[] passwordC = password.toCharArray();
        FileInputStream fIn = new FileInputStream(keystorePath);
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(fIn, passwordC);
        X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);

        return cert;
    }
/**
 * Get a x509certificate from a keystore.
 *
 * @param keystore
 * @param password Password for the keystore.
 * @param alias Alias to retrieve. If <code>null</code>, the first certificate
 * in the keystore is retrieved.
 * @param credentialsCacheKey if more keystores are available this parameter is
 * a must. Apply different keys for each keystore.
 * @return The certificate.
 */
public X509Certificate getCertificate(KeyStore keystore, String password, String alias, String credentialsCacheKey) {
        BasicX509Credential credential = null;
        if (credentialsCacheKey != null) {
            Key key = new Key(credentialsCacheKey, password, alias);
            credential = credentials.get(key);
        }
        if (credential == null) {
            try {
                Enumeration<String> eAliases = keystore.aliases();
                while (eAliases.hasMoreElements()) {
                    String strAlias = eAliases.nextElement();
                    if (strAlias == null ? alias != null : !strAlias.equals(alias)) {
                        continue;
                    }
                    log.debug("Trying " + strAlias);
                    if (keystore.isCertificateEntry(strAlias)) {
                        X509Certificate certificate = (X509Certificate) keystore.getCertificate(strAlias);
                        credential = new BasicX509Credential();
                        credential.setEntityCertificate(certificate);
                        if (credentialsCacheKey != null) {
                            credentials.put(new Key(credentialsCacheKey, password, strAlias), credential);
                        }
                        alias = strAlias;
                    }
                    
                }
                log.debug("Getting certificate from alias " + alias);
                if (credentialsCacheKey != null) {
                    credential = credentials.get(new Key(credentialsCacheKey, password, alias));
                }
                if (credential == null) {
                    throw new NullPointerException("Unable to find certificate for " + alias);
                }
            } catch (GeneralSecurityException e) {
                throw new WrappedException(Layer.CLIENT, e);
            }
        }
        return credential.getEntityCertificate();
    }

    /**
     * Read credentials from a inputstream.
     *
     * The stream can either point to a PKCS12 keystore or a JKS keystore. The
     * store is converted into a {@link Credential} including the private key.
     *
     * @param ks
     * @param password Password for the store. The same password is also used
     * for the certificate.
     *
     * @return The {@link Credential}
     */
    public static BasicX509Credential createCredential(KeyStore ks, String password) {
        BasicX509Credential credential = new BasicX509Credential();
        try {
            Enumeration<String> eAliases = ks.aliases();
            while (eAliases.hasMoreElements()) {
                String strAlias = eAliases.nextElement();

                if (ks.isKeyEntry(strAlias)) {
                    PrivateKey privateKey = (PrivateKey) ks.getKey(strAlias, password.toCharArray());
                    credential.setPrivateKey(privateKey);
                    credential.setEntityCertificate((X509Certificate) ks.getCertificate(strAlias));
                    PublicKey publicKey = ks.getCertificate(strAlias).getPublicKey();
                    if (log.isDebugEnabled()) {
                        log.debug("publicKey..:" + publicKey + ", privateKey: " + privateKey);
                    }
                    credential.setPublicKey(publicKey);
                }
            }
        } catch (GeneralSecurityException e) {
            throw new WrappedException(Layer.CLIENT, e);
        }

        return credential;
    }
    
    /**
     * Lookup credential in keystore by alias, keystore password must be provided
     * @param ks
     * @param password
     * @param alias
     * @return 
     */
    public static BasicX509Credential createCredential(KeyStore ks, String password, String alias) {
        BasicX509Credential credential = new BasicX509Credential();
        try {
            Enumeration<String> eAliases = ks.aliases();
            while (eAliases.hasMoreElements()) {
                String strAlias = eAliases.nextElement();
                if (strAlias == null ? alias != null : !strAlias.equals(alias)) {
                    continue;
                }
                if (ks.isKeyEntry(strAlias)) {
                    PrivateKey privateKey = (PrivateKey) ks.getKey(strAlias, password.toCharArray());
                    credential.setPrivateKey(privateKey);
                    credential.setEntityCertificate((X509Certificate) ks.getCertificate(strAlias));
                    PublicKey publicKey = ks.getCertificate(strAlias).getPublicKey();
                    if (log.isDebugEnabled()) {
                        log.debug("publicKey..:" + publicKey + ", privateKey: " + privateKey);
                    }
                    credential.setPublicKey(publicKey);
                }
            }
        } catch (GeneralSecurityException e) {
            throw new WrappedException(Layer.CLIENT, e);
        }

        return credential;
    

}

    private static class Key {

    private final String location;
    private final String password;
    private final String alias;

    public Key(String location, String password) {
        this.location = location;
        this.password = password;
        this.alias = null;
    }

    public Key(String location, String password, String alias) {
        this.location = location;
        this.password = password;
        this.alias = alias;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Key other = (Key) obj;
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }

        return true;
    }
}
}
