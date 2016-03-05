package com.qcadoo.mes.basic;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PurchaseFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseService {

  final private String purchasesListUrl = "../page/basic/purchasesList.html";

  @Autowired
  private DataDefinitionService dataDefinitionService;


  public void getAveragePriceOfAllPurchases(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
    SearchResult resultList = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PURCHASE).find().list();
    List<Entity> purchases = resultList.getEntities();
    BigDecimal averagePrice = getAveragePrice(purchases);
    String url = purchasesListUrl + "?averagePrice=" + averagePrice;
    view.redirectTo(url, false, true);
  }

  private BigDecimal getAveragePrice(List<Entity> purchases) {
    BigDecimal sum = new BigDecimal(0L);
    int size = purchases.size();

    for (Entity purchase : purchases) {
      sum = sum.add(purchase.getDecimalField(PurchaseFields.PRICE));
    }

    if (size > 0) {
      return sum.divide(new BigDecimal(size));
    } else {
      return sum;
    }
  }

}
