package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.PurchaseFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

@Service
public class PurchaseHooks {

  public boolean checkIfNotDuplicateProductAndPrice(final DataDefinition purchaseDD, final Entity purchase) {
    if (purchase != null) {

      SearchCriteriaBuilder duplicatePurchaseRestriction = purchaseDD.find().
          add(
              SearchRestrictions.eq(PurchaseFields.PRODUCT, purchase.getBelongsToField(PurchaseFields.PRODUCT))).
          add(
              SearchRestrictions.eq(PurchaseFields.PRICE, purchase.getDecimalField(PurchaseFields.PRICE))
          );

      Entity duplicatePurchase = duplicatePurchaseRestriction.setMaxResults(1).uniqueResult();

      if (duplicatePurchase != null) {
        purchase.addGlobalError("basic.purchase.error.isNotUnique");
        return false;
      }
    }
    return true;
  }

}
