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
package org.mybatis.dynamic.sql.mybatis3;

import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlConditions.*;

import java.sql.JDBCType;
import java.util.Date;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectSupport;

@RunWith(JUnitPlatform.class)
public class SelectSupportTest {
    public static final SqlTable table = SqlTable.of("foo");
    public static final SqlColumn<Date> column1 = SqlColumn.of(table, "column1", JDBCType.DATE);
    public static final SqlColumn<Integer> column2 = SqlColumn.of(table, "column2", JDBCType.INTEGER);

    @Test
    public void testSimpleCriteriaWithoutAlias() {
        Date d = new Date();

        SelectSupport selectSupport = select(column1, column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectSupport.getWhereClause()).isEqualTo(
                    "where a.column1 = #{parameters.p1,jdbcType=DATE} or a.column2 = #{parameters.p2,jdbcType=INTEGER} and a.column2 < #{parameters.p3,jdbcType=INTEGER}");

            Map<String, Object> parameters = selectSupport.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
            softly.assertThat(parameters.get("p2")).isEqualTo(4);
            softly.assertThat(parameters.get("p3")).isEqualTo(3);
        });
    }

    @Test
    public void testComplexCriteriaWithoutAlias() {
        Date d = new Date();

        SelectSupport selectSupport = select(column1, column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .or(column2, isEqualTo(4), and(column2, isEqualTo(6)))
                .and(column2, isLessThan(3), or(column1, isEqualTo(d)))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        

        String expected = "where a.column1 = #{parameters.p1,jdbcType=DATE}" +
                " or a.column2 = #{parameters.p2,jdbcType=INTEGER}" +
                " and a.column2 < #{parameters.p3,jdbcType=INTEGER}" +
                " or (a.column2 = #{parameters.p4,jdbcType=INTEGER} and a.column2 = #{parameters.p5,jdbcType=INTEGER})" +
                " and (a.column2 < #{parameters.p6,jdbcType=INTEGER} or a.column1 = #{parameters.p7,jdbcType=DATE})";
        
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectSupport.getWhereClause()).isEqualTo(expected);

            Map<String, Object> parameters = selectSupport.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
            softly.assertThat(parameters.get("p2")).isEqualTo(4);
            softly.assertThat(parameters.get("p3")).isEqualTo(3);
            softly.assertThat(parameters.get("p4")).isEqualTo(4);
            softly.assertThat(parameters.get("p5")).isEqualTo(6);
            softly.assertThat(parameters.get("p6")).isEqualTo(3);
            softly.assertThat(parameters.get("p7")).isEqualTo(d);
        });
    }

    @Test
    public void testSimpleCriteriaWithAlias() {
        Date d = new Date();

        SelectSupport selectSupport = select(column1, column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectSupport.getWhereClause()).isEqualTo(
                    "where a.column1 = #{parameters.p1,jdbcType=DATE} or a.column2 = #{parameters.p2,jdbcType=INTEGER} and a.column2 < #{parameters.p3,jdbcType=INTEGER}");

            Map<String, Object> parameters = selectSupport.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
            softly.assertThat(parameters.get("p2")).isEqualTo(4);
            softly.assertThat(parameters.get("p3")).isEqualTo(3);
        });
    }

    @Test
    public void testComplexCriteriaWithAlias() {
        Date d = new Date();

        SelectSupport selectSupport = select(column1, column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .or(column2, isEqualTo(4), and(column2, isEqualTo(6), or(column2, isEqualTo(7))))
                .and(column2, isLessThan(3), or(column1, isEqualTo(d), and(column2, isEqualTo(88))))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        

        String expected = "where a.column1 = #{parameters.p1,jdbcType=DATE}" +
                " or a.column2 = #{parameters.p2,jdbcType=INTEGER}" +
                " and a.column2 < #{parameters.p3,jdbcType=INTEGER}" +
                " or (a.column2 = #{parameters.p4,jdbcType=INTEGER} and (a.column2 = #{parameters.p5,jdbcType=INTEGER} or a.column2 = #{parameters.p6,jdbcType=INTEGER}))" +
                " and (a.column2 < #{parameters.p7,jdbcType=INTEGER} or (a.column1 = #{parameters.p8,jdbcType=DATE} and a.column2 = #{parameters.p9,jdbcType=INTEGER}))";
        
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectSupport.getWhereClause()).isEqualTo(expected);

            Map<String, Object> parameters = selectSupport.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
            softly.assertThat(parameters.get("p2")).isEqualTo(4);
            softly.assertThat(parameters.get("p3")).isEqualTo(3);
            softly.assertThat(parameters.get("p4")).isEqualTo(4);
            softly.assertThat(parameters.get("p5")).isEqualTo(6);
            softly.assertThat(parameters.get("p6")).isEqualTo(7);
            softly.assertThat(parameters.get("p7")).isEqualTo(3);
            softly.assertThat(parameters.get("p8")).isEqualTo(d);
            softly.assertThat(parameters.get("p9")).isEqualTo(88);
        });
    }
}
