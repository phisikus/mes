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
package com.qcadoo.mes.masterOrders.hooks;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.COMPANY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.DEADLINE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.masterOrders.util.MasterOrderProductsDataService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MasterOrderHooks {

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Autowired
    private MasterOrderProductsDataService masterOrderProductsDataService;

    public void onCreate(final DataDefinition dataDefinition, final Entity masterOrder) {
        setExternalSynchronizedField(masterOrder);
    }

    public void onSave(final DataDefinition dataDefinition, final Entity masterOrder) {
        onTypeTransitionFromOneToOther(masterOrder);
        onTypeTransitionFromManyToOther(masterOrder);
    }

    public void onUpdate(final DataDefinition dataDefinition, final Entity masterOrder) {
        changedDeadlineAndInOrder(masterOrder);
    }

    public void onCopy(final DataDefinition dataDefinition, final Entity masterOrder) {
        clearExternalFields(masterOrder);
    }

    protected void setExternalSynchronizedField(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

    protected void calculateCumulativeQuantityFromOrders(final Entity masterOrder) {
        if (masterOrder.getId() == null || MasterOrderType.of(masterOrder) != MasterOrderType.ONE_PRODUCT) {
            return;
        }

        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);

        BigDecimal quantitiesSum = masterOrderOrdersDataProvider.sumBelongingOrdersPlannedQuantities(masterOrder, product);

        masterOrder.setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, quantitiesSum);
    }

    private void fillRegisteredQuantity(Entity masterOrder) {
        if (masterOrder.getId() == null || MasterOrderType.of(masterOrder) != MasterOrderType.ONE_PRODUCT) {
            return;
        }
        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);

        BigDecimal doneQuantity = masterOrderOrdersDataProvider.sumBelongingOrdersDoneQuantities(masterOrder, product);
        masterOrder.setField("producedOrderQuantity", doneQuantity);

    }

    protected void changedDeadlineAndInOrder(final Entity masterOrder) {
        Preconditions.checkArgument(masterOrder.getId() != null, "Method expects already persisted entity");
        Date deadline = masterOrder.getDateField(DEADLINE);
        Entity customer = masterOrder.getBelongsToField(COMPANY);

        if (deadline == null && customer == null) {
            return;
        }

        List<Entity> allOrders = masterOrder.getHasManyField(MasterOrderFields.ORDERS);
        boolean hasBeenChanged = false;

        for (Entity order : allOrders) {
            if (OrderState.of(order) != OrderState.PENDING) {
                continue;
            }

            if (!ObjectUtils.equals(order.getDateField(OrderFields.DEADLINE), deadline)) {
                order.setField(OrderFields.DEADLINE, deadline);
                hasBeenChanged = true;
            }

            if (!ObjectUtils.equals(order.getBelongsToField(OrderFields.COMPANY), customer)) {
                order.setField(OrderFields.COMPANY, customer);
                hasBeenChanged = true;
            }
        }

        if (hasBeenChanged) {
            masterOrder.setField(MasterOrderFields.ORDERS, allOrders);
        }

    }

    private void onTypeTransitionFromOneToOther(final Entity masterOrder) {
        if (masterOrder.getId() == null || isNotLeavingType(masterOrder, MasterOrderType.ONE_PRODUCT)) {
            return;
        }
        if (isTransitionToManyProducts(masterOrder)) {
            clearAndRegenerateProducts(masterOrder);
        }
        clearFieldsForOneProduct(masterOrder);
    }

    private void clearAndRegenerateProducts(final Entity masterOrder) {
        // to avoid uniqueness validation issues on the old data
        masterOrderProductsDataService.deleteExistingMasterOrderProducts(masterOrder);
        Entity masterOrderProduct = masterOrderProductsDataService.createProductEntryFor(masterOrder);
        masterOrder.setField(MasterOrderFields.MASTER_ORDER_PRODUCTS, Lists.newArrayList(masterOrderProduct));
    }

    private void clearFieldsForOneProduct(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.PRODUCT, null);
        masterOrder.setField(MasterOrderFields.TECHNOLOGY, null);
        masterOrder.setField(MasterOrderFields.MASTER_ORDER_QUANTITY, null);
    }

    private void onTypeTransitionFromManyToOther(final Entity masterOrder) {
        if (masterOrder.getId() == null || isNotLeavingType(masterOrder, MasterOrderType.MANY_PRODUCTS)) {
            return;
        }
        // remove master order's products when leaving 'many products' mode
        masterOrderProductsDataService.deleteExistingMasterOrderProducts(masterOrder);
    }

    private boolean isTransitionToManyProducts(final Entity masterOrder) {
        return MasterOrderType.of(masterOrder) == MasterOrderType.MANY_PRODUCTS;
    }

    private boolean isNotLeavingType(final Entity masterOrder, final MasterOrderType type) {
        Entity masterOrderFromDB = masterOrder.getDataDefinition().get(masterOrder.getId());
        MasterOrderType existingMasterOrderType = MasterOrderType.of(masterOrderFromDB);
        MasterOrderType newMasterOrderType = MasterOrderType.of(masterOrder);
        return existingMasterOrderType != type || newMasterOrderType == type;
    }

    private void clearExternalFields(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_NUMBER, null);
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

}
