package com.qcadoo.mes.purchase;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.purchase.constants.PurchaseConstans;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.internal.search.SearchResultImpl;
import com.qcadoo.view.api.ViewDefinitionState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;

public class PurchaseServiceTest {

  protected PurchaseService purchaseService;

  @Mock
  protected DataDefinitionService dataDefinitionService;

  @Mock
  protected DataDefinition dataDefinition;

  @Mock
  protected SearchCriteriaBuilder searchCriteriaBuilder;


  @Mock
  private ViewDefinitionState view;

  @Mock
  private Entity mockPurchase;

  @Before
  public final void init() {
    MockitoAnnotations.initMocks(this);
    purchaseService = new PurchaseService();
    given(dataDefinitionService.get(PurchaseConstans.PLUGIN_IDENTIFIER, PurchaseConstans.MODEL_PURCHASE)).willReturn(dataDefinition);
    given(dataDefinition.find()).willReturn(searchCriteriaBuilder);
    ReflectionTestUtils.setField(purchaseService, "dataDefinitionService", dataDefinitionService);
  }

  @Test
  public final void getAveragePriceOfAllPurchasesTest() {

    BigDecimal average = new BigDecimal(2.77);
    given(mockPurchase.getDecimalField(anyString())).willReturn(average);

    SearchResultImpl searchResult = getPurchaseSearchResults(1000);
    given(searchCriteriaBuilder.list()).willReturn(searchResult);

    purchaseService.getAveragePriceOfAllPurchases(view, null, null);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(view).redirectTo(stringCaptor.capture(), anyBoolean(), anyBoolean());
    String resultUrl = stringCaptor.getValue();
    BigDecimal resultAverage = new BigDecimal(extractPriceFromUrl(resultUrl));

    assertEquals(resultAverage, average);

  }


  @Test
  public final void getAveragePriceOfAllPurchasesZeroTest() {

    BigDecimal average = new BigDecimal(2.77);
    BigDecimal zero = new BigDecimal(0);
    given(mockPurchase.getDecimalField(anyString())).willReturn(average);

    SearchResultImpl searchResult = getPurchaseSearchResults(0);
    given(searchCriteriaBuilder.list()).willReturn(searchResult);

    purchaseService.getAveragePriceOfAllPurchases(view, null, null);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(view).redirectTo(stringCaptor.capture(), anyBoolean(), anyBoolean());
    String resultUrl = stringCaptor.getValue();
    BigDecimal resultAverage = new BigDecimal(extractPriceFromUrl(resultUrl));

    assertEquals(resultAverage, zero);

  }

  private String extractPriceFromUrl(String url) {
    String[] results = url.split("=");
    assertEquals(results.length, 2);
    return results[1];
  }


  private SearchResultImpl getPurchaseSearchResults(int quantity) {
    SearchResultImpl searchResult = new SearchResultImpl();
    searchResult.setResults(getPurchases(quantity));
    return searchResult;
  }

  private List<Entity> getPurchases(int quantity) {
    List<Entity> purchases = new ArrayList<>();
    for (int i = 0; i < quantity; i++) {
      purchases.add(mockPurchase);
    }
    return purchases;
  }


}
