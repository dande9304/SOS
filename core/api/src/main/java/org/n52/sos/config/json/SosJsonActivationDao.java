/*
 * Copyright (C) 2012-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sos.config.json;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.n52.iceland.coding.encode.ResponseFormatKey;
import org.n52.iceland.config.json.AbstractJsonActivationDao;
import org.n52.shetland.ogc.ows.service.OwsServiceKey;
import org.n52.sos.coding.encode.ProcedureDescriptionFormatKey;
import org.n52.sos.config.SosActivationDao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class SosJsonActivationDao extends AbstractJsonActivationDao
        implements SosActivationDao {
    protected static final String RESPONSE_FORMATS = "responseFormats";
    protected static final String PROCEDURE_DESCRIPTION_FORMATS
            = "procedureDescriptionFormats";
    protected static final String FORMAT = "format";
    private static final Function<ProcedureDescriptionFormatKey, OwsServiceKey> PDF_SOK
            = ProcedureDescriptionFormatKey::getServiceOperatorKey;
    private static final Function<ProcedureDescriptionFormatKey, String> PDF_FORMAT
            = ProcedureDescriptionFormatKey::getProcedureDescriptionFormat;
    private static final Function<ResponseFormatKey, OwsServiceKey> RF_SOK
            = ResponseFormatKey::getServiceOperatorKey;
    private static final Function<ResponseFormatKey, String> RF_FORMAT
            = ResponseFormatKey::getResponseFormat;

    @Override
    public boolean isResponseFormatActive(ResponseFormatKey key) {
        return isActive(RESPONSE_FORMATS, matches(key, RF_SOK, RF_FORMAT), true);
    }

    @Override
    public void setResponseFormatStatus(ResponseFormatKey key, boolean active) {
        setStatus(RESPONSE_FORMATS, matches(key, RF_SOK, RF_FORMAT),
                  s -> createFormatEncoder(s, key, RF_SOK, RF_FORMAT), active);
    }

    @Override
    public Set<ResponseFormatKey> getResponseFormatKeys() {
        return getKeys(RESPONSE_FORMATS, createFormatDecoder(ResponseFormatKey::new));
    }

    @Override
    public boolean isProcedureDescriptionFormatActive(ProcedureDescriptionFormatKey key) {
        return isActive(PROCEDURE_DESCRIPTION_FORMATS, matches(key, PDF_SOK, PDF_FORMAT), true);
    }

    @Override
    public void setProcedureDescriptionFormatStatus(ProcedureDescriptionFormatKey key, boolean active) {
        setStatus(PROCEDURE_DESCRIPTION_FORMATS, matches(key, PDF_SOK, PDF_FORMAT),
                  s -> createFormatEncoder(s, key, PDF_SOK, PDF_FORMAT), active);
    }

    @Override
    public Set<ProcedureDescriptionFormatKey> getProcedureDescriptionFormatKeys() {
        return getKeys(PROCEDURE_DESCRIPTION_FORMATS, createFormatDecoder(ProcedureDescriptionFormatKey::new));
    }

    protected <K> Function<JsonNode, K> createFormatDecoder(
            BiFunction<OwsServiceKey, String, K> fun) {
        Objects.requireNonNull(fun);
        return n -> fun.apply(decodeServiceOperatorKey(n),
                              n.path(FORMAT).textValue());
    }

    protected <K> Supplier<ObjectNode> createFormatEncoder(
            Supplier<ObjectNode> supplier, K key,
            Function<K, OwsServiceKey> sokFun,
            Function<K, String> formatFun) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(sokFun);
        Objects.requireNonNull(formatFun);
        return () -> {
            OwsServiceKey sok = key == null ? null : sokFun.apply(key);
            String format = key == null ? null : formatFun.apply(key);
            return encode(supplier, sok).get().put(FORMAT, format);
        };
    }

    protected <K> Predicate<JsonNode> matches(K key,
                                              Function<K, OwsServiceKey> sokFun,
                                              Function<K, String> formatFun) {
        OwsServiceKey sok = key == null ? null : sokFun.apply(key);
        String responseFormat = key == null ? null : formatFun.apply(key);
        return matches(sok).and(matchesFormat(responseFormat));
    }

    protected Predicate<JsonNode> matchesFormat(String responseFormat) {
        if (responseFormat == null) {
            return isNullOrMissing(FORMAT);
        }
        return n -> n.path(FORMAT).asText().equals(responseFormat);
    }

}
