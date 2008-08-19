/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.xpath.types;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.types.Type;

public class TypeTest {

  Type anyType, anySimpleType, anyAtomicType, untypedAtomic, untyped, string,
      duration, dateTime, time, date, yearMonth, month, monthDay, year, day,
      bool, base64, hex, anyURI, notation, floatT, doubleT, integerT, longT,
      intT, qName, pDecimal, decimal, shortT, byteT, nPosInt, posInt,
      unsignedLong, name, token, language;

  @Before
  public void setUp() throws Exception {

    anyType = Type.ANY_TYPE;
    anySimpleType = Type.ANY_SIMPLE_TYPE;
    anyAtomicType = Type.ANY_ATOMIC_TYPE;
    untypedAtomic = Type.UNTYPED_ATOMIC;
    untyped = Type.UNTYPED;
    string = Type.STRING;
    duration = Type.DURATION;
    dateTime = Type.DATE_TIME;
    time = Type.TIME;
    date = Type.DATE;
    yearMonth = Type.G_YEAR_MONTH;
    year = Type.G_YEAR;
    monthDay = Type.G_MONTH_DAY;
    day = Type.G_DAY;
    month = Type.G_MONTH;
    bool = Type.BOOLEAN;
    base64 = Type.BASE_64_BINARY;
    hex = Type.HEX_BINARY;
    anyURI = Type.ANY_URI;
    qName = Type.QNAME;
    notation = Type.NOTATION;
    floatT = Type.FLOAT;
    doubleT = Type.DOUBLE;
    pDecimal = Type.PDECIMAL;
    decimal = Type.DECIMAL;
    integerT = Type.INTEGER;
    longT = Type.LONG;
    intT = Type.INT;
    shortT = Type.SHORT;
    byteT = Type.BYTE;
    nPosInt = Type.NON_POSITIVE_INTEGER;
    unsignedLong = Type.UNSIGNED_LONG;
    token = Type.TOKEN;
    language = Type.LANGUAGE;
    name = Type.NAME;
    posInt = Type.POSITIVE_INTEGER;
  }

   @Test
   public final void testGetLeastCommonType() {
     assertEquals(Type.getLeastCommonType(anyType, string), Type.ANY_TYPE);
     assertEquals(Type.getLeastCommonType(anyAtomicType, string), Type.ANY_ATOMIC_TYPE);
     assertEquals(Type.getLeastCommonType(integerT, string), Type.ANY_ATOMIC_TYPE);
     assertEquals(Type.getLeastCommonType(nPosInt, string), Type.ANY_ATOMIC_TYPE);
     assertEquals(Type.getLeastCommonType(shortT, decimal), Type.DECIMAL);
     assertEquals(Type.getLeastCommonType(name, token), Type.TOKEN);
     assertEquals(Type.getLeastCommonType(hex, date), Type.ANY_ATOMIC_TYPE);
   }
  
