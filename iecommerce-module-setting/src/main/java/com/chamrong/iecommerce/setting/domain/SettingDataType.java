package com.chamrong.iecommerce.setting.domain;

/** Represents the expected data type of a setting value for safe deserialization. */
public enum SettingDataType {
  /** Plain string value. */
  STRING,

  /** Integer numeric value. */
  INTEGER,

  /** Boolean flag stored as "true" / "false". */
  BOOLEAN,

  /** Arbitrary JSON blob for complex, structured values. */
  JSON,
}
