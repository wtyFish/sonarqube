/*
 * SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.db.migration.version.v83.util;

import java.sql.SQLException;
import java.util.List;
import org.junit.Test;
import org.sonar.db.Database;
import org.sonar.db.dialect.H2;
import org.sonar.db.dialect.MsSql;
import org.sonar.db.dialect.Oracle;
import org.sonar.db.dialect.PostgreSql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DropPrimaryKeySqlGeneratorTest {

  private static final String TABLE_NAME = "issues";
  private static final String ORIGINAL_TABLE_NAME = "original_issues";
  private static final String PK_COLUMN = "id";
  private static final String CONSTRAINT = "pk_id";

  private static final PostgreSql POSTGRESQL = new PostgreSql();
  private static final MsSql MS_SQL = new MsSql();
  private static final Oracle ORACLE = new Oracle();
  private static final org.sonar.db.dialect.H2 H2 = new H2();

  private Database db = mock(Database.class);
  private GetConstraintHelper getConstraintHelper = mock(GetConstraintHelper.class);

  private DropPrimaryKeySqlGenerator underTest = new DropPrimaryKeySqlGenerator(db, getConstraintHelper);

  @Test
  public void generate_for_postgres_sql() throws SQLException {
    when(getConstraintHelper.getPostgresSqlConstraint(TABLE_NAME)).thenReturn(CONSTRAINT);
    when(db.getDialect()).thenReturn(POSTGRESQL);

    List<String> sqls = underTest.generate(TABLE_NAME, ORIGINAL_TABLE_NAME, PK_COLUMN);

    assertThat(sqls).containsExactly("ALTER TABLE issues ALTER COLUMN id DROP DEFAULT",
      "DROP SEQUENCE original_issues_id_seq",
      "ALTER TABLE issues DROP CONSTRAINT pk_id");
  }

  @Test
  public void generate_for_ms_sql() throws SQLException {
    when(getConstraintHelper.getMssqlConstraint(TABLE_NAME)).thenReturn(CONSTRAINT);
    when(db.getDialect()).thenReturn(MS_SQL);

    List<String> sqls = underTest.generate(TABLE_NAME, ORIGINAL_TABLE_NAME, PK_COLUMN);

    assertThat(sqls).containsExactly("ALTER TABLE issues DROP CONSTRAINT pk_id");
  }

  @Test
  public void generate_for_oracle() throws SQLException {
    when(getConstraintHelper.getOracleConstraint(TABLE_NAME)).thenReturn(CONSTRAINT);
    when(db.getDialect()).thenReturn(ORACLE);

    List<String> sqls = underTest.generate(TABLE_NAME, ORIGINAL_TABLE_NAME, PK_COLUMN);

    assertThat(sqls).containsExactly("DROP TRIGGER issues_IDT",
      "DROP SEQUENCE issues_SEQ",
      "ALTER TABLE issues DROP CONSTRAINT pk_id");
  }

  @Test
  public void generate_for_h2() throws SQLException {
    when(getConstraintHelper.getH2Constraint(TABLE_NAME)).thenReturn(CONSTRAINT);
    when(db.getDialect()).thenReturn(H2);

    List<String> sqls = underTest.generate(TABLE_NAME, ORIGINAL_TABLE_NAME, PK_COLUMN);

    assertThat(sqls).containsExactly("ALTER TABLE issues DROP CONSTRAINT PK_original_issues",
      "ALTER TABLE issues ALTER COLUMN id INTEGER NOT NULL");
  }
}
