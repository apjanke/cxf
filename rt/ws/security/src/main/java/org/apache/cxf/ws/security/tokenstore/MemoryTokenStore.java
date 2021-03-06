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

package org.apache.cxf.ws.security.tokenstore;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.cxf.common.util.StringUtils;

/**
 * A simple HashMap-based TokenStore. The default TTL is 5 minutes and the max TTL is 1 hour.
 */
public class MemoryTokenStore implements TokenStore {
    public static final long DEFAULT_TTL = 60L * 5L;
    public static final long MAX_TTL = DEFAULT_TTL * 12L;
    
    private Map<String, CacheEntry> tokens = new ConcurrentHashMap<String, CacheEntry>();
    private long ttl = DEFAULT_TTL;
    
    public void add(SecurityToken token) {
        if (token != null && !StringUtils.isEmpty(token.getId())) {
            CacheEntry cacheEntry = createCacheEntry(token);
            if (cacheEntry != null) {
                tokens.put(token.getId(), cacheEntry);
            }
        }
    }
    
    public void add(String identifier, SecurityToken token) {
        if (token != null && !StringUtils.isEmpty(identifier)) {
            CacheEntry cacheEntry = createCacheEntry(token);
            if (cacheEntry != null) {
                tokens.put(identifier, cacheEntry);
            }
        }
    }
    
    /**
     * Set a new (default) TTL value in seconds
     * @param newTtl a new (default) TTL value in seconds
     */
    public void setTTL(long newTtl) {
        ttl = newTtl;
    }
    
    public void remove(String identifier) {
        if (!StringUtils.isEmpty(identifier) && tokens.containsKey(identifier)) {
            tokens.remove(identifier);
        }
    }

    public Collection<String> getTokenIdentifiers() {
        processTokenExpiry();
        return tokens.keySet();
    }
    
    public SecurityToken getToken(String id) {
        processTokenExpiry();
        
        CacheEntry cacheEntry = tokens.get(id);
        if (cacheEntry != null) {
            return cacheEntry.getSecurityToken();
        }
        return null;
    }
    
    protected void processTokenExpiry() {
        Date current = new Date();
        synchronized (tokens) {
            for (String id : tokens.keySet()) {
                CacheEntry cacheEntry = tokens.get(id);
                if (cacheEntry.getExpiry().before(current)) {
                    tokens.remove(id);
                }
            }
        }
    }
    
    private CacheEntry createCacheEntry(SecurityToken token) {
        Date expires = new Date();
        long currentTime = expires.getTime();
        expires.setTime(currentTime + (ttl * 1000L));
        return new CacheEntry(token, expires);
    }
    
    private static class CacheEntry {
        
        private final SecurityToken securityToken;
        private final Date expires;
        
        public CacheEntry(SecurityToken securityToken, Date expires) {
            this.securityToken = securityToken;
            this.expires = expires;
        }
        
        /**
         * Get the SecurityToken
         * @return the SecurityToken
         */
        public SecurityToken getSecurityToken() {
            return securityToken;
        }
        
        /**
         * Get when this CacheEntry is to be removed from the cache
         * @return when this CacheEntry is to be removed from the cache
         */
        public Date getExpiry() {
            return expires;
        }
        
    }
 
}
