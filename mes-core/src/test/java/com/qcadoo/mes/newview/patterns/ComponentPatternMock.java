package com.qcadoo.mes.newview.patterns;

import java.util.Map;

import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.AbstractContainerPattern;
import com.qcadoo.mes.newview.ComponentPattern;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.states.components.ComponentStateMock;

public class ComponentPatternMock extends AbstractContainerPattern {

    public ComponentPatternMock(final String name, final String fieldPath, final String sourceFieldPath,
            final AbstractComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new ComponentStateMock(null);
    }

    @Override
    public Map<String, ComponentPattern> getFieldEntityIdChangeListeners() {
        return super.getFieldEntityIdChangeListeners();
    }

    @Override
    public Map<String, ComponentPattern> getScopeEntityIdChangeListeners() {
        return super.getScopeEntityIdChangeListeners();
    }

}
