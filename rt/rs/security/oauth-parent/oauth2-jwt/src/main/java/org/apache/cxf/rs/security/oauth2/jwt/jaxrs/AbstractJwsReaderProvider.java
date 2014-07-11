/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.rs.security.oauth2.jwt.jaxrs;

import java.security.PublicKey;

import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.rs.security.oauth2.jws.JwsSignatureProperties;
import org.apache.cxf.rs.security.oauth2.jws.JwsSignatureVerifier;
import org.apache.cxf.rs.security.oauth2.jws.PublicKeyJwsSignatureVerifier;
import org.apache.cxf.rs.security.oauth2.utils.crypto.CryptoUtils;

public class AbstractJwsReaderProvider {
    private static final String RSSEC_SIGNATURE_PROPS = "rs.security.signature.in.properties";
    
    private JwsSignatureVerifier sigVerifier;
    private JwsSignatureProperties sigProperties;
    private String defaultMediaType;
    
    public void setSignatureVerifier(JwsSignatureVerifier signatureVerifier) {
        this.sigVerifier = signatureVerifier;
    }

    public void setSignatureProperties(JwsSignatureProperties signatureProperties) {
        this.sigProperties = signatureProperties;
    }
    
    public JwsSignatureProperties getSigProperties() {
        return sigProperties;
    }
    
    protected JwsSignatureVerifier getInitializedSigVerifier() {
        if (sigVerifier != null) {
            return sigVerifier;    
        } 
        try {
            PublicKey pk = CryptoUtils.loadPublicKey(JAXRSUtils.getCurrentMessage(), 
                                                     RSSEC_SIGNATURE_PROPS);
            return new PublicKeyJwsSignatureVerifier(pk);
        } catch (SecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SecurityException(ex);
        }
    }

    public String getDefaultMediaType() {
        return defaultMediaType;
    }

    public void setDefaultMediaType(String defaultMediaType) {
        this.defaultMediaType = defaultMediaType;
    }
    
    
}
