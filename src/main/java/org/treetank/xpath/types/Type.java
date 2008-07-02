package org.treetank.xpath.types;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;

/**
 * Enum that represents the built-in-types of XPath 2.0.
 * 
 * @author Tina Scherer
 */
public enum Type {

  /* --------- UR Types ------------ */
  /** XML Schema type 'anyType. */
  ANY_TYPE(null, "xs:anyType", 1, false),

  /** XML Schema type 'anySimpleType'. */
  ANY_SIMPLE_TYPE(ANY_TYPE, "xs:anySimpleType", 2, false),

  /** XML Schema type 'anyAtomicType'. */
  ANY_ATOMIC_TYPE(ANY_SIMPLE_TYPE, "xs:anyAtomicType", 3, false),

  /**
   * Additional XMD type 'untyped value'. It denotes untyped atomic data, such
   * as text that has not been assigned a more specific type. An attribute that
   * has been validated in skip mode is represented in the Data Model by an
   * attribute node with the type xs:untypedAtomic.
   */
  UNTYPED_ATOMIC(ANY_ATOMIC_TYPE, "xs:untypedAtomic", 4, false) {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType || STRING == targetType || derivesFrom(targetType));

    }
  },

  /**
   * Additional XMD type 'untyped'. It denotes the dynamic type of an element
   * node that has not been validated, or has been validated in skip mode.
   */
  UNTYPED(ANY_TYPE, "xs:untyped", 2, false),

  /* --------- primitive types -------- */
  /**
   * XML Schema type 'string'. The string datatype represents character strings
   * in XML.
   */
  STRING(ANY_ATOMIC_TYPE, "xs:string", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return ".*"; // stringRep ::= Char* => all characters
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType || UNTYPED_ATOMIC == targetType || derivesFrom(targetType));

    }

  },

  /**
   * XML Schema type 'duration'. duration represents a duration of time.
   */
  DURATION(ANY_ATOMIC_TYPE, "xs:duration", 4, true) {

    public String getFacet() {

      return "-?P(((([0-9]+Y([0-9]+M)?)"
          + "|(([0-9]+M)))(([0-9]+D(T(([0-9]+H([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?)"
          + "|(([0-9]+M) ([0-9]+(\\.[0-9]+)?S)?)"
          + "|(([0-9]+(\\.[0-9]+)?S))))?)"
          + "|((T(([0-9]+H([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?)|(([0-9]+M) "
          + "([0-9]+(\\.[0-9]+)?S)?)"
          + "|(([0-9]+(\\.[0-9]+)?S))))))?)"
          + "|((([0-9]+D(T(([0-9]+H([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?)"
          + "|(([0-9]+M)([0-9]+(\\.[0-9]+)?S)?)"
          + "|(([0-9]+(\\.[0-9]+)?S))))?)"
          + "|((T(([0-9]+H([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?)|(([0-9]+M)([0-9]+"
          + "(\\.[0-9]+)?S)?)"
          + "|(([0-9]+(\\.[0-9]+)?S))))))))";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType

      || YEAR_MONTH_DURATION == targetType || DAY_TIME_DURATION == targetType

      || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'YearMonthDuration'.
   */
  YEAR_MONTH_DURATION(DURATION, "xs:yearMonthDuration", 5, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType

          || DAY_TIME_DURATION == targetType
          || DURATION == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'DayTimeDuration'.
   */
  DAY_TIME_DURATION(DURATION, "xs:dayTimeDuration", 5, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType

          || YEAR_MONTH_DURATION == targetType
          || DURATION == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'dateTime'. dateTime values may be viewed as objects with
   * integer-valued year, month, day, hour and minute properties, a
   * decimal-valued second property, and a boolean timezoned property.
   */
  DATE_TIME(ANY_ATOMIC_TYPE, "xs:dateTime", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || TIME == targetType
          || DATE == targetType
          || G_DAY == targetType
          || G_MONTH == targetType
          || G_MONTH_DAY == targetType
          || G_YEAR == targetType
          || G_YEAR_MONTH == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'time'. time represents an instant of time that recurs
   * every day.
   */
  TIME(ANY_ATOMIC_TYPE, "xs:time", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "(((([01][0-9])|(2[0-3])):([0-5][0-9])"
          + ":(([0-5][0-9])(\\.[0-9]+)?))|(24:00:00(\\.0+)?))"
          + "(Z|((\\+|-)(0[0-9]|1[0-4]):[0-5][0-9]))?";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType || UNTYPED_ATOMIC == targetType || STRING == targetType);
    }
  },

  /**
   * XML Schema type 'date'. The "value space" of date consists of top-open
   * intervals of exactly one day in length on the timelines of dateTime,
   * beginning on the beginning moment of each day (in each timezone), i.e.
   * '00:00:00', up to but not including '24:00:00' (which is identical with
   * '00:00:00' of the next day).
   */
  DATE(ANY_ATOMIC_TYPE, "xs:date", 4, true) {

    /**
     * {@inheritDoc}
     */
    public String getFacet() {

      return "-?(([1-9][0-9][0-9][0-9]+)|(0[0-9][0-9][0-9]))"
          + "-((0[1-9])|(1[0-2]))-(([0-2][0-9])|(3[01]))"
          + "((\\+|-)(0[0-9]|1[0-4]):[0-5][0-9])?";

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || DATE_TIME == targetType
          || G_DAY == targetType
          || G_MONTH == targetType
          || G_MONTH_DAY == targetType
          || G_YEAR == targetType
          || G_YEAR_MONTH == targetType
          || UNTYPED_ATOMIC == targetType || STRING == targetType);
    }
  },

  /* Gregorian Types */
  /**
   * XML Schema type 'gYearMonth'. gYearMonth represents a specific gregorian
   * month in a specific gregorian year.
   */
  G_YEAR_MONTH(ANY_ATOMIC_TYPE, "xs:gYearMonth", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "-?(([1-9][0-9][0-9][0-9]+)|(0[0-9][0-9][0-9])"
          + ")-((0[1-9])|(1[0-2]))((\\+|-)(0[0-9]|1[0-4]):[0-5][0-9])?";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'gYear'. gYear represents a gregorian calendar year.
   */
  G_YEAR(ANY_ATOMIC_TYPE, "xs:gYear", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "-?(([1-9][0-9][0-9][0-9]+)|(0[0-9][0-9][0-9])"
          + ")((\\+|\\-)(0[0-9]|1[0-4]):[0-5][0-9])?";

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'gMonthDay'. gMonthDay is a gregorian date that recurs,
   * specifically a day of the year such as the third of May.
   */
  G_MONTH_DAY(ANY_ATOMIC_TYPE, "xs:gMonthDay", 4, true) {

    /**
     * {@inheritDoc}
     */
    public String getFacet() {

      return "--((0[1-9])|(1[0-2]))-(([0-2][0-9])|(3[01]))"
          + "((\\+|-)(0[0-9]|1[0-4]):[0-5][0-9])?";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'gDay'. gDay is a gregorian day that recurs, specifically a
   * day of the month such as the 5th of the month.
   */
  G_DAY(ANY_ATOMIC_TYPE, "xs:gDay", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "---(([0-2][0-9]|3[01]))" + "((\\+|-)(0[0-9]|1[0-4]):[0-5][0-9])?";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'gMonth'. gMonth is a Gregorian month that recurs every
   * year.
   */
  G_MONTH(ANY_ATOMIC_TYPE, "xs:gMonth", 4, true) {

    /**
     * {@inheritDoc}
     */
    public String getFacet() {

      return "--((0[1-9])|(1[0-2]))" + "((\\+|-)(0[0-9]|1[0-4]):[0-5][0-9])?";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'boolean'. boolean has the "value space" required to
   * support the mathematical concept of binary-valued logic: {true, false}.
   */
  BOOLEAN(ANY_ATOMIC_TYPE, "xs:boolean", 4, true) {

    /**
     * {@inheritDoc}
     */
    public String getFacet() {

      return "0|1|true|false";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || FLOAT == targetType
          || DOUBLE == targetType
          || DECIMAL == targetType
          || INTEGER == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }

  },

  /**
   * XML Schema type 'base64Binary'. base64Binary represents Base64-encoded
   * arbitrary binary data.
   */
  BASE_64_BINARY(ANY_ATOMIC_TYPE, "xs:base64Binary", 4, true) {

    /**
     * {@inheritDoc}
     */
    public String getFacet() {

      return "((([A-Za-z0-9+/] ?){4})*(([A-Za-z0-9+/] ?){3}"
          + "[A-Za-z0-9+/]|([A-Za-z0-9+/] ?){2}[AEIMQUYcgkosw048] ?="
          + "|[A-Za-z0-9+/] ?[AQgw] ?= ?=))?";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || HEX_BINARY == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'hexBinary'. hexBinary represents arbitrary hex-encoded
   * binary data.
   */
  HEX_BINARY(ANY_ATOMIC_TYPE, "xs:hexBinary", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "([0-9a-fA-F]{2})*";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || BASE_64_BINARY == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'anyURI'. anyURI represents a Uniform Resource Identifier
   * Reference (URI).
   */
  ANY_URI(ANY_ATOMIC_TYPE, "xs:anyURI", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'QName'. QName represents XML qualified names.
   */
  QNAME(ANY_ATOMIC_TYPE, "xs:QName", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'NOTATION'. NOTATION represents the NOTATION attribute type
   * from [XML 1.0 (Second Edition)].
   */
  NOTATION(ANY_ATOMIC_TYPE, "xs:NOTATION", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType || UNTYPED_ATOMIC == targetType || STRING == targetType);
    }
  },

  /* NUMBERICS */
  /**
   * XML Schema type 'float'. float is patterned after the IEEE single-precision
   * 32-bit floating point type [IEEE 754-1985].
   */
  FLOAT(ANY_ATOMIC_TYPE, "xs:float", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "(-|\\+)?(([0-9]+(.[0-9]*)?)"
          + "|(.[0-9]+))((e|E)(-|\\+)?[0-9]+)?|-?INF|NaN|\\+INF";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || DOUBLE == targetType
          || DECIMAL == targetType
          || INTEGER == targetType
          || BOOLEAN == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }

  },

  /**
   * XML Schema type 'double'. The double data type is patterned after the IEEE
   * double-precision 64-bit floating point type [IEEE 754-1985].
   */
  DOUBLE(ANY_ATOMIC_TYPE, "xs:double", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "(-|\\+)?(([0-9]+(.[0-9]*)?)"
          + "|(.[0-9]+))((e|E)(-|\\+)?[0-9]+)?|-?INF|NaN";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || FLOAT == targetType
          || DECIMAL == targetType
          || INTEGER == targetType
          || BOOLEAN == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }

  },

  /** XML Schema type 'pDecimal'. */
  PDECIMAL(ANY_ATOMIC_TYPE, "xs:pDecimal", 4, true) {

    private String facet =
        "(-|\\+)?(([0-9]+(.[0-9]*)?)"
            + "|(.[0-9]+))((e|E)(-|\\+)?[0-9]+)?|-?INF|NaN|\\+INF";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }
  },

  /**
   * XML Schema type 'decimal'. decimal represents a subset of the real numbers,
   * which can be represented by decimal numerals.
   */
  DECIMAL(ANY_ATOMIC_TYPE, "xs:decimal", 4, true) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "-?(([0-9]+(.[0-9]*)?)|(.[0-9]+))";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || FLOAT == targetType
          || DOUBLE == targetType
          || INTEGER == targetType
          || BOOLEAN == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }

  },

  /* --------- derived types -------------- */
  /**
   * XML Schema type 'integer'. integer is derived from decimal by fixing the
   * value of fractionDigits to be 0 and disallowing the trailing decimal point.
   * This results in the standard mathematical concept of the integer numbers.
   * The value space of integer is the infinite set {...,-2,-1,0,1,2,...}.
   */
  INTEGER(DECIMAL, "xs:integer", 5, false) {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFacet() {

      return "-?[0-9]+";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean castsTo(final Type targetType) {

      return (this == targetType
          || FLOAT == targetType
          || DOUBLE == targetType
          || DECIMAL == targetType
          || BOOLEAN == targetType
          || UNTYPED_ATOMIC == targetType
          || STRING == targetType || derivesFrom(targetType));
    }

  },

  /**
   * XML Schema type 'long'. long is "derived" from integer by setting the value
   * of "maxInclusive" to be 9223372036854775807 and "minInclusive" to be
   * -9223372036854775808.
   */
  LONG(INTEGER, "xs:long", 6, false),

  /**
   * XML Schema type 'int'. int is "derived" from long by setting the value of
   * "maxInclusive" to be 2147483647 and "minInclusive" to be -2147483648.
   */
  INT(LONG, "xs:int", 7, false),

  /**
   * XML Schema type 'short'. short is "derived" from int by setting the value
   * of "maxInclusive" to be 32767 and "minInclusive" to be -32768.
   */
  SHORT(INT, "xs:short", 8, false),

  /**
   * XML Schema type 'byte'. byte is "derived" from short by setting the value
   * of "maxInclusive" to be 127 and "minInclusive" to be -128.
   */
  BYTE(SHORT, "xs:byte", 9, false),

  /**
   * XML Schema type 'nonPositiveInteger'. nonPositiveInteger is "derived" from
   * integer by setting the value of "maxInclusive" to be 0. This results in the
   * standard mathematical concept of the non-positive integers. The "value
   * space" of nonPositiveInteger is the infinite set {...,-2,-1,0}.
   */
  NON_POSITIVE_INTEGER(INTEGER, "xs:nonPositiveInteger", 6, false),

  /**
   * XML Schema type 'negativeInteger'. negativeInteger is "derived" from
   * nonPositiveInteger by setting the value of "maxInclusive" to be -1. This
   * results in the standard mathematical concept of the negative integers. The
   * "value space" of negativeInteger is the infinite set {...,-2,-1}.
   */
  NEGATIVE_INTEGER(NON_POSITIVE_INTEGER, "xs:negativeInteger", 7, false),

  /**
   * XML Schema type 'nonNegativeInteger'. nonNegativeInteger is "derived" from
   * integer by setting the value of "minInclusive" to be 0. This results in the
   * standard mathematical concept of the non-negative integers. The "value
   * space" of nonNegativeInteger is the infinite set {0,1,2,...}.
   */
  NON_NEGATIVE_INTERGER(INTEGER, "xs:nonNegativeInteger", 6, false),

  /**
   * XML Schema type 'positiveInteger'. positiveInteger is "derived" from
   * nonNegativeInteger by setting the value of "minInclusive" to be 1. This
   * results in the standard mathematical concept of the positive integer
   * numbers. The "value space" of positiveInteger is the infinite set
   * {1,2,...}.
   */
  POSITIVE_INTEGER(NON_NEGATIVE_INTERGER, "xs:positiveInteger", 7, false),

  /**
   * XML Schema type 'unsignedLong'. unsignedLong is "derived" from
   * nonNegativeInteger by setting the value of "maxInclusive" to be
   * 18446744073709551615.
   */
  UNSIGNED_LONG(NON_NEGATIVE_INTERGER, "xs:unsignedLong", 7, false),

  /**
   * XML Schema type 'unsignedInt'. unsignedInt is "derived" from unsignedLong
   * by setting the value of "maxInclusive" to be 4294967295.
   */
  UNSIGNED_INT(UNSIGNED_LONG, "xs:unsignedInt", 8, false),

  /**
   * XML Schema type 'unsignedShort'. unsignedShort is "derived" from
   * unsignedInt by setting the value of "maxInclusive" to be 65535.
   */
  UNSIGNED_SHORT(UNSIGNED_INT, "xs:unsignedShort", 9, false),

  /**
   * XML Schema type 'unsignedByte'. unsignedByte is "derived" from
   * unsignedShort by setting the value of "maxInclusive" to be 255.
   */
  UNSIGNED_BYTE(UNSIGNED_SHORT, "xs:unsignedByte", 10, false),

  /**
   * XML Schema type 'normalizedString'. normalizedString represents white space
   * normalized strings. The "value space" of normalizedString is the set of
   * strings that do not contain the carriage return (#xD), line feed (#xA) nor
   * tab (#x9) characters. The "lexical space" of normalizedString is the set of
   * strings that do not contain the carriage return (#xD), line feed (#xA) nor
   * tab (#x9) characters.
   */
  NORMALIZED_STRING(STRING, "xs:normalizedString", 5, false),

  /**
   * XML Schema type 'token'. token represents tokenized strings. The "value
   * space" of token is the set of strings that do not contain the carriage
   * return (#xD), line feed (#xA) nor tab (#x9) characters, that have no
   * leading or trailing spaces (#x20) and that have no internal sequences of
   * two or more spaces. The "lexical space" of token is the set of strings that
   * do not contain the carriage return (#xD), line feed (#xA) nor tab (#x9)
   * characters, that have no leading or trailing spaces (#x20) and that have no
   * internal sequences of two or more spaces.
   */
  TOKEN(NORMALIZED_STRING, "xs:token", 6, false),

  /**
   * XML Schema type 'language'. language represents natural language
   * identifiers as defined by [RFC 3066]. The "value space" of language is the
   * set of all strings that are valid language identifiers as defined [RFC
   * 3066] . The "lexical space" of language is the set of all strings that
   * conform to the pattern [a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})* .
   */
  LANGUAGE(TOKEN, "xs:language", 7, false),

  /**
   * XML Schema type 'name'. Name represents XML Names. The "value space" of
   * Name is the set of all strings which "match" the Name production of [XML
   * 1.0 (Second Edition)]. The "lexical space" of Name is the set of all
   * strings which "match" the Name production of [XML 1.0 (Second Edition)].
   */
  NAME(TOKEN, "xs:name", 7, false),

  /**
   * XML Schema type 'NCName'. NCName represents XML "non-colonized" Names. The
   * "value space" of NCName is the set of all strings which "match" the NCName
   * production of Namespaces in XML. The "lexical space" of NCName is the set
   * of all strings which match the NCName production of [Namespaces in XML].
   */
  NCNAME(NAME, "xs:NCName", 8, false),

  /**
   * XML Schema type 'ID'. ID represents the ID attribute type from [XML 1.0
   * (Second Edition)]. The "value space" of ID is the set of all strings that
   * "match" the NCName production in [Namespaces in XML]. The "lexical space"
   * of ID is the set of all strings that "match" the NCName production in
   * [Namespaces in XML].
   */
  ID(NCNAME, "xs:ID", 9, false),

  /**
   * XML Schema type 'IDREF'. IDREF represents the IDREF attribute type from
   * [XML 1.0 (Second Edition)]. The "value space" of IDREF is the set of all
   * strings that "match" the NCName production in [Namespaces in XML]. The
   * "lexical space" of IDREF is the set of strings that "match" the NCName
   * production in Namespaces in XML.
   */
  IDREF(NCNAME, "xs:IDREF", 9, false),

  /**
   * XML Schema type 'ENTITY'. ENTITY represents the ENTITY attribute type from
   * XML 1.0 (Second Edition). The "value space" of ENTITY is the set of all
   * strings that "match" the NCName production in [Namespaces in XML] and have
   * been declared as an unparsed entity in a document type definition. The
   * "lexical space" of ENTITY is the set of all strings that "match" the NCName
   * production in [Namespaces in XML].
   */
  ENTITY(NCNAME, "xs:ENTITY", 9, false),

  /**
   * XML Schema type 'IDREFS'.
   */
  IDFRES(ANY_SIMPLE_TYPE, "xs:IDREFS", 3, false),
  /**
   * XML Schema type 'ENTITIES'. ENTITIES(ANY_SIMPLE_TYPE, "xs:ENTITIES", 3) ,
   * /** XML Schema type 'NMTOKEN'. NMTOKEN represents the NMTOKEN attribute
   * type from XML 1.0 (2nd Edition). The "value space" of NMTOKEN is the set of
   * tokens that "match" the Nmtoken production in XML 1.0 (2nd Edition). The
   * "lexical space" of NMTOKEN is the set of strings that match the Nmtoken
   * production in XML 1.0 (2nd Edition).
   */
  NMTOKEN(TOKEN, "xs:NMTOKEN", 7, false),

  /**
   * XML Schema type 'NMTOKENS'.
   */
  NMTOKENS(ANY_SIMPLE_TYPE, "xs:NMTOKENS", 3, false);

  /** Base type of the type. */
  private final Type derivedFrom;

  /** Name of the type. (Official string representation) */
  private final String stringRepr;

  /** Precedence of a type. The lower the value, the higher the precedence. */
  private final int precedence;

  /** Defines whether the type is a primitive type. */
  private final boolean isPrimitive;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param baseType
   *          The type's base type
   * @param representation
   *          The string representation of the type
   * @param prec
   *          precedence of the type
   * @param primitive
   *          true, of type is a primitive type
   */
  private Type(
      final Type baseType,
      final String representation,
      final int prec,
      final boolean primitive) {

    derivedFrom = baseType;
    stringRepr = representation;
    precedence = prec;
    isPrimitive = primitive;
  }

  /**
   * Declare, whether the type is one of the primitive value types.
   * 
   * @return true, if type is a primitive type
   */
  public boolean isPrimitive() {

    return isPrimitive;
  }

  /**
   * Returns the string representation of the current type.
   * 
   * @return string representation of the type.
   */
  public String getStringRepr() {

    return stringRepr;
  }

  public static Type getLeastCommonType(final int type1, final int type2) {

    return getLeastCommonType(Type.getType(type1), Type.getType(type2));

  }

  /**
   * Returns the least common base type of the given types.
   * 
   * @param type1
   *          First type
   * @param type2
   *          Second type
   * @return the common type of the parameter types
   */
  public static Type getLeastCommonType(final Type type1, final Type type2) {

    Type a = type1;
    Type b = type2;

    if (a.derivedFrom != null && b.derivedFrom != null) {

      // to get a common type, we need both type and baseTypes respectively on
      // the same level of precedence
      if (a.precedence != b.precedence) {
        while (a.precedence > b.precedence) {
          a = a.derivedFrom;
        }

        while (a.precedence < b.precedence) {
          b = b.derivedFrom;
        }
      }

      while (a != b) {
        assert a.precedence == b.precedence;
        // step up the basetypes of the parameters until a common type is found
        // since all types are derived from the same base type (anyType), this
        // is not an infinite loop, in fact this loop will be left when a's and
        // b's type is 'anyType', at the latest.
        a = a.derivedFrom;
        b = b.derivedFrom;
      }
      assert (a == b);

    } else {
      assert a == ANY_TYPE || b == ANY_TYPE;

      a = ANY_TYPE;
    }

    return a;
  }

  /**
   * Tests whether a type is derived by restriction from a certain type.
   * 
   * @param expectedType
   *          the type to check, if this type is derived from
   * @return true, if this type is derived from the input type.
   */
  public boolean derivesFrom(final Type expectedType) {

    if (this == expectedType) {
      return true;
    }

    if (this.precedence > expectedType.precedence) {
      // only if actualType has a lower precedence than expectedTyte,
      // expectedTyte can be a baseType of actualType
      Type baseType = this.derivedFrom;
      while (baseType.precedence > expectedType.precedence) {
        baseType = baseType.derivedFrom;
      }

      assert baseType.precedence == expectedType.precedence;
      return baseType == expectedType;

    } else {
      // if actualType has higher or equal precedence than expectedTyte, it can
      // not be derived from expectedTyte, instead, it could be even the other
      // way around
      return false;
    }
  }

  /**
   * Specifies if the current type is a numeric type or derived from a numeric
   * type.
   * 
   * @return true, if the type is a numeric type.
   */
  public boolean isNumericType() {

    return (derivesFrom(Type.DOUBLE) || derivesFrom(Type.FLOAT) || derivesFrom(Type.DECIMAL));
  }

  /**
   * Determines, whether a value of the current type can be casted to the input
   * type, as defined in XML Schema 1.1-1 Spec.
   * 
   * @param targetType
   *          type to which the current value shall be casted to
   * @param value
   *          value of the source value
   * @return true, if the source value is castable to the target type.
   */
  public boolean isCastableTo(final Type targetType, final String value) {

    // casting to or from NOTATION or anySimpleType is not possible
    if (targetType == NOTATION
        || this == NOTATION
        || targetType == ANY_SIMPLE_TYPE
        || this == ANY_SIMPLE_TYPE) {
      throw new XPathError(ErrorType.XPST0080);
    }

    // (4.a) a type can always be casted to itself, or one of its super types
    if (this == targetType || derivesFrom(targetType)) {
      return true;
    }

    if (isPrimitive() && targetType.isPrimitive()) {
      // source and target types are both primitive atomic types

      if (targetType == UNTYPED_ATOMIC
          || targetType == STRING
          || castsTo(targetType)
          || targetType.facetIsSatisfiedBy(value)) {
        return true;
      } else {
        throw new XPathError(ErrorType.XPTY0004);
      }
    }

    if (!targetType.isPrimitive() && targetType.derivesFrom(this)) {
      // (4.d) if target type is non-primitive-atomic-type and target type is
      // supertype of source type, the source type must satisfy all facets of
      // the target type.
      return targetType.facetIsSatisfiedBy(value);

    } else {
      // (4.e)if the primitive base type of the source is castable to the
      // primitive base type of the target type, the source is castable to the
      // target, if the facets of the target is satisfied.
      // getPrimitiveBaseType(); //TODO: what is that??

      return (getPrimitiveBaseType().isCastableTo(
          targetType.getPrimitiveBaseType(),
          value) && targetType.facetIsSatisfiedBy(value));
    }

    // throw new XPathError(ErrorType.XPTY0004);

  }

  /**
   * Return the next base type of the current type that is a primitive one. If
   * the current type is a primitive type the current type is returned.
   * 
   * @return primitive base type of the current type
   */
  public Type getPrimitiveBaseType() {

    Type type = this;

    if (type == UNTYPED || type == UNTYPED_ATOMIC) { // TODO: this is not a
      return type; // real primitive type
    }
    if (type.precedence <= 3 && type != ANY_ATOMIC_TYPE) {
      throw new IllegalStateException("Type "
          + type.stringRepr
          + " has no primitive base type.");
    }

    while (!type.isPrimitive) {
      type = type.derivedFrom;
    }
    assert (type.isPrimitive);
    return type;

  }

  /**
   * Defines, whether the input string statisfies the pattern of the type.
   * 
   * @param value
   *          the the value as string
   * @return true, if the values structure matches the regular expression of the
   *         type's pattern.
   */
  boolean facetIsSatisfiedBy(final String value) {

    if (getFacet().equals("")) {
      // no facet is specified
      return false;
    } else {
      final Pattern pattern = Pattern.compile(getFacet());
      final Matcher matcher = pattern.matcher(value);
      return matcher.matches();
    }
  }

  /**
   * Gets the facet of the type.
   * 
   * @return type'S facet.
   */
  public String getFacet() {

    return "";

  }

  /**
   * Defines if the current type is castable to the targetType.
   * 
   * @param targetType
   *          the type the current type should be casted to
   * @return true, if it is possible to cast the current type to the targettype
   */
  public boolean castsTo(final Type targetType) {

    return false;
  }

  private static Map<Integer, Type> keyToType;
  static {

    Map<String, Type> nameToKey;

    nameToKey = new HashMap<String, Type>();
    for (Type type : Type.values()) {
      nameToKey.put(type.getStringRepr(), type);
    }

    keyToType = new HashMap<Integer, Type>();
    for (Type type : Type.values()) {
      keyToType.put(type.getStringRepr().hashCode(), type);
    }
  }

  public static Type getType(final int key) {

    final Type type = keyToType.get(key);
    if (type != null) {

      return type;
    }
    throw new XPathError(ErrorType.XPST0051);

  }

}
