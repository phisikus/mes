package com.qcadoo.mes.basic.hooks;


import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PurchaseFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

public class PurchaseHooksTest {

    protected PurchaseHooks purchaseHooks;

    @Mock
    protected Entity purchase;
    @Mock
    protected DataDefinition dataDefinition;
    @Mock
    protected SearchCriteriaBuilder searchCriteriaBuilder;
    @Mock
    private DataDefinitionService dataDefinitionService;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        purchaseHooks = new PurchaseHooks();
        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PURCHASE)).willReturn(dataDefinition);
        given(dataDefinition.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(any())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(anyInt())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(purchase);
        ReflectionTestUtils.setField(purchaseHooks, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public final void onCreateCheckDuplicateNullTest() {
        assertEquals(purchaseHooks.onCreateCheckDuplicate(null, null), true);
    }

    @Test
    public final void onCreateCheckDuplicateTest() {
        ArgumentCaptor<SearchCriterion> restrictionCaptor = ArgumentCaptor.forClass(SearchCriterion.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        purchaseHooks.onCreateCheckDuplicate(dataDefinition, purchase);

        Mockito.verify(purchase).getBelongsToField(stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), PurchaseFields.PRODUCT);
        Mockito.verify(purchase).getDecimalField(stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), PurchaseFields.PRICE);

        Mockito.verify(searchCriteriaBuilder, times(2)).add(restrictionCaptor.capture());

        assertTrue(correctPurchaseCriteria(restrictionCaptor.getAllValues()));

    }


    private boolean correctPurchaseCriteria(List<SearchCriterion> searchCriterions) {
        if (searchCriterions.size() == 2) {
            Criterion priceCriteria = searchCriterions.get(0).getHibernateCriterion();
            Criterion productCritera = searchCriterions.get(1).getHibernateCriterion();

            String priceCriteriaPropertyName = (String) ReflectionTestUtils.
              getField(priceCriteria, "propertyName");
            String productCriteriaPropertyName = (String) ReflectionTestUtils.
              getField(productCritera, "propertyName");
            String priceCriteriaOperator = (String) ReflectionTestUtils.getField(priceCriteria, "op");
            //String productCriteriaOperator = (String) ReflectionTestUtils.getField(productCritera, "op");

            return priceCriteriaPropertyName.equals("price") &&
              productCriteriaPropertyName.equals("product") &&
              priceCriteriaOperator.equals("="); /*&&
              productCriteriaOperator.equals("=");*/


        } else {
            return false;
        }
    }


}
