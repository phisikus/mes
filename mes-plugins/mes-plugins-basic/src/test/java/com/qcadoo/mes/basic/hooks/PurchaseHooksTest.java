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

import static junit.framework.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
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
        given(purchase.getBelongsToField(anyString())).willReturn(null);

        ReflectionTestUtils.setField(purchaseHooks, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public final void onCreateCheckDuplicateNullTest() {
        assertEquals(purchaseHooks.onCreateCheckDuplicate(null, null), true);
    }

    @Test
    public final void onCreateCheckDuplicateTest() {
        ArgumentCaptor<SearchCriterion> restrictionCaptor = ArgumentCaptor.forClass(SearchCriterion.class);

        assertFalse(purchaseHooks.onCreateCheckDuplicate(dataDefinition, purchase));

        assertTrue(correctFieldsCalled());

        Mockito.verify(searchCriteriaBuilder, times(2)).add(restrictionCaptor.capture());
        assertTrue(correctPurchaseCriteria(restrictionCaptor.getAllValues()));

    }


    @Test
    public final void onUpdateCheckDuplicateTest() {
        ArgumentCaptor<SearchCriterion> restrictionCaptor = ArgumentCaptor.forClass(SearchCriterion.class);

        assertTrue(purchaseHooks.onUpdateCheckDuplicate(dataDefinition, purchase));

        assertTrue(correctFieldsCalled());

        Mockito.verify(searchCriteriaBuilder, times(2)).add(restrictionCaptor.capture());
        assertTrue(correctPurchaseCriteria(restrictionCaptor.getAllValues()));

    }

    private boolean correctFieldsCalled() {
        boolean result;
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(purchase).getBelongsToField(stringCaptor.capture());
        result = stringCaptor.getValue().equals(PurchaseFields.PRODUCT);
        Mockito.verify(purchase).getDecimalField(stringCaptor.capture());
        return result && stringCaptor.getValue().equals(PurchaseFields.PRICE);
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

            return priceCriteriaPropertyName.equals("price") &&
                productCriteriaPropertyName.equals("product") &&
                priceCriteriaOperator.equals("=");

        } else {
            return false;
        }
    }


}
