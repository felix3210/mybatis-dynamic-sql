/**
 *    Copyright 2016-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.where.render;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.AbstractNoValueCondition;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.AbstractSubselectCondition;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;
import org.mybatis.dynamic.sql.ConditionVisitor;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectSupport;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.render.WhereFragmentCollector.Triple;

public class WhereConditionVisitor<T> implements ConditionVisitor<T, FragmentAndParameters> {
    
    private static final String PARAMETERS_PREFIX = "parameters"; //$NON-NLS-1$
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence;
    private SqlColumn<T> column;
    private Map<SqlTable, String> tableAliases;
    
    public WhereConditionVisitor(RenderingStrategy renderingStrategy, AtomicInteger sequence, SqlColumn<T> column,
            Map<SqlTable, String> tableAliases) {
        this.renderingStrategy = renderingStrategy;
        this.sequence = sequence;
        this.column = column;
        this.tableAliases = tableAliases;
    }

    @Override
    public FragmentAndParameters visit(AbstractListValueCondition<T> condition) {
        WhereFragmentCollector fc = condition.values()
                .map(this::toTriple)
                .collect(WhereFragmentCollector.tripleCollector());
        
        return new FragmentAndParameters.Builder(condition.renderCondition(columnName(), fc.fragments()))
                .withParameters(fc.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractNoValueCondition<T> condition) {
        return new FragmentAndParameters.Builder(condition.renderCondition(columnName())).build();
    }

    @Override
    public FragmentAndParameters visit(AbstractSingleValueCondition<T> condition) {
        String mapKey = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = condition.renderCondition(columnName(),
                getFormattedJdbcPlaceholder(mapKey));

        return new FragmentAndParameters.Builder(fragment)
                .withParameter(mapKey, condition.value())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractTwoValueCondition<T> condition) {
        String mapKey1 = formatParameterMapKey(sequence.getAndIncrement());
        String mapKey2 = formatParameterMapKey(sequence.getAndIncrement());
        String fragment = condition.renderCondition(columnName(),
                getFormattedJdbcPlaceholder(mapKey1),
                getFormattedJdbcPlaceholder(mapKey2));
                
        return new FragmentAndParameters.Builder(fragment)
                .withParameter(mapKey1, condition.value1())
                .withParameter(mapKey2, condition.value2())
                .build();
    }
    

    @Override
    public FragmentAndParameters visit(AbstractSubselectCondition<T> condition) {
        SelectSupport ss = SelectRenderer.of(condition.selectModel()).render(renderingStrategy, sequence);
        
        return new FragmentAndParameters.Builder(condition.renderCondition(columnName(), ss.getFullSelectStatement()))
                .withParameters(ss.getParameters())
                .build();
    }
    
    private Triple toTriple(Object value) {
        String mapKey = formatParameterMapKey(sequence.getAndIncrement());
        return Triple.of(mapKey, getFormattedJdbcPlaceholder(mapKey), value);
    }

    protected String formatParameterMapKey(int number) {
        return "p" + number; //$NON-NLS-1$
    }
    
    private String getFormattedJdbcPlaceholder(String mapKey) {
        return renderingStrategy.getFormattedJdbcPlaceholder(column, PARAMETERS_PREFIX, mapKey);        
    }
    
    private String columnName() {
        return column.nameIncludingTableAlias(tableAlias());
    }
    
    private Optional<String> tableAlias() {
        return column.table()
                .flatMap(t -> Optional.ofNullable(tableAliases.get(t)));
    }
}