   @Test
   public final void testDerivesFrom() {
     assertEquals(anyType.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(anyType.derivesFrom(Type.ANY_ATOMIC_TYPE), false);
     assertEquals(anyType.derivesFrom(Type.INTEGER), false);
     
     assertEquals(anySimpleType.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(anySimpleType.derivesFrom(Type.STRING), false);
     assertEquals(anySimpleType.derivesFrom(Type.ANY_SIMPLE_TYPE), true);
     assertEquals(anySimpleType.derivesFrom(Type.INTEGER), false);
     
     assertEquals(anyAtomicType.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(anyAtomicType.derivesFrom(Type.ANY_SIMPLE_TYPE), true);
     assertEquals(anyAtomicType.derivesFrom(Type.DATE), false);
     assertEquals(anyAtomicType.derivesFrom(Type.STRING), false);
     assertEquals(anyAtomicType.derivesFrom(Type.TOKEN), false);
     
     assertEquals(string.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(string.derivesFrom(Type.ANY_SIMPLE_TYPE), true);
     assertEquals(string.derivesFrom(Type.DATE), false);
     assertEquals(string.derivesFrom(Type.STRING), true);
     assertEquals(string.derivesFrom(Type.TOKEN), false);
     
     assertEquals(floatT.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(floatT.derivesFrom(Type.ANY_ATOMIC_TYPE), true);
     assertEquals(floatT.derivesFrom(Type.DATE), false);
     assertEquals(floatT.derivesFrom(Type.STRING), false);
     assertEquals(floatT.derivesFrom(Type.TOKEN), false);
     
     
     assertEquals(doubleT.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(doubleT.derivesFrom(Type.ANY_ATOMIC_TYPE), true);
     assertEquals(doubleT.derivesFrom(Type.DATE), false);
     assertEquals(doubleT.derivesFrom(Type.STRING), false);
     assertEquals(doubleT.derivesFrom(Type.TOKEN), false);
     
     assertEquals(integerT.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(integerT.derivesFrom(Type.ANY_ATOMIC_TYPE), true);
     assertEquals(integerT.derivesFrom(Type.DATE), false);
     assertEquals(integerT.derivesFrom(Type.STRING), false);
     assertEquals(integerT.derivesFrom(Type.DECIMAL), true);
     
     assertEquals(longT.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(longT.derivesFrom(Type.ANY_ATOMIC_TYPE), true);
     assertEquals(longT.derivesFrom(Type.DATE), false);
     assertEquals(longT.derivesFrom(Type.STRING), false);
     assertEquals(longT.derivesFrom(Type.DECIMAL), true);
     assertEquals(longT.derivesFrom(Type.INTEGER), true);
     
     assertEquals(intT.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(intT.derivesFrom(Type.ANY_ATOMIC_TYPE), true);
     assertEquals(intT.derivesFrom(Type.DATE), false);
     assertEquals(intT.derivesFrom(Type.STRING), false);
     assertEquals(intT.derivesFrom(Type.DECIMAL), true);
     assertEquals(intT.derivesFrom(Type.INTEGER), true);
     assertEquals(intT.derivesFrom(Type.INT), true);
     assertEquals(intT.derivesFrom(Type.LONG), true);
     

     assertEquals(qName.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(qName.derivesFrom(Type.ANY_ATOMIC_TYPE), true);
     assertEquals(qName.derivesFrom(Type.DATE), false);
     assertEquals(qName.derivesFrom(Type.STRING), false);
     assertEquals(qName.derivesFrom(Type.DECIMAL), false);
     assertEquals(qName.derivesFrom(Type.INTEGER), false);
     assertEquals(qName.derivesFrom(Type.INT), false);
     assertEquals(qName.derivesFrom(Type.LONG), false);
     
     assertEquals(name.derivesFrom(Type.ANY_TYPE), true);
     assertEquals(name.derivesFrom(Type.ANY_ATOMIC_TYPE), true);
     assertEquals(name.derivesFrom(Type.DATE), false);
     assertEquals(name.derivesFrom(Type.STRING), true);
     assertEquals(name.derivesFrom(Type.DECIMAL), false);
     assertEquals(name.derivesFrom(Type.INTEGER), false);
     assertEquals(name.derivesFrom(Type.INT), false);
     assertEquals(name.derivesFrom(Type.LONG), false);
     assertEquals(name.derivesFrom(Type.NORMALIZED_STRING), true);
     assertEquals(name.derivesFrom(Type.TOKEN), true);
   }
  
  @Test
  public final void testGetStringRepresentation() {

    assertEquals(anyType.getStringRepr(), "xs:anyType");
    assertEquals(anySimpleType.getStringRepr(), "xs:anySimpleType");
    assertEquals(anyAtomicType.getStringRepr(), "xs:anyAtomicType");
    assertEquals(untypedAtomic.getStringRepr(), "xs:untypedAtomic");
    assertEquals(untyped.getStringRepr(), "xs:untyped");
    assertEquals(string.getStringRepr(), "xs:string");
    assertEquals(duration.getStringRepr(), "xs:duration");
    assertEquals(dateTime.getStringRepr(), "xs:dateTime");
    assertEquals(time.getStringRepr(), "xs:time");
    assertEquals(date.getStringRepr(), "xs:date");
    assertEquals(yearMonth.getStringRepr(), "xs:gYearMonth");
    assertEquals(month.getStringRepr(), "xs:gMonth");
    assertEquals(monthDay.getStringRepr(), "xs:gMonthDay");
    assertEquals(year.getStringRepr(), "xs:gYear");
    assertEquals(day.getStringRepr(), "xs:gDay");
    assertEquals(bool.getStringRepr(), "xs:boolean");
    assertEquals(base64.getStringRepr(), "xs:base64Binary");
    assertEquals(hex.getStringRepr(), "xs:hexBinary");
    assertEquals(anyURI.getStringRepr(), "xs:anyURI");
    assertEquals(notation.getStringRepr(), "xs:NOTATION");
    assertEquals(floatT.getStringRepr(), "xs:float");
    assertEquals(doubleT.getStringRepr(), "xs:double");
    assertEquals(integerT.getStringRepr(), "xs:integer");
    assertEquals(longT.getStringRepr(), "xs:long");
    assertEquals(intT.getStringRepr(), "xs:int");
    assertEquals(qName.getStringRepr(), "xs:QName");
    assertEquals(pDecimal.getStringRepr(), "xs:pDecimal");
    assertEquals(decimal.getStringRepr(), "xs:decimal");
    assertEquals(shortT.getStringRepr(), "xs:short");
    assertEquals(byteT.getStringRepr(), "xs:byte");
    assertEquals(nPosInt.getStringRepr(), "xs:nonPositiveInteger");
    assertEquals(posInt.getStringRepr(), "xs:positiveInteger");
    assertEquals(unsignedLong.getStringRepr(), "xs:unsignedLong");
    assertEquals(name.getStringRepr(), "xs:name");
    assertEquals(token.getStringRepr(), "xs:token");
    assertEquals(language.getStringRepr(), "xs:language");

  }

  @Test
  public final void testIsNumericType() {

    assertEquals(anyType.isNumericType(), false);
    assertEquals(anySimpleType.isNumericType(), false);
    assertEquals(anyAtomicType.isNumericType(), false);
    assertEquals(untypedAtomic.isNumericType(), false);
    assertEquals(untyped.isNumericType(), false);
    assertEquals(string.isNumericType(), false);
    assertEquals(duration.isNumericType(), false);
    assertEquals(dateTime.isNumericType(), false);
    assertEquals(time.isNumericType(), false);
    assertEquals(date.isNumericType(), false);
    assertEquals(yearMonth.isNumericType(), false);
    assertEquals(month.isNumericType(), false);
    assertEquals(monthDay.isNumericType(), false);
    assertEquals(year.isNumericType(), false);
    assertEquals(day.isNumericType(), false);
    assertEquals(bool.isNumericType(), false);
    assertEquals(base64.isNumericType(), false);
    assertEquals(hex.isNumericType(), false);
    assertEquals(anyURI.isNumericType(), false);
    assertEquals(notation.isNumericType(), false);
    assertEquals(floatT.isNumericType(), true);
    assertEquals(doubleT.isNumericType(), true);
    assertEquals(integerT.isNumericType(), true);
    assertEquals(longT.isNumericType(), true);
    assertEquals(intT.isNumericType(), true);
    assertEquals(qName.isNumericType(), false);
    assertEquals(pDecimal.isNumericType(), false);
    assertEquals(decimal.isNumericType(), true);
    assertEquals(shortT.isNumericType(), true);
    assertEquals(byteT.isNumericType(), true);
    assertEquals(nPosInt.isNumericType(), true);
    assertEquals(posInt.isNumericType(), true);
    assertEquals(unsignedLong.isNumericType(), true);
    assertEquals(name.isNumericType(), false);
    assertEquals(token.isNumericType(), false);
    assertEquals(language.isNumericType(), false);
  }

  
  @Test
  public final void testIsPrimitiv() {

    assertEquals(anyType.isPrimitive(), false);
    assertEquals(anySimpleType.isPrimitive(), false);
    assertEquals(anyAtomicType.isPrimitive(), false);
    assertEquals(untypedAtomic.isPrimitive(), false);
    assertEquals(untyped.isPrimitive(), false);
      assertEquals(duration.isPrimitive(), true);
    assertEquals(dateTime.isPrimitive(), true);
    assertEquals(time.isPrimitive(), true);
    assertEquals(date.isPrimitive(), true);
    assertEquals(yearMonth.isPrimitive(), true);
    assertEquals(month.isPrimitive(), true);
    assertEquals(monthDay.isPrimitive(), true);
    assertEquals(year.isPrimitive(), true);
    assertEquals(day.isPrimitive(), true);
    assertEquals(bool.isPrimitive(), true);
    assertEquals(base64.isPrimitive(), true);
    assertEquals(hex.isPrimitive(), true);
    assertEquals(anyURI.isPrimitive(), true);
    assertEquals(notation.isPrimitive(), true);
    assertEquals(floatT.isPrimitive(), true);
    assertEquals(doubleT.isPrimitive(), true);
    assertEquals(integerT.isPrimitive(), false);
    assertEquals(longT.isPrimitive(), false);
    assertEquals(intT.isPrimitive(), false);
    assertEquals(qName.isPrimitive(), true);
    assertEquals(pDecimal.isPrimitive(), true);
    assertEquals(decimal.isPrimitive(), true);
    assertEquals(shortT.isPrimitive(), false);
    assertEquals(byteT.isPrimitive(), false);
    assertEquals(nPosInt.isPrimitive(), false);
    assertEquals(posInt.isPrimitive(), false);
    assertEquals(unsignedLong.isPrimitive(), false);
    assertEquals(name.isPrimitive(), false);
    assertEquals(token.isPrimitive(), false);
    assertEquals(language.isPrimitive(), false);
  }
  
  //set modifier of getPrimitiveBaseType to private
  @Test
  public final void testgetPrimBT() {

    assertEquals(notation.getPrimitiveBaseType(), Type.NOTATION);
    assertEquals(integerT.getPrimitiveBaseType(), Type.DECIMAL);
    assertEquals(longT.getPrimitiveBaseType(), Type.DECIMAL);
    assertEquals(intT.getPrimitiveBaseType(), Type.DECIMAL);
   
    assertEquals(pDecimal.getPrimitiveBaseType(), Type.PDECIMAL);
    assertEquals(decimal.getPrimitiveBaseType(), Type.DECIMAL);
    assertEquals(shortT.getPrimitiveBaseType(), Type.DECIMAL);
    assertEquals(byteT.getPrimitiveBaseType(), Type.DECIMAL);
    assertEquals(name.getPrimitiveBaseType(), Type.STRING);
    assertEquals(token.getPrimitiveBaseType(), Type.STRING);
    assertEquals(language.getPrimitiveBaseType(), Type.STRING);
  }
  
  @Test
  public final void testFacets() {
    assertEquals(true, string.facetIsSatisfiedBy("hallo welt!"));
    assertEquals(true, string.facetIsSatisfiedBy("r7321741237r8gruqewfgducnb2138"));
    assertEquals(true, string.facetIsSatisfiedBy("-12.E24"));
    assertEquals(true, string.facetIsSatisfiedBy("&%)=1"));
    assertEquals(true, string.facetIsSatisfiedBy("\""));
    
    assertEquals(true, integerT.facetIsSatisfiedBy("12345"));
    assertEquals(true, integerT.facetIsSatisfiedBy("-12345"));
    assertEquals(false, integerT.facetIsSatisfiedBy("123-45"));
    assertEquals(false, integerT.facetIsSatisfiedBy("1234.5"));
    
    assertEquals(true, floatT.facetIsSatisfiedBy(".12345"));
    assertEquals(true, floatT.facetIsSatisfiedBy("-.12345"));
    assertEquals(true, floatT.facetIsSatisfiedBy("123E-45"));
    assertEquals(true, floatT.facetIsSatisfiedBy("1234.5"));
    
    assertEquals(true, doubleT.facetIsSatisfiedBy(".12345"));
    assertEquals(true, doubleT.facetIsSatisfiedBy("-.12345"));
    assertEquals(true, doubleT.facetIsSatisfiedBy("123E-45"));
    assertEquals(false, doubleT.facetIsSatisfiedBy("Hallo"));
    
    assertEquals(true, bool.facetIsSatisfiedBy("1"));
    assertEquals(false, bool.facetIsSatisfiedBy("2"));
    assertEquals(true, bool.facetIsSatisfiedBy("0"));
    assertEquals(true, bool.facetIsSatisfiedBy("true"));
  }
  
  
  @Test 
  public final void testCastability() {
  assertEquals(true, string.isCastableTo(Type.INTEGER, "-1232138"));
  assertEquals(true, string.isCastableTo(Type.BOOLEAN, "1"));
  
  }
  
  @Test (expected = XPathError.class)
  public final void testCastException() {
    assertEquals(false, string.isCastableTo(Type.INTEGER, "hallo welt!"));
    assertEquals(false, string.isCastableTo(Type.BOOLEAN, "13"));

    assertEquals(true, string.isCastableTo(Type.NOTATION, "\""));
    
    assertEquals(true, integerT.isCastableTo(Type.DOUBLE, "12345"));
    assertEquals(true, integerT.isCastableTo(Type.FLOAT, "-12345"));
    //TODO: add more tests
  }

}
