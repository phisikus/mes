package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.PurchaseFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PurchaseHooks {

  @Autowired
  DataDefinitionService dataDefinitionService;

  public boolean checkIfNotDuplicateProductAndPrice(final DataDefinition purchaseDD, final Entity purchase) {
    if (purchase != null) {

      /*SearchCriteriaBuilder duplicateCriteria = purchaseDD.find();
      duplicateCriteria = duplicateCriteria.add(SearchRestrictions.eq(PurchaseFields.PRODUCT, purchase.getField(PurchaseFields.PRODUCT)));
      duplicateCriteria = duplicateCriteria.add(SearchRestrictions.eq(PurchaseFields.PRICE, purchase.getDecimalField(PurchaseFields.PRICE)));
      boolean isDuplicated = duplicateCriteria.list().getTotalNumberOfEntities() != 0;*/
      boolean isDuplicated = purchaseDD.find().add(SearchRestrictions.eq(PurchaseFields.PRODUCT, purchase.getField(PurchaseFields.PRODUCT))).uniqueResult() != null;

      if (isDuplicated) {
        purchase.addGlobalError("basic.purchase.error.isNotUnique");
        return false;
      }
    }
    return true;
  }

}
