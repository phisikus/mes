package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PurchaseFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PurchaseHooks {

  @Autowired
  DataDefinitionService dataDefinitionService;

  public boolean onCreateCheckDuplicate(final DataDefinition purchaseDD, final Entity purchase) {
    if (purchase != null) {
      Entity existingDuplicate = getDuplicatePurchase(purchase);

      if (existingDuplicate != null) {
        purchase.addGlobalError("basic.purchase.error.isNotUnique");
        return false;
      }
    }
    return true;
  }

  public boolean onUpdateCheckDuplicate(final DataDefinition purchaseDD, final Entity purchase) {
    if (purchase != null) {
      Entity existingDuplicate = getDuplicatePurchase(purchase);

      if (existingDuplicate != null) {
        if (purchase.getId() != null && !purchase.getId().equals(existingDuplicate.getId())) {
          purchase.addGlobalError("basic.purchase.error.isNotUnique");
          return false;
        }
      }
    }
    return true;
  }

  private Entity getDuplicatePurchase(Entity purchase) {
    SearchCriteriaBuilder duplicateCriteria = getPurchaseDuplicationCriteria(purchase);
    return duplicateCriteria.setMaxResults(1).uniqueResult();
  }


  private SearchCriteriaBuilder getPurchaseDuplicationCriteria(Entity purchase) {

    Entity product = purchase.getBelongsToField("product");
    return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PURCHASE).find().
        add(SearchRestrictions.eq(PurchaseFields.PRICE, purchase.getDecimalField(PurchaseFields.PRICE))).
        add(SearchRestrictions.belongsTo(PurchaseFields.PRODUCT, product));
  }

}
