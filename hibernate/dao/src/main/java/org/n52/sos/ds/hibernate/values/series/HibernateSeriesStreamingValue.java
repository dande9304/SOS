/**
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sos.ds.hibernate.values.series;

import org.hibernate.Session;
import org.n52.sos.ds.hibernate.dao.DaoFactory;
import org.n52.sos.ds.hibernate.dao.series.AbstractSeriesValueDAO;
import org.n52.sos.ds.hibernate.dao.series.AbstractSeriesValueTimeDAO;
import org.n52.sos.ds.hibernate.entities.series.values.SeriesValue;
import org.n52.sos.ds.hibernate.entities.values.AbstractValue;
import org.n52.sos.ds.hibernate.util.ObservationTimeExtrema;
import org.n52.sos.ds.hibernate.values.AbstractHibernateStreamingValue;
import org.n52.sos.exception.CodedException;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.util.GmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Hibernate series streaming value class for the series concept
 * 
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.0.2
 *
 */
public abstract class HibernateSeriesStreamingValue extends AbstractHibernateStreamingValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateSeriesStreamingValue.class);

    private static final long serialVersionUID = 201732114914686926L;

    protected final AbstractSeriesValueDAO seriesValueDAO;

    protected final AbstractSeriesValueTimeDAO seriesValueTimeDAO;

    protected long series;
    
    private boolean duplicated;

    /**
     * constructor
     * 
     * @param request
     *            {@link GetObservationRequest}
     * @param series
     *            Datasource series id
     * @param duplicated 
     * @throws CodedException
     */
    public HibernateSeriesStreamingValue(GetObservationRequest request, long series, boolean duplicated) throws CodedException {
        super(request);
        this.series = series;
        this.duplicated = duplicated;
        this.seriesValueDAO = (AbstractSeriesValueDAO) DaoFactory.getInstance().getValueDAO();
        this.seriesValueTimeDAO = (AbstractSeriesValueTimeDAO) DaoFactory.getInstance().getValueTimeDAO();
    }

    @Override
    protected void queryTimes() {
        Session s = null;
        try {
            s = sessionHolder.getSession();
            ObservationTimeExtrema timeExtrema =
                    seriesValueTimeDAO.getTimeExtremaForSeries(request, series, temporalFilterCriterion, s);
            if (timeExtrema.isSetPhenomenonTime()) {
                setPhenomenonTime(GmlHelper.createTime(timeExtrema.getMinPhenTime(), timeExtrema.getMaxPhenTime()));
            }
            if (timeExtrema.isSetResultTime()) {
                setResultTime(new TimeInstant(timeExtrema.getMaxResultTime()));
            }
            if (timeExtrema.isSetValidTime()) {
                setValidTime(GmlHelper.createTime(timeExtrema.getMinValidTime(), timeExtrema.getMaxValidTime()));
            }
        } catch (OwsExceptionReport owse) {
            LOGGER.error("Error while querying times", owse);
        } finally {
            sessionHolder.returnSession(s);
        }
    }

    @Override
    protected void queryUnit() {
        Session s = null;
        try {
           s = sessionHolder.getSession();
            setUnit(seriesValueDAO.getUnit(request, series, s));
        } catch (OwsExceptionReport owse) {
            LOGGER.error("Error while querying unit", owse);
        } finally {
            sessionHolder.returnSession(s);
        }
    }
    
    protected boolean checkValue(AbstractValue value) {
        if (isDuplicated()) {
            return value.getOfferings() != null && value.getOfferings().size() == 1;
        }
        return true;
     }
    
    protected boolean isDuplicated() {
        return duplicated;
    }

}
