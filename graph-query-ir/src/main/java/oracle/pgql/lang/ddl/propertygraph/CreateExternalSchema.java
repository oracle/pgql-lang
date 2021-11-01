/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ddl.propertygraph;

import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.StatementType;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;
import static oracle.pgql.lang.ir.PgqlUtils.printLiteral;

public class CreateExternalSchema implements PgqlStatement {

  private String localSchemaName;

  private String url;

  private String userName;

  private String keystoreAlias;

  private String dataSourceName;

  private String remoteSchemaName;

  public CreateExternalSchema(String localSchemaName, String url, String userName, String keystoreAlias,
      String remoteSchemaName) {
    this.localSchemaName = localSchemaName;
    this.url = url;
    this.userName = userName;
    this.keystoreAlias = keystoreAlias;
    this.remoteSchemaName = remoteSchemaName;
  }

  public CreateExternalSchema(String localSchemaName, String dataSourceName, String remoteSchemaName) {
    this.localSchemaName = localSchemaName;
    this.dataSourceName = dataSourceName;
    this.remoteSchemaName = remoteSchemaName;
  }

  public String getLocalSchemaName() {
    return localSchemaName;
  }

  public void setLocalSchemaName(String localSchemaName) {
    this.localSchemaName = localSchemaName;
  }

  public String getDataSourceName() {
    return dataSourceName;
  }

  public void setDataSourceName(String dataSourceName) {
    this.dataSourceName = dataSourceName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getKeystoreAlias() {
    return keystoreAlias;
  }

  public void setKeystoreAlias(String keystoreAlias) {
    this.keystoreAlias = keystoreAlias;
  }

  public String getRemoteSchemaName() {
    return remoteSchemaName;
  }

  public void setRemoteSchemaName(String remoteSchemaName) {
    this.remoteSchemaName = remoteSchemaName;
  }

  @Override
  public String toString() {
    String separator = "\n  ";
    String result = "CREATE EXTERNAL SCHEMA " + printIdentifier(localSchemaName) + "\nFROM DATABASE";
    if (url != null) {
      result += separator + "URL " + printLiteral(url);
    }
    if (userName != null) {
      result += separator + "USER " + printLiteral(userName);
    }
    if (keystoreAlias != null) {
      result += separator + "KEYSTORE_ALIAS " + printLiteral(keystoreAlias);
    }
    if (dataSourceName != null) {
      result += separator + "DATA_SOURCE " + printLiteral(dataSourceName);
    }
    if (remoteSchemaName != null) {
      result += separator + "SCHEMA " + printLiteral(remoteSchemaName);
    }
    return result;
  }

  @Override
  public StatementType getStatementType() {
    return StatementType.CREATE_EXTERNAL_SCHEMA;
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CreateExternalSchema other = (CreateExternalSchema) obj;
    if (dataSourceName == null) {
      if (other.dataSourceName != null)
        return false;
    } else if (!dataSourceName.equals(other.dataSourceName))
      return false;
    if (keystoreAlias == null) {
      if (other.keystoreAlias != null)
        return false;
    } else if (!keystoreAlias.equals(other.keystoreAlias))
      return false;
    if (localSchemaName == null) {
      if (other.localSchemaName != null)
        return false;
    } else if (!localSchemaName.equals(other.localSchemaName))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    if (userName == null) {
      if (other.userName != null)
        return false;
    } else if (!userName.equals(other.userName))
      return false;
    if (remoteSchemaName == null) {
      if (other.remoteSchemaName != null)
        return false;
    } else if (!remoteSchemaName.equals(other.remoteSchemaName))
      return false;
    return true;
  }
}
