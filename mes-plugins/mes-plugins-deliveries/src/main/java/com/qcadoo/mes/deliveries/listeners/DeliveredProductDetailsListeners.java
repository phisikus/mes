/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deliveries.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.hooks.DeliveredProductDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class DeliveredProductDetailsListeners {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DeliveredProductDetailsHooks deliveredProductDetailsHooks;

    public void fillUnitFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillUnitFields(view);
    }

    public void fillCurrencyFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillCurrencyFields(view);
    }

    public void fillOrderedQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillOrderedQuantities(view);
    }

    public void calculatePriceFromTotalPrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePriceFromTotalPrice(view, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public void calculatePriceFromPricePerUnit(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePriceFromPricePerUnit(view, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public void calculatePrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePrice(view, DeliveredProductFields.DELIVERED_QUANTITY);
    }

}
