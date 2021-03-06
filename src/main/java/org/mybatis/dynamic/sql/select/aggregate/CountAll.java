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
package org.mybatis.dynamic.sql.select.aggregate;

import java.util.Optional;

import org.mybatis.dynamic.sql.SelectListItem;
import org.mybatis.dynamic.sql.SqlTable;

/**
 * CountAll seems like the other aggregates, but it is special because there is no column.
 * Rather than dealing with a useless and confusing abstraction, we simply implement
 * SelectListItem directly.
 *  
 * @author Jeff Butler
 */
public class CountAll implements SelectListItem {
    
    private Optional<String> alias = Optional.empty();

    public CountAll() {
        super();
    }

    @Override
    public String nameIncludingTableAlias(Optional<String> tableAlias) {
        return "count(*)"; //$NON-NLS-1$
    }

    @Override
    public Optional<SqlTable> table() {
        return Optional.empty();
    }

    @Override
    public Optional<String> alias() {
        return alias;
    }

    public CountAll as(String alias) {
        CountAll copy = new CountAll();
        copy.alias = Optional.of(alias);
        return copy;
    }
}
